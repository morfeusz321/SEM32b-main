package nl.tudelft.sem.template.activity.domain;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.sem.template.activity.domain.filters.AvailabilityValidator;
import nl.tudelft.sem.template.activity.domain.filters.CertificateValidator;
import nl.tudelft.sem.template.activity.domain.filters.ExperienceValidator;
import nl.tudelft.sem.template.activity.domain.filters.GenderValidator;
import nl.tudelft.sem.template.activity.domain.filters.OrganizationValidator;
import nl.tudelft.sem.template.activity.domain.filters.PositionValidator;
import nl.tudelft.sem.template.activity.domain.filters.StartTimeValidator;
import nl.tudelft.sem.template.activity.domain.filters.Validator;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CompetitionService {
    // The repository containing all the activities
    private final transient ActivityRepository activityRepository;
    // The validator which checks if a user matches all the requirements of a competition.
    private final transient Validator handler;

    /**
     * Constructor method.
     *
     * @param activityRepository The repository containing all the activities
     */
    public CompetitionService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
        this.handler = setupValidator();
    }

    /**
     * Sets up all the validators in the chain of responsibility for the competition checks.
     *
     * @return a validator with all the connections created
     */
    public static Validator setupValidator() {
        Validator certificateValidator = new CertificateValidator();
        Validator genderValidator = new GenderValidator();
        certificateValidator.setNext(genderValidator);

        Validator organizationValidator = new OrganizationValidator();
        genderValidator.setNext(organizationValidator);

        Validator experienceValidator = new ExperienceValidator();
        organizationValidator.setNext(experienceValidator);

        Validator availabilityValidator = new AvailabilityValidator();
        experienceValidator.setNext(availabilityValidator);

        Validator startTimeValidator = new StartTimeValidator();
        ((StartTimeValidator) startTimeValidator).setMyClock(new MyClock());
        availabilityValidator.setNext(startTimeValidator);

        Validator positionValidator = new PositionValidator();
        startTimeValidator.setNext(positionValidator);

        return certificateValidator;
    }

    /**
     * Adds a new competition to the database; throws exception if activity with id already exists.
     *
     * @param newCompetition the competition to be added to the database
     * @return the added element
     * @throws Exception if an activity with the same id already exists in the database
     */
    public Competition addNewCompetition(Competition newCompetition) throws Exception {
        if (!activityRepository.existsByActivityId(newCompetition.getActivityId())) {
            return activityRepository.save(newCompetition);
        }
        throw new Exception();
    }

    /**
     * Get all activities from database and filter only competitions.
     *
     * @return all the competitions stored in the database
     */
    public List<Activity> getAllCompetitions() {
        return activityRepository.findAll().stream()
                        .filter(a -> a instanceof Competition).collect(Collectors.toList());
    }

    /**
     * Get all the compatible competitions for a given user.
     *
     * @param user the user for which we have to retrieve the competitions.
     * @return all the compatible competitions for a given user in the database.
     */
    public List<Activity> getAllCompatibleCompetitions(User user) {
        log.info("Trying to find all the competitions for user {}", user.getId());
        List<Activity> activities = activityRepository.findAll()
                .stream()
                .filter(a -> a instanceof Competition && handler.handle(user, a))
                .collect(Collectors.toList());
        return activities;
    }
}
