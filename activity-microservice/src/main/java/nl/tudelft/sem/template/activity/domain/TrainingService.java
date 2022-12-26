package nl.tudelft.sem.template.activity.domain;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.activity.domain.filters.AvailabilityValidator;
import nl.tudelft.sem.template.activity.domain.filters.CertificateValidator;
import nl.tudelft.sem.template.activity.domain.filters.PositionValidator;
import nl.tudelft.sem.template.activity.domain.filters.StartTimeValidator;
import nl.tudelft.sem.template.activity.domain.filters.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingService {
    // The repository containing all the activities
    private final transient ActivityRepository activityRepository;

    // The validator which checks if a user matches all the requirements of a training.
    private final transient Validator handler;

    /**
     * Constructor method.
     *
     * @param activityRepository The repository containing all the activities
     */
    public TrainingService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
        this.handler = setupValidator();
    }

    /**
     * Sets up all the validators in the chain of responsibility for the training checks.
     *
     * @return a validator with all the connections created
     */
    public static Validator setupValidator() {
        Validator certificateValidator = new CertificateValidator();
        Validator availabilityValidator = new AvailabilityValidator();
        Validator startTimeValidator = new StartTimeValidator();
        ((StartTimeValidator) startTimeValidator).setMyClock(new MyClock());

        certificateValidator.setNext(availabilityValidator);
        availabilityValidator.setNext(startTimeValidator);

        Validator positionValidator = new PositionValidator();
        startTimeValidator.setNext(positionValidator);

        return certificateValidator;
    }

    /**
     * Adds a new training to the database; throws exception if activity with id already exists.
     *
     * @param newTraining the training to be added to the database
     * @return the added element
     * @throws Exception if an activity with the same id already exists in the database
     */
    public Training addNewTraining(Training newTraining) throws Exception {
        if (!activityRepository.existsByActivityId(newTraining.getActivityId())) {
            return activityRepository.save(newTraining);
        }
        throw new Exception();
    }

    /**
     * Get all activities from database and filter only trainings.
     *
     * @return all the trainings stored in the database
     */
    public List<Activity> getAllTrainings() {
        return activityRepository
                .findAll().stream().filter(a -> a instanceof Training).collect(Collectors.toList());
    }

    /**
     * Get all the compatible trainings for a given user.
     *
     * @param user the user for which we have to retrieve the trainings.
     * @return all the compatible trainings for a given user in the database.
     */
    public List<Activity> getAllCompatibleTrainings(User user) {
        return activityRepository.findAll()
                .stream()
                .filter(a -> a instanceof Training && handler.handle(user, a))
                .collect(Collectors.toList());
    }
}
