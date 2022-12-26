package nl.tudelft.sem.template.activity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import nl.tudelft.sem.template.activity.domain.enums.Gender;
import nl.tudelft.sem.template.activity.domain.enums.Level;
import nl.tudelft.sem.template.activity.domain.enums.Positions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TrainingServiceTest {
    @Autowired
    private transient TrainingService trainingService;
    @Autowired
    private transient CompetitionService competitionService;

    @Autowired
    private transient ActivityRepository activityRepository;

    private List<Activity> activities;

    @BeforeEach
    public void clearDatabase() {
        activities = activityRepository.findAll();
        activityRepository.deleteAll();
    }

    @AfterEach
    public void resetDatabase() {
        activityRepository.deleteAll();
        activityRepository.saveAll(activities);
    }

    @Test
    public void testCreateNewTraining() throws Exception {
        Set<RequiredPositions> s = new HashSet<>();
        s.add(new RequiredPositions(Positions.COX, 1));
        s.add(new RequiredPositions(Positions.PORT_SIDE_ROWER, 2));
        s.add(new RequiredPositions(Positions.PORT_SIDE_ROWER, 3));
        Training newTraining = new Training(1, new Date(12345), LocalTime.of(10, 30),
                LocalTime.of(12, 0), s, BoatType.C4);
        Training savedTraining = trainingService.addNewTraining(newTraining);
        assertThat(newTraining).isEqualTo(savedTraining);
    }

    @Test
    public void testAddSameTraining() throws Exception {
        Set<RequiredPositions> s = new HashSet<>();
        s.add(new RequiredPositions(Positions.COX, 1));
        s.add(new RequiredPositions(Positions.PORT_SIDE_ROWER, 2));
        s.add(new RequiredPositions(Positions.PORT_SIDE_ROWER, 3));
        Training newTraining = new Training(1, new Date(12345), LocalTime.of(10, 30),
                LocalTime.of(12, 0), s, BoatType.C4);
        trainingService.addNewTraining(newTraining);
        ThrowableAssert.ThrowingCallable action = () -> trainingService.addNewTraining(newTraining);
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);
    }

    @Test
    public void testGetAllTrainings() throws Exception {
        Set<RequiredPositions> s1 = new HashSet<>();
        s1.add(new RequiredPositions(Positions.COX, 1));
        s1.add(new RequiredPositions(Positions.PORT_SIDE_ROWER, 2));
        s1.add(new RequiredPositions(Positions.PORT_SIDE_ROWER, 3));
        Training newTraining = new Training(1, new Date(12345), LocalTime.of(10, 30),
                LocalTime.of(12, 0), s1, BoatType.C4);
        trainingService.addNewTraining(newTraining);

        Set<RequiredPositions> s2 = new HashSet<>();
        s2.add(new RequiredPositions(Positions.COACH, 1));
        s2.add(new RequiredPositions(Positions.COX, 2));
        s2.add(new RequiredPositions(Positions.PORT_SIDE_ROWER, 3));
        Competition newCompetition = new Competition(2, new Date(12345), LocalTime.of(10, 30),
                LocalTime.of(12, 0), s2, Level.AMATEUR, Gender.MALE, BoatType.C4, "org");
        competitionService.addNewCompetition(newCompetition);

        List<Activity> trainings = trainingService.getAllTrainings();
        assertThat(trainings.size()).isEqualTo(1);
        assertThat(trainings.get(0)).isEqualTo(newTraining);
    }
}
