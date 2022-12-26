package nl.tudelft.sem.template.request.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javassist.tools.web.BadHttpRequest;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.sem.template.request.domain.exceptions.ActivityDoesNotExist;
import nl.tudelft.sem.template.request.domain.exceptions.RequestNotFoundException;
import nl.tudelft.sem.template.request.domain.exceptions.UserDoesNotExistException;
import nl.tudelft.sem.template.request.event.RequestCreatedEvent;
import nl.tudelft.sem.template.request.models.FillPositionRequestModel;
import nl.tudelft.sem.template.request.models.RequestRequestModel;
import nl.tudelft.sem.template.request.models.RequestResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@Slf4j
public class RequestService {
    public static final String HTTP_LOCALHOST_8083 = "http://localhost:8083";
    public static final String HTTP_LOCALHOST_8082 = "http://localhost:8082";
    private final transient RequestRepository requestRepository;

    private final transient WebClient webClientUser;

    private final transient WebClient webClientActivity;

    private final transient KafkaTemplate<String, RequestCreatedEvent> kafkaTemplate;

    /**
     * Constructor for autowiring.
     */
    @Autowired(required = true)
    public RequestService(RequestRepository requestRepository, KafkaTemplate kafkaTemplate) {
        this.requestRepository = requestRepository;
        this.webClientUser = WebClient.create(HTTP_LOCALHOST_8082);
        this.webClientActivity = WebClient.create(HTTP_LOCALHOST_8083);
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Constructor used for testing that allows to change base urls of web clients.
     *
     * @param baseUser base url for user
     * @param baseActivity base url for activity
     */
    public RequestService(RequestRepository requestRepository, KafkaTemplate kafkaTemplate,
                          String baseUser, String baseActivity) {
        this.requestRepository = requestRepository;
        this.webClientUser = WebClient.create(baseUser);
        this.webClientActivity = WebClient.create(baseActivity);
        this.kafkaTemplate = kafkaTemplate;
    }


    /**
     * Retrieves request entity form the database with provided request id.
     *
     * @param requestId id of the request that you want to retrieve.
     * @return request retrieved from the database.
     */
    public RequestResponseModel getRequestById(Long requestId) {

        log.info(String.format("RequestService.getRequest: Trying to get request with id: %d", requestId));
        Request request = getRequest(requestId);

        log.info(String.format("RequestService.getRequest: Request with id: %d has been found", requestId));
        return mapToRequestResponseModel(request);
    }

    /**
     * Lists the requests of which user is the owner.
     *
     * @param userId for who you want to list the requests.
     * @return list of the requests.
     */
    public List<RequestResponseModel> listRequestsByUserId(Long userId, Optional<Status> status) {
        log.info(String.format(
                "RequestService.listRequestsByUserId: Trying to list requests for user with id: %d",
                userId));
        List<Request> requests;
        if (status.isPresent()) {
            requests = requestRepository.findByOwnerIdAndStatus(userId, status.get());
        } else {
            requests = requestRepository.findByOwnerId(userId);
        }

        return requests.stream().map(this::mapToRequestResponseModel).collect(Collectors.toList());
    }

    /**
     * Creates new request entity with properties from the requestModel and saves it in the database.
     *
     * @param requestRequestModel requestModel containing parameters for the new entity.
     * @return Newly created request entity that has been saved in the database.
     */
    public RequestResponseModel createRequest(RequestRequestModel requestRequestModel, String bearerToken) {

        log.info("RequestService.createRequest: Trying to create a new request.");
        validatePayload(requestRequestModel, bearerToken);
        Request request = buildRequest(requestRequestModel);

        log.info("RequestService.createRequest: Trying to save a new request.");
        request = requestRepository.save(request);

        log.info(String.format(
                "RequestService.createRequest: New request with id: %d has been successfully saved in database",
                request.getRequesterId()));
        kafkaTemplate.send("notificationTopic", new RequestCreatedEvent(request.getId()));
        return mapToRequestResponseModel(request);
    }

    /**
     * Creates new request entity with properties from the requestModel and saves it in the database.
     *
     * @param requestId           of the request that you want to update.
     * @param requestRequestModel requestModel containing parameters for the updated entity.
     * @return Updated request entity that has been saved in the database.
     */
    public RequestResponseModel updateRequest(RequestRequestModel requestRequestModel, Long requestId, String bearerToken) {

        log.info(String.format("RequestService.updateRequest: Trying to update a request with id: %d.", requestId));
        validatePayload(requestRequestModel, bearerToken);
        Request updateRequest = buildRequest(requestRequestModel);
        log.info(String.format("RequestService.updateRequest: Trying to save a request with id: %d.", requestId));
        return getRequestResponseModel(requestId, updateRequest);
    }

    private RequestResponseModel getRequestResponseModel(Long requestId, Request updateRequest) {
        return requestRepository.findById(requestId)
                .map(request -> {
                    request.setRequesterId(updateRequest.getRequesterId());
                    request.setStatus(updateRequest.getStatus());
                    request.setPositions(updateRequest.getPositions());
                    request.setActivityId(updateRequest.getActivityId());
                    request.setOwnerId(updateRequest.getOwnerId());
                    return mapToRequestResponseModel(requestRepository.save(request));
                })
                .orElseGet(() -> {
                    return mapToRequestResponseModel(requestRepository.save(updateRequest));
                });
    }

    public RequestResponseModel declineRequest(Long requestId, Long userId) throws BadHttpRequest {
        Request request = changeStatusRequest(requestId, userId, Status.DENIED);
        return mapToRequestResponseModel(request);
    }

    /**
     * Change the status of request to accepted.
     *
     * @param requestId that was accepted.
     * @param userId    that owns activity.
     * @return updated request.
     * @throws BadHttpRequest is thrown if the user is not the owner of the activity.
     */
    public RequestResponseModel acceptRequest(Long requestId, Long userId, String bearerToken) throws BadHttpRequest {
        Request request = changeStatusRequest(requestId, userId, Status.ACCEPTED);
        updateActivity(request, bearerToken);
        return mapToRequestResponseModel(request);
    }

    /**
     * Update the activity's positions with the one that was taken.
     *
     * @param request that was accepted.
     */
    private void updateActivity(Request request, String bearerToken) {
        FillPositionRequestModel fillPositionRequestModel = new FillPositionRequestModel(request.getPositions(),
                request.getActivityId());
        webClientActivity.put().uri(uriBuilder -> uriBuilder.path("fillPosition").build())
                .body(Mono.just(fillPositionRequestModel), FillPositionRequestModel.class)
                .header("Authorization", bearerToken)
                .retrieve().onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new ActivityDoesNotExist()))).bodyToMono(String.class).block();
    }

    private Request changeStatusRequest(Long requestId, Long userId, Status accepted) throws BadHttpRequest {
        Request request = getRequest(requestId);
        checkIfUserOwnsActivity(userId, request);
        request.setStatus(accepted);
        requestRepository.save(request);
        return request;
    }

    /**
     * Deletes request entity with given id from the database.
     *
     * @param requestId of the request that you want to delete.
     */
    public void deleteRequest(Long requestId, Long userId) throws BadHttpRequest {
        log.warn(String.format("RequestService.deleteRequest: Trying to delete a request with id: %d.", requestId));
        Request request = getRequest(requestId);
        //TODO: Check if user has permissions to do so.
        checkIfUserOwnsActivity(userId, request);
        requestRepository.delete(request);
        log.info(String.format(
                "RequestService.updateRequest: The request with id: %d has been successfully saved in database",
                requestId));
    }

    private RequestResponseModel mapToRequestResponseModel(Request request) {
        RequestResponseModel requestResponseModel = RequestResponseModel.builder()
                .requesterId(request.getRequesterId())
                .activityId(request.getActivityId())
                .ownerId(request.getOwnerId())
                .id(request.getId())
                .positions(request.getPositions())
                .status(request.getStatus()).build();
        return requestResponseModel;
    }

    /**
     * Checks if the user is the owner of the activity for which request was created.
     *
     * @param userId  of user that you want to verify.
     * @param request that you want to check if the user is the owner.
     * @throws BadHttpRequest since the wrong user has been provided.
     */
    private void checkIfUserOwnsActivity(Long userId, Request request) throws BadHttpRequest {
        if (!Objects.equals(request.getOwnerId(), userId)) {
            throw new BadHttpRequest();
        }
    }

    /**
     * Checks if all the provided ids actually exist in the databases if not throws an exception.
     *
     * @param requestRequestModel that you want to check for validity.
     */
    private void validatePayload(RequestRequestModel requestRequestModel, String bearerToken) {
        validateIfUserExists(requestRequestModel.getRequesterId(), bearerToken);
        validateIfUserExists(requestRequestModel.getOwnerId(), bearerToken);
        validateIfActivityExists(requestRequestModel.getActivityId(), bearerToken);
    }

    /**
     * Checks if the provided activityId exists in the database, if not throws exception.
     *
     * @param activityId of the activity that you want to verify.
     */
    private void validateIfActivityExists(Long activityId, String bearerToken) {
        webClientActivity.get().uri(uriBuilder -> uriBuilder.path("activities/" + activityId).build())
                .header("Authorization", bearerToken).retrieve()
                .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                .flatMap(error -> Mono.error(new ActivityDoesNotExist()))).bodyToMono(String.class).block();
    }

    /**
     * Checks if the provided user id exists in the database, if not throws an exception.
     *
     * @param userId that you want to verify.
     */
    private void validateIfUserExists(Long userId, String bearerToken) {
        webClientUser.get().uri(uriBuilder -> uriBuilder.path("exists/" + userId).build())
                .header("Authorization", bearerToken).retrieve()
                .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                .flatMap(error -> Mono.error(new UserDoesNotExistException()))).bodyToMono(String.class).block();
    }

    /**
     * Retrieves request entity from the database based on provided id, if it is not found throws exception.
     *
     * @param requestId that you want to retrieve from the database.
     * @return found request
     */
    private Request getRequest(Long requestId) {
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isEmpty()) {
            throw new RequestNotFoundException();
        }
        return request.get();
    }

    private static Request buildRequest(RequestRequestModel requestRequestModel) {
        System.out.println(requestRequestModel.getPositions());
        Request request = Request.builder().requesterId(requestRequestModel.getRequesterId())
                .activityId(requestRequestModel.getActivityId())
                .ownerId(requestRequestModel.getOwnerId())
                .positions(requestRequestModel.getPositions()).build();
        return request;
    }

}
