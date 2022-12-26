package nl.tudelft.sem.template.user.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.sem.template.user.authentication.AuthManager;
import nl.tudelft.sem.template.user.domain.enums.Position;
import nl.tudelft.sem.template.user.domain.enums.Status;
import nl.tudelft.sem.template.user.models.CreateCompetitionRequestModel;
import nl.tudelft.sem.template.user.models.CreateTrainingRequestModel;
import nl.tudelft.sem.template.user.models.RequestActivitiesModel;
import nl.tudelft.sem.template.user.models.RequestRequestModel;
import nl.tudelft.sem.template.user.models.RequestResponseModel;
import nl.tudelft.sem.template.user.models.UpdateCompetitionRequestModel;
import nl.tudelft.sem.template.user.models.UpdateTrainingRequestModel;
import nl.tudelft.sem.template.user.models.UserRequestModel;
import nl.tudelft.sem.template.user.models.UserResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@Slf4j
public class UserService {
    public static final String AUTHORIZATION = "Authorization";
    private final transient UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final transient AuthManager authManager;
    private final transient WebClient webClient;

    /**
     * Creates new instance of  UserService.
     */
    public UserService(UserRepository userRepository, AuthManager authManager, WebClient webClient) {
        this.userRepository = userRepository;
        this.authManager = authManager;
        this.webClient = webClient;
    }

    /**
     * Find user by its netId.
     */
    public UserResponseModel findById(Long id) {
        logger.info(String.format("UserService.getUser: Trying to get user with id: %d", id));
        User user = userRepository.findById(id);

        logger.info(String.format("UserService.getUser: User with id: %d has been found", id));
        return mapToUserResponseModel(user);
    }

    /**
     * Check if an existing user already uses a netId.
     */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Save a user.
     */
    public UserResponseModel save(UserRequestModel userRequestModel) {
        User user = User.builder()
                .certificate(userRequestModel.getCertificate())
                .username(userRequestModel.getUsername())
                .firstName(userRequestModel.getFirstName())
                .gender(userRequestModel.getGender())
                .lastName(userRequestModel.getLastName())
                .level(userRequestModel.getLevel())
                .organisation(userRequestModel.getOrganisation())
                .positions(userRequestModel.getPositions())
                .availabilities(userRequestModel.getAvailabilities())
                .build();
        user = userRepository.save(user);
        UserResponseModel userResponseModel = mapToUserResponseModel(user);
        return userResponseModel;
    }

    /**
     * Delete a user.
     */
    public void delete(User user) {
        userRepository.delete(user);
    }

    /**
     * Delete a user by its netId.
     */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Get all users.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Get all users by their netId.
     */
    public List<User> findAllById(Long id) {
        return userRepository.findAllById(id);
    }

    /**
     * Maps user to userResponseModel.
     */
    public UserResponseModel mapToUserResponseModel(User user) {
        return UserResponseModel.builder()
                .username(user.getUsername())
                .id(user.getId())
                .positions(user.getPositions())
                .gender(user.getGender())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .level(user.getLevel())
                .organisation(user.getOrganisation())
                .certificate(user.getCertificate())
                .availabilities(user.getAvailabilities())
                .build();
    }

    private UserRequestModel mapToUserRequestModel(User user) {
        return UserRequestModel.builder()
                .username(user.getUsername())
                .positions(user.getPositions())
                .gender(user.getGender())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .level(user.getLevel())
                .organisation(user.getOrganisation())
                .certificate(user.getCertificate())
                .availabilities(user.getAvailabilities())
                .build();
    }

    /**
     * Method for extracting the username from the token.
     *
     * @return the username of the user.
     */
    public String getUsername() {
        return authManager.getNetId();
    }

    /**
     * Method for returning the user with given username.
     *
     * @return The user data of the user with given username
     * @throws Exception if user does not exist
     */
    public User findByUsername() throws Exception {
        for (User user : findAll()) {
            if (user.getUsername().equals(getUsername())) {
                return user;
            }
        }
        throw new Exception("User does not exist!");
    }

    /**
     * Method for retrieving all the competitions a user can participate in.
     *
     * @param user     the user for which to retrieve the competitions
     * @param position the position the user wants to fill
     * @param token    the token provided when authenticating
     * @return All the competitions the user can participate in.
     */
    public List<Competition> getCompetitions(User user, Position position, String token) {
        RequestActivitiesModel activitiesModel = new RequestActivitiesModel(user.getId(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getOrganisation(), user.getCertificate(),
                user.getGender(), user.getLevel(), position, new ArrayList<>(user.getAvailabilities()));
        log.info("Trying to get all activities for the user {}", user.getId());
        return webClient
                .method(HttpMethod.GET)
                .uri("http://localhost:8083/retrieveCompatible/competition")
                .body(Mono.just(activitiesModel), RequestActivitiesModel.class)
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Competition>>() {
                }).block();
    }

    /**
     * Method for retrieving all the trainings a user can participate in.
     *
     * @param user     the user for which to retrieve the trainings
     * @param position the position the user wants to fill
     * @param token    the token provided when authenticating
     * @return All the trainings the user can participate in.
     */

    public List<Training> getTrainings(User user, Position position, String token) {
        RequestActivitiesModel activitiesModel = new RequestActivitiesModel(user.getId(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getOrganisation(), user.getCertificate(),
                user.getGender(), user.getLevel(), position, new ArrayList<>(user.getAvailabilities()));
        log.info("Trying to get all activities for the user {}", user.getId());
        return webClient
                .method(HttpMethod.GET)
                .uri("http://localhost:8083/retrieveCompatible/training")
                .body(Mono.just(activitiesModel), RequestActivitiesModel.class)
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Training>>() {
                }).block();
    }

    /**
     * Method for sending a request to join an activity.
     *
     * @param user     the user which is to join the activity.
     * @param position the position the user wants to fill.
     * @param id       the id of the activity the user wants to join.
     * @param token    the token provided when authenticating
     * @throws Exception if for any reason the user cannot join the activity
     */
    public void joinActivity(User user, Positions position, long id, String token) throws Exception {
        List<Training> trainings = getTrainings(user, position.getPosition(), token);
        for (Training training : trainings) {
            if (training.getActivityId() == id) {
                RequestRequestModel requestModel = new RequestRequestModel((long) (training.getOwnerId()),
                        (long) (training.getActivityId()), user.getId(), Status.PENDING, position.getPosition());
                webClient
                        .method(HttpMethod.POST)
                        .uri("http://localhost:8086/request")
                        .body(Mono.just(requestModel), RequestRequestModel.class)
                        .header(AUTHORIZATION, token)
                        .retrieve()
                        .bodyToMono(Object.class).block();
                return;
            }
        }
        List<Competition> competitions = getCompetitions(user, position.getPosition(), token);
        for (Competition competition : competitions) {
            if (competition.getActivityId() == id) {
                RequestRequestModel requestModel = new RequestRequestModel((long) (competition.getOwnerId()),
                        (long) (competition.getActivityId()), user.getId(), Status.PENDING, position.getPosition());
                webClient
                        .method(HttpMethod.POST)
                        .uri("http://localhost:8086/request")
                        .body(Mono.just(requestModel), RequestRequestModel.class)
                        .header(AUTHORIZATION, token)
                        .retrieve()
                        .bodyToMono(Object.class).block();
                return;
            }
        }
        throw new Exception("Activity does not exist or user cannot join it");
    }

    /**
     * Gets the list of pending request from request service.
     *
     * @param bearerToken token of the requester.
     * @param id          of the user whose pending responses you want to get.
     * @return list of requests.
     */
    public List<RequestResponseModel> getPendingRequests(String bearerToken, long id) {
        log.info("Trying to get pending requests for user {}", id);
        return webClient
                .method(HttpMethod.GET)
                .uri("http://localhost:8086/users/" + id + "/requests?status=PENDING")
                .body(Mono.just(RequestResponseModel.class), RequestActivitiesModel.class)
                .header(AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RequestResponseModel>>() {
                }).block();
    }

    /**
     * Method for creating a new training.
     *
     * @param training the training model to be added
     * @param token    the token provided when authenticating
     * @return whether the training was successfully added
     */
    public String saveTraining(CreateTrainingRequestModel training, String token) {
        String message = webClient
                .method(HttpMethod.PUT)
                .uri("http://localhost:8083/createActivity/training")
                .bodyValue(training)
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(String.class).block();
        return message;
    }

    /**
     * Method for creating a new competition.
     *
     * @param competition the competition model to be added
     * @param token       the token provided when authenticating
     * @return whether the competition was successfully added
     */
    public String saveCompetition(CreateCompetitionRequestModel competition, String token) {
        String message = webClient
                .method(HttpMethod.PUT)
                .uri("http://localhost:8083/createActivity/competition")
                .bodyValue(competition)
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(String.class).block();
        return message;
    }

    /**
     * Method for deleting an activity.
     *
     * @param userId     the id of the user who is deleting the activity
     * @param activityId the id of the activity
     * @param token      the token provided when authenticating
     * @return whether the activity was successfully deleted
     */
    public String deleteActivity(int userId, int activityId, String token) {
        String message = webClient
                .method(HttpMethod.DELETE)
                .uri("http://localhost:8083/deleteActivity/" + activityId + "/" + userId)
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(String.class).block();
        return message;
    }

    /**
     * Method for updating a training.
     *
     * @param training the training to be updated
     * @param token    the token provided when authenticating
     * @return whether the training was successfully updated
     */
    public String updateTraining(UpdateTrainingRequestModel training, String token) {
        String message = webClient
                .method(HttpMethod.PUT)
                .uri("http://localhost:8083/updateActivity/training")
                .bodyValue(training)
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(String.class).block();
        return message;
    }


    /**
     * Method for updating a competition.
     *
     * @param competition the competition to be updated
     * @param token       the token provided when authenticating
     * @return whether the competition was successfully updated
     */
    public String updateCompetition(UpdateCompetitionRequestModel competition, String token) {
        String message = webClient
                .method(HttpMethod.PUT)
                .uri("http://localhost:8083/updateActivity/competition")
                .bodyValue(competition)
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(String.class).block();
        return message;
    }

    /**
     * Method to accept request.
     *
     * @param userId    the user to accept the request
     * @param requestId the id of the request to be accepted
     * @param token     the token provided when authenticating
     */
    public void acceptRequest(long userId, long requestId, String token) {
        webClient
                .method(HttpMethod.PUT)
                .uri("http://localhost:8086/request/" + userId + "/" + requestId + "/accept")
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Object.class).block();
    }


    /**
     * Method to decline request.
     *
     * @param userId    the user to decline the request
     * @param requestId the id of the request to be declined
     * @param token     the token provided when authenticating
     */
    public void declineRequest(long userId, long requestId, String token) {
        webClient
                .method(HttpMethod.PUT)
                .uri("http://localhost:8086/request/" + userId + "/" + requestId + "/decline")
                .header(AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(Object.class).block();
    }
}
