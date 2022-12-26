package nl.tudelft.sem.template.user.controllers;

import java.util.ArrayList;
import java.util.List;
import javassist.tools.web.BadHttpRequest;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.sem.template.user.domain.Availability;
import nl.tudelft.sem.template.user.domain.AvailabilityRepository;
import nl.tudelft.sem.template.user.domain.Competition;
import nl.tudelft.sem.template.user.domain.Positions;
import nl.tudelft.sem.template.user.domain.Training;
import nl.tudelft.sem.template.user.domain.User;
import nl.tudelft.sem.template.user.domain.UserRepository;
import nl.tudelft.sem.template.user.domain.UserService;
import nl.tudelft.sem.template.user.domain.enums.Position;
import nl.tudelft.sem.template.user.models.CreateAvailability;
import nl.tudelft.sem.template.user.models.CreateCompetitionRequestModel;
import nl.tudelft.sem.template.user.models.CreateTrainingRequestModel;
import nl.tudelft.sem.template.user.models.RequestResponseModel;
import nl.tudelft.sem.template.user.models.UpdateCompetitionRequestModel;
import nl.tudelft.sem.template.user.models.UpdateTrainingRequestModel;
import nl.tudelft.sem.template.user.models.UserRequestModel;
import nl.tudelft.sem.template.user.models.UserResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Hello World example controller.
 * <p>
 * This controller shows how you can extract information from the JWT token.
 * </p>
 */
@RestController
@Slf4j
public class UserController {

    public static final String INCORRECT_FORMAT = "Incorrect format";
    public static final String AUTHORIZATION = "Authorization";
    private final transient UserRepository userRepository;

    private final transient UserService userService;

    private final transient AvailabilityRepository availabilityRepository;

    /**
     * Instantiates a new controller.
     *
     * @param userRepository         repository of users.
     * @param userService            with the logic for the controllers.
     * @param availabilityRepository repository for availability.
     */
    @Autowired
    public UserController(UserRepository userRepository, UserService userService,
                          AvailabilityRepository availabilityRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
    }


    /**
     * Gets example by id.
     *
     * @return the example found in the database with the given id
     */
    @GetMapping("/users")
    public List<UserResponseModel> getUsers() throws Exception {
        try {
            List<User> users = userService.findAll();
            List<UserResponseModel> res = new ArrayList<>();
            for (User u : users) {
                res.add(userService.mapToUserResponseModel(u));
            }
            return res;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INCORRECT_FORMAT);
        }

    }

    /**
     * Endpoint for adding a user.
     *
     * @param requestModel the user request model
     * @return String of request completion information
     * @throws Exception exception
     */
    @PostMapping("/saveUser")
    public ResponseEntity<String> saveUser(@RequestBody UserRequestModel requestModel)
            throws Exception {
        User user;
        try {
            userService.save(requestModel);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INCORRECT_FORMAT);
        }
        return ResponseEntity.ok().body("User successfully added!");
    }

    /**
     * Deletes the user with user id provided in the path.
     *
     * @param id of user who you want to delete.
     * @return response entity.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // Find the user by primary key and delete it
        if (userService.existsById(id)) {
            User user = userRepository.findById(id);
            userRepository.delete(user);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Checks if the user exists in the database.
     *
     * @param id of the user who you want to check.
     * @return boolean if the user exists.
     */
    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> exists(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            } else {
                return new ResponseEntity<Boolean>(false, HttpStatus.OK);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INCORRECT_FORMAT);
        }
    }

    /**
     * Updates availability of the user.
     *
     * @param id                 of the user whose availability you want to change.
     * @param availabilityId     id of availability you want to change.
     * @param createAvailability model of the new availability.
     * @return response whether operation was successful.
     */
    @PostMapping("/updateAvailability/{id}/{availabilityId}")
    public ResponseEntity<String> updateAvailability(@PathVariable Long id, @PathVariable Long availabilityId,
                                                     @RequestBody CreateAvailability createAvailability) {
        if (userRepository.existsById(id)) {
            User user = userRepository.findById(id);
            Availability availability = user.getAvailabilities().stream()
                    .filter(a -> a.getId() == availabilityId).findFirst().get();
            user.getAvailabilities().remove(availability);
            availability.setDayOfWeek(createAvailability.getDayOfWeek());
            availability.setStartTime(createAvailability.getStartTime());
            availability.setEndTime(createAvailability.getEndTime());
            user.addAvailability(availability);
            userRepository.save(user);
            return ResponseEntity.ok().body("availability successfully updated!");
        } else {
            return ResponseEntity.badRequest().body("User does not exist!");
        }
    }

    /**
     * List availabilities of the user.
     *
     * @param id of the user whose availabilities you want to list.
     * @return list of user availabilities.
     */
    @GetMapping("/users/{id}/getAvailabilities")
    public List<Availability> getAvailabilities(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id);
            return (List<Availability>) user.getAvailabilities();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INCORRECT_FORMAT);
        }
    }

    /**
     * Deletes the availability of the user.
     *
     * @param id             of the user whose availability you want to delete.
     * @param availabilityId of the availability that you want to delete.
     * @return response whether it has been deleted.
     */
    @DeleteMapping("/deleteAvailability/{id}/{availabilityId}")
    public ResponseEntity<String> deleteAvailability(@PathVariable Long id, @PathVariable Long availabilityId) {
        if (userRepository.existsById(id)) {
            User user = userRepository.findById(id);
            if (user.getAvailabilities().stream()
                    .filter(a -> a.getId() == availabilityId).findFirst().isPresent()) {
                Availability availability = user.getAvailabilities().stream()
                        .filter(a -> a.getId() == availabilityId).findFirst().get();
                user.getAvailabilities().remove(availability);
                userRepository.save(user);
                return ResponseEntity.ok().body("Availability successfully deleted!");
            } else {
                return ResponseEntity.badRequest().body("Availability does not exist!");
            }
        }
        return ResponseEntity.badRequest().body("User does not exist!");
    }

    /**
     * Creates new availability for the user.
     *
     * @param id                 of the user for who you want to add the availability.
     * @param createAvailability model of the availability.
     * @return created availability.
     */
    @PostMapping("/addAvailability/{id}")
    public ResponseEntity<String> addAvailability(@PathVariable Long id,
                                                  @RequestBody CreateAvailability createAvailability) {
        if (userRepository.existsById(id)) {
            User user = userRepository.findById(id);
            Availability availability = Availability.builder()
                    .dayOfWeek(createAvailability.getDayOfWeek())
                    .startTime(createAvailability.getStartTime())
                    .endTime(createAvailability.getEndTime())
                    .build();
            availability = availabilityRepository.save(availability);
            user.getAvailabilities().add(availability);
            userRepository.save(user);

            return ResponseEntity.ok().body(availability.toString() + " successfully added!");
        } else {
            return ResponseEntity.badRequest().body("User does not exist!");
        }
    }


    /**
     * Endpoint for retrieving all the competitions a user can participate in.
     *
     * @param bearerToken the token provided when authenticating
     * @param position    the position the user wants to fill
     * @return the list of competitions the user can participate in
     * @throws Exception if the user cannot fill the position specified
     */

    @GetMapping("/retrieve/competitions")
    public ResponseEntity<List<Competition>> retrieveCompetitions(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                                  @RequestBody Positions position) throws Exception {
        try {
            User user = userService.findByUsername();
            List<Position> positionList = new ArrayList<>();
            for (Positions p : user.getPositions()) {
                positionList.add(p.getPosition());
            }
            if (!positionList.contains(position.getPosition())) {
                return ResponseEntity.badRequest().build();
            }
            List<Competition> competitions = userService.getCompetitions(user, position.getPosition(), bearerToken);
            return ResponseEntity.ok().body(competitions);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Endpoint for retrieving all the trainings a user can participate in.
     *
     * @param bearerToken the token provided when authenticating
     * @param position    the position the user wants to fill
     * @return the list of trainings the user can participate in
     * @throws Exception if the user cannot fill the position specified
     */

    @GetMapping("/retrieve/trainings")
    public ResponseEntity<List<Training>> retrieveTrainings(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                            @RequestBody Positions position) throws Exception {
        try {
            User user = userService.findByUsername();
            List<Position> positionList = new ArrayList<>();
            for (Positions p : user.getPositions()) {
                positionList.add(p.getPosition());
            }
            if (!positionList.contains(position.getPosition())) {
                return ResponseEntity.badRequest().build();
            }
            List<Training> trainings = userService.getTrainings(user, position.getPosition(), bearerToken);
            return ResponseEntity.ok().body(trainings);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint for sending a request to join an activity.
     *
     * @param bearerToken the token provided when authenticating
     * @param id          the id of the activity the user wants to join
     * @param position    the position the user wants to fill
     * @return whether the request was created or not
     * @throws Exception if for any reason the user cannot join the activity
     */
    @GetMapping("/join/{id}")
    public ResponseEntity<String> joinActivity(@RequestHeader(AUTHORIZATION) String bearerToken,
                                               @PathVariable long id, @RequestBody Positions position)
            throws Exception {
        try {
            User user = userService.findByUsername();
            List<Position> positionList = new ArrayList<>();
            for (Positions p : user.getPositions()) {
                positionList.add(p.getPosition());
            }
            if (!positionList.contains(position.getPosition())) {
                return ResponseEntity.badRequest().body("User cannot fill position");
            }
            userService.joinActivity(user, position, id, bearerToken);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Request was successfully created!");
    }

    /**
     * List pending responses for the user.
     *
     * @param bearerToken token of the requester.
     * @param id          of the user you want to get pending requests for.
     * @return list of pending responses for the user.
     * @throws Exception if the request is not successful.
     */
    @GetMapping("/users/{id}/requests/pending")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestResponseModel> getPendingRequests(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                         @PathVariable long id)
            throws Exception {
        List<RequestResponseModel> requests;
        try {
            requests = userService.getPendingRequests(bearerToken, id);
            return requests;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadHttpRequest();
        }
    }

    /**
     * Endpoint for creating a new training.
     *
     * @param bearerToken          the token provided when authenticating
     * @param trainingRequestModel the training to be added
     * @return whether the training was successfully added
     * @throws Exception if training couldn't be added
     */
    @PutMapping("/create/training")
    public ResponseEntity<String> createTraining(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                 @RequestBody CreateTrainingRequestModel trainingRequestModel)
            throws Exception {
        try {
            User user = userService.findByUsername();
            trainingRequestModel.setOwnerId(user.getId().intValue());
            userService.saveTraining(trainingRequestModel, bearerToken);
            return ResponseEntity.ok().body("Training successfully added!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint for creating a new competition.
     *
     * @param bearerToken             the token provided when authenticating
     * @param competitionRequestModel the competition to be added
     * @return whether the competition was successfully added
     * @throws Exception if training couldn't be added
     */
    @PutMapping("/create/competition")
    public ResponseEntity<String> createTraining(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                 @RequestBody CreateCompetitionRequestModel competitionRequestModel)
            throws Exception {
        try {
            User user = userService.findByUsername();
            competitionRequestModel.setOwnerId(user.getId().intValue());
            userService.saveCompetition(competitionRequestModel, bearerToken);
            return ResponseEntity.ok().body("Competition successfully added!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint for deleting an activity. Only the owner id can do this.
     *
     * @param bearerToken the token provided when authenticating
     * @param id          the id of the activity to be deleted
     * @return whether the activity was successfully deleted
     */
    @DeleteMapping("deleteActivity/{id}")
    public ResponseEntity<String> deleteActivity(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                 @PathVariable int id) {
        try {
            User user = userService.findByUsername();
            String message = userService.deleteActivity(user.getId().intValue(), id, bearerToken);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint for updating a training.
     *
     * @param bearerToken          the token provided when authenticating
     * @param trainingRequestModel the training to be updated
     * @return whether the training was successfully updated
     */
    @PutMapping("/update/training")
    public ResponseEntity<String> updateTraining(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                 @RequestBody UpdateTrainingRequestModel trainingRequestModel) {
        try {
            User user = userService.findByUsername();
            trainingRequestModel.setOwnerId(user.getId().intValue());
            String message = userService.updateTraining(trainingRequestModel, bearerToken);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint for updating a competition.
     *
     * @param bearerToken             the token provided when authenticating
     * @param competitionRequestModel the competition to be updated
     * @return whether the competition was successfully updated
     */
    @PutMapping("/update/competition")
    public ResponseEntity<String> updateCompetition(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                    @RequestBody UpdateCompetitionRequestModel competitionRequestModel) {
        try {
            User user = userService.findByUsername();
            competitionRequestModel.setOwnerId(user.getId().intValue());
            String message = userService.updateCompetition(competitionRequestModel, bearerToken);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    /**
     * Endpoint for accepting a request.
     *
     * @param bearerToken the token provided when authenticating
     * @param userId      the id of the user which accepts the request
     * @param requestId   the id of the request to be accepted
     * @return whether the attempt was successful
     */
    @PutMapping("/request/{userId}/{requestId}/accept")
    public ResponseEntity<String> acceptRequest(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                @PathVariable long userId, @PathVariable long requestId) {
        userService.acceptRequest(userId, requestId, bearerToken);
        return ResponseEntity.ok().body("Request was accepted");
    }


    /**
     * Endpoint for declining a request.
     *
     * @param bearerToken the token provided when authenticating
     * @param userId      the id of the user which declines the request
     * @param requestId   the id of the request to be declined
     * @return whether the attempt was successful
     */
    @PutMapping("/request/{userId}/{requestId}/decline")
    public ResponseEntity<String> declineRequest(@RequestHeader(AUTHORIZATION) String bearerToken,
                                                 @PathVariable long userId, @PathVariable long requestId) {
        userService.declineRequest(userId, requestId, bearerToken);
        return ResponseEntity.ok().body("Request was declined");
    }
}
