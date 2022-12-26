package nl.tudelft.sem.template.activity.controllers;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.ActivityRepository;
import nl.tudelft.sem.template.activity.domain.Competition;
import nl.tudelft.sem.template.activity.domain.CompetitionService;
import nl.tudelft.sem.template.activity.domain.RequiredPositions;
import nl.tudelft.sem.template.activity.domain.RequiredPositionsRepository;
import nl.tudelft.sem.template.activity.domain.Training;
import nl.tudelft.sem.template.activity.domain.TrainingService;
import nl.tudelft.sem.template.activity.domain.User;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import nl.tudelft.sem.template.activity.domain.enums.Gender;
import nl.tudelft.sem.template.activity.domain.enums.Level;
import nl.tudelft.sem.template.activity.models.CreateCompetitionRequestModel;
import nl.tudelft.sem.template.activity.models.CreateTrainingRequestModel;
import nl.tudelft.sem.template.activity.models.FillPositionRequestModel;
import nl.tudelft.sem.template.activity.models.UpdateCompetitionRequestModel;
import nl.tudelft.sem.template.activity.models.UpdateTrainingRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@Slf4j
public class ActivityController {
    private final transient ActivityRepository activityRepository;
    private final transient RequiredPositionsRepository positionsRepository;
    private final transient CompetitionService competitionService;
    private final transient TrainingService trainingService;


    /**
     * Constructor for the activity controller.
     *
     * @param activityRepository the activity repository
     * @param competitionService the competition service
     * @param trainingService the training service
     */
    @Autowired
    public ActivityController(ActivityRepository activityRepository,
                              CompetitionService competitionService,
                              TrainingService trainingService,
                              RequiredPositionsRepository positionsRepository) {
        this.activityRepository = activityRepository;
        this.competitionService = competitionService;
        this.trainingService = trainingService;
        this.positionsRepository = positionsRepository;
    }

    /**
     * Endpoint for adding a training.
     *
     * @param requestModel the training request model
     * @return String of request completion information
     * @throws Exception exception
     */
    @PutMapping("/createActivity/training")
    public ResponseEntity<String> createTraining(@RequestBody CreateTrainingRequestModel requestModel)
            throws Exception {

        int ownerId = requestModel.getOwnerId();
        Date date = requestModel.getDate();
        Set<RequiredPositions> positions = requestModel.getRequiredPositions();
        BoatType boatType = requestModel.getBoatType();
        LocalTime startTime = requestModel.getStartTime();
        LocalTime endTime = requestModel.getEndTime();

        if (ownerId != 0 && date != null && !positions.isEmpty()
                && boatType != null && startTime != null && endTime != null) {

            trainingService.addNewTraining(new Training(ownerId, date, startTime, endTime, positions, boatType));
            positions.stream().forEach(pos -> positionsRepository.save(pos));
            return ResponseEntity.ok().body("Training successfully added!");
        }
        return ResponseEntity.badRequest().body("All fields must be introduced");
    }

    /**
     * Endpoint for adding a competition.
     *
     * @param requestModel the competition request model
     * @return String of request completion information
     * @throws Exception exception
     */
    @PutMapping("/createActivity/competition")
    public ResponseEntity<String> createCompetition(@RequestBody CreateCompetitionRequestModel requestModel)
            throws Exception {

        int ownerId = requestModel.getOwnerId();
        Date date = requestModel.getDate();
        Set<RequiredPositions> positions = requestModel.getRequiredPositions();
        Level level = requestModel.getAllowedLevel();
        Gender gender = requestModel.getAllowedGender();
        BoatType boatType = requestModel.getBoatType();
        LocalTime startTime = requestModel.getStartTime();
        LocalTime endTime = requestModel.getEndTime();
        String organization = requestModel.getOrganization();

        if (ownerId != 0 && date != null && !positions.isEmpty()
                && boatType != null && startTime != null && endTime != null
                && level != null && gender != null && organization != null) {
            competitionService.addNewCompetition(
                    new Competition(ownerId, date, startTime, endTime, positions,
                            level, gender, boatType, organization));
            positions.stream().forEach(pos -> positionsRepository.save(pos));
            return ResponseEntity.ok().body("Competition successfully added!");
        }
        return ResponseEntity.badRequest().body("All fields must be introduced");
    }

    /**
     * Endpoint for retrieving an activity by id.
     *
     * @param activityId - the id of the activity
     * @return the activity with the give id, or
     * @throws Exception if activity with given id doesn't exist
     */
    @GetMapping("/activities/{activityId}")
    public ResponseEntity<Activity> retrieveActivityById(@PathVariable Integer activityId) throws Exception {
        if (activityRepository.existsByActivityId(activityId)) {
            Activity activity = activityRepository.findByActivityId(activityId).get();
            return ResponseEntity.ok().body(activity);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }

    /**
     * Endpoint for retrieving all the trainings.
     *
     * @return the trainings currently stored in the database.
     */
    @GetMapping("/retrieveActivities/training")
    public ResponseEntity<List<Activity>> retrieveTrainings() {
        return ResponseEntity.ok().body(trainingService.getAllTrainings());
    }


    /**
     * Endpoint for retrieving all the competitions.
     *
     * @return the competitions currently stored in the database.
     */
    @GetMapping("/retrieveActivities/competition")
    public ResponseEntity<List<Activity>> retrieveCompetitions() {
        return ResponseEntity.ok().body(competitionService.getAllCompetitions());
    }

    /**
     * Endpoint for retrieving a list of all the compatible competitions for a given user.
     *
     * @param user the user for which to retrieve the competitions
     * @return the list of all the compatible competitions in which the given user can participate
     */
    @GetMapping("/retrieveCompatible/competition")
    @ResponseStatus(HttpStatus.OK)
    public List<Activity> retrieveCompatibleCompetitions(@RequestBody User user) {
        List<Activity> competitions = competitionService.getAllCompatibleCompetitions(user);
        return competitions;
    }

    /**
     * Endpoint for filling a position.
     *
     * @param model Request model for filling a position (JSON)
     * @return String of request status
     */
    @PutMapping("/fillPosition")
    public ResponseEntity<String> fillPosition(@RequestBody FillPositionRequestModel model) {
        try {
            int id = model.getActivityId();

            if (!activityRepository.existsByActivityId(id)) {
                return ResponseEntity.badRequest().body("Activity does not exist");
            }

            Activity activity = activityRepository.findByActivityId(id).get();
            Set<RequiredPositions> positions = activity.getPositions();

            for (RequiredPositions pos : positions) {
                if (pos.getPosition().equals(model.getPosition())) {
                    if (pos.fillPosition()) {
                        activityRepository.save(activity);
                        return ResponseEntity.ok("Position successfully filled");
                    }
                    return ResponseEntity.badRequest().body("Position already filled");
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Incorrect format");
        }
        return ResponseEntity.badRequest().body("Position not available in team");
    }

    /**
     * Endpoint for retrieving a list of all the compatible trainings for a given user.
     *
     * @param user the user for which to retrieve the trainings
     * @return the list of all the compatible trainings in which the given user can participate
     */
    @GetMapping("/retrieveCompatible/training")
    @ResponseStatus(HttpStatus.OK)
    public List<Activity> retrieveCompatibleTrainings(@RequestBody User user) {
        return trainingService.getAllCompatibleTrainings(user);
    }

    /**
     * Endpoint for deleting an activity from the repository.
     *
     * @param id the id of the activity
     * @param userId the id of the user
     * @return message regarding the activity removal
     */
    @DeleteMapping("/deleteActivity/{id}/{userId}")
    public ResponseEntity deleteActivity(@PathVariable int id, @PathVariable int userId) {
        if (!activityRepository.existsByActivityId(id)) {
            return ResponseEntity.badRequest().body("Activity not available");
        }
        Activity activity = activityRepository.findByActivityId(id).get();
        if (activity.getOwnerId() != userId) {
            return ResponseEntity.badRequest()
                    .body("User does not have permissions to delete activity!");
        }
        activityRepository.delete(activity);
        return ResponseEntity.ok("Activity with id " + id + " has been deleted");
    }

    /**
     * Endpoint for updating a training.
     *
     * @param model the updated training
     * @return whether the training was updated
     * @throws Exception if training couldn't be updated
     */
    @PutMapping("/updateActivity/training")
    public ResponseEntity updateTraining(@RequestBody UpdateTrainingRequestModel model) throws Exception {

        try {
            if (model.getActivityId() == 0) {
                return ResponseEntity.badRequest().body("Please provide an activity ID");
            }

            int id = model.getActivityId();

            if (!activityRepository.existsByActivityId(id)) {
                return ResponseEntity.badRequest().body("Activity does not exist");
            }
            Activity activity = activityRepository.findByActivityId(id).get();
            if (activity instanceof Competition) {
                return ResponseEntity.badRequest()
                        .body("Id provided for competition, please use 'updateActivity/competition'");
            }

            if (activity.getOwnerId() != model.getOwnerId()) {
                return ResponseEntity.badRequest()
                        .body("User does not have permissions to edit activity!");
            }

            int ownerId = model.getOwnerId();
            Date date = model.getDate();
            Set<RequiredPositions> positions = model.getRequiredPositions();
            BoatType boatType = model.getBoatType();
            LocalTime startTime = model.getStartTime();
            LocalTime endTime = model.getEndTime();

            Set<RequiredPositions> currentPositions = new HashSet<>();

            activity.getPositions().stream().forEach(pos -> {
                currentPositions.add(pos);
            });

            currentPositions.stream().forEach(pos -> {
                activity.removePosition(pos);
            });

            positions.stream().forEach(pos -> positionsRepository.save(pos));

            Training source = new Training(ownerId, date, startTime, endTime, positions, boatType);

            Training target = (Training) activity;

            if (source.getOwnerId() != 0) {
                target.setOwnerId(source.getOwnerId());
            }
            if (source.getActivityDate() != null) {
                target.setActivityDate(source.getActivityDate());
            }
            if (source.getStartTime() != null) {
                target.setStartTime(source.getStartTime());
            }
            if (source.getEndTime() != null) {
                target.setEndTime(source.getEndTime());
            }
            if (source.getPositions() != null) {
                positions.stream().forEach(pos -> {
                    positionsRepository.save(pos);
                    activity.addPosition(pos);
                });
            }
            if (source.getBoatType() != null) {
                target.setBoatType(source.getBoatType());
            }
            activityRepository.save(target);
            return ResponseEntity.ok("Training successfully updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Incorrect format");
        }
    }


    /**
     * Endpoint for updating a competition.
     *
     * @param model the updated competition
     * @return whether the competition was updated
     * @throws Exception if competition couldn't be updated
     */
    @PutMapping("/updateActivity/competition")
    public ResponseEntity updateCompetition(@RequestBody UpdateCompetitionRequestModel model) {
        try {
            if (model.getActivityId() == 0) {
                return ResponseEntity.badRequest().body("Please provide an activity ID");
            }

            int id = model.getActivityId();

            if (!activityRepository.existsByActivityId(id)) {
                return ResponseEntity.badRequest().body("Activity does not exist");
            }
            Activity activity = activityRepository.findByActivityId(id).get();
            if (activity instanceof Training) {
                return ResponseEntity.badRequest()
                        .body("Id provided for training, please use 'updateActivity/training'");
            }

            if (activity.getOwnerId() != model.getOwnerId()) {
                return ResponseEntity.badRequest()
                        .body("User does not have permissions to edit activity!");
            }

            int ownerId = model.getOwnerId();
            Date date = model.getDate();
            Set<RequiredPositions> positions = model.getRequiredPositions();
            BoatType boatType = model.getBoatType();
            LocalTime startTime = model.getStartTime();
            LocalTime endTime = model.getEndTime();
            Level level = model.getLevel();
            Gender gender = model.getGender();
            String organization = model.getOrganization();

            Set<RequiredPositions> currentPositions = new HashSet<>();

            activity.getPositions().stream().forEach(pos -> {
                currentPositions.add(pos);
            });

            currentPositions.stream().forEach(pos -> {
                activity.removePosition(pos);
            });

            positions.stream().forEach(pos -> positionsRepository.save(pos));

            Competition source = new Competition(ownerId, date, startTime, endTime, positions,
                    level, gender, boatType, organization);

            Competition target = (Competition) activity;

            if (source.getOwnerId() != 0) {
                target.setOwnerId(source.getOwnerId());
            }
            if (source.getActivityDate() != null) {
                target.setActivityDate(source.getActivityDate());
            }
            if (source.getStartTime() != null) {
                target.setStartTime(source.getStartTime());
            }
            if (source.getEndTime() != null) {
                target.setEndTime(source.getEndTime());
            }
            if (source.getPositions() != null) {
                positions.stream().forEach(pos -> {
                    positionsRepository.save(pos);
                    activity.addPosition(pos);
                });
            }
            if (source.getBoatType() != null) {
                target.setBoatType(source.getBoatType());
            }
            if (source.getAllowedGender() != null) {
                target.setAllowedGender(gender);
            }
            if (source.getAllowedLevel() != null) {
                target.setAllowedLevel(level);
            }
            if (source.getOrganization() != null) {
                target.setOrganization(organization);
            }
            activityRepository.save(target);
            return ResponseEntity.ok("Competition successfully updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Incorrect format");
        }
    }
}
