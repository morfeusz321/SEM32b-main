package nl.tudelft.sem.template.request.controllers;

import java.util.List;
import java.util.Optional;
import javassist.tools.web.BadHttpRequest;
import nl.tudelft.sem.template.request.domain.RequestService;
import nl.tudelft.sem.template.request.domain.Status;
import nl.tudelft.sem.template.request.models.RequestRequestModel;
import nl.tudelft.sem.template.request.models.RequestResponseModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



@RestController
public class RequestController {
    private final transient RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }


    /**
     * Method to pass a built-in gradle test.
     *
     * @return "Hello"
     */
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello";
    }

    /**
     * Creates a request in the database.
     *
     * @return Confirmation that request was succesfully created.
     */
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponseModel createRequest(@RequestHeader("Authorization") String bearerToken,
                                              @RequestBody RequestRequestModel requestModel) {
        try {
            RequestResponseModel requestResponseModel = requestService.createRequest(requestModel, bearerToken);
            return requestResponseModel;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect format");
        }
    }

    /**
     * Deletes request from the database. The user should only be able
     * to delete a request if he/she is the owner.
     *
     * @return Response that the deletion was successful.
     */
    @DeleteMapping("/request/{userId}/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> deleteRequest(@PathVariable long requestId,
                                                @PathVariable long userId)
            throws Exception {
        requestService.deleteRequest(requestId, userId);
        return ResponseEntity.ok().body("");
    }

    /**
     * Edits an existing request in the database. A user is only allowed
     * to make changes to a request, when he is the activity owner.
     *
     * @return Response that editing of request was succesful.
     */
    @PutMapping("/request/{userId}/{requestId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponseModel editRequest(@RequestHeader("Authorization") String bearerToken,
                                            @PathVariable long requestId,
                                            @RequestBody RequestRequestModel requestModel) {
        RequestResponseModel requestResponseModel = requestService.updateRequest(requestModel, requestId, bearerToken);
        return requestResponseModel;
    }

    /**
     * Sets the status of the request to accepted. Only the owner of the
     * request is allowed to do this.
     *
     * @return Response if the accepting was succesful.
     */
    @PutMapping("/request/{userId}/{requestId}/accept")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponseModel acceptRequest(@RequestHeader("Authorization") String bearerToken,
                                              @PathVariable long requestId,
                                              @PathVariable long userId) throws BadHttpRequest {
        RequestResponseModel requestResponseModel = requestService.acceptRequest(requestId, userId, bearerToken);
        return requestResponseModel;
    }

    /**
     * Declines a request. Only the owner of the request is allowed to do this.
     *
     * @return Response if the request is succesfully deleted.
     */
    @PutMapping("/request/{userId}/{requestId}/decline")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponseModel declineRequest(@PathVariable long requestId,
                                               @PathVariable long userId) throws BadHttpRequest {
        RequestResponseModel requestResponseModel = requestService.declineRequest(requestId, userId);
        return requestResponseModel;
    }

    @GetMapping("/users/{userId}/requests/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    public RequestResponseModel getRequestById(@PathVariable long userId, @PathVariable long requestId) {
        RequestResponseModel requestResponseModel = requestService.getRequestById(requestId);
        return requestResponseModel;
    }

    /**
     * javadoc.
     *
     * @return ja
     */
    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestResponseModel> listRequests(@PathVariable Long userId, @RequestParam Optional<Status> status) {
        List<RequestResponseModel> requests = requestService.listRequestsByUserId(userId, status);
        return requests;
    }
}