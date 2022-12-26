package nl.tudelft.sem.template.activity.domain.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.Availability;
import nl.tudelft.sem.template.activity.domain.Competition;
import nl.tudelft.sem.template.activity.domain.MyClock;
import nl.tudelft.sem.template.activity.domain.RequiredPositions;
import nl.tudelft.sem.template.activity.domain.Training;
import nl.tudelft.sem.template.activity.domain.User;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import nl.tudelft.sem.template.activity.domain.enums.Gender;
import nl.tudelft.sem.template.activity.domain.enums.Level;
import nl.tudelft.sem.template.activity.domain.enums.Positions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StartTimeValidatorTest {
    MyClock myClock = mock(MyClock.class);

    @Test
    public void testActivityStartTimeInCompetitionRequirementRange() {
        when(myClock.getCurrentTime()).thenReturn(0L);
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(14, 20)));
        availabilities.add(new Availability(3,
                LocalTime.of(12, 30), LocalTime.of(12, 20)));
        final User user = new User(1L, "matei", "matei", "matei", "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Date date = new Date(172800000L);
        Activity activity = new Competition(2, date,
                LocalTime.of(11, 45), LocalTime.of(13, 0), positions,
                Level.PROFESSIONAL, Gender.MALE, BoatType.C4, "org");

        Validator validator = new StartTimeValidator();
        ((StartTimeValidator) validator).setMyClock(myClock);

        assertTrue(validator.handle(user, activity));
    }

    @Test
    public void testActivityStartTimeInTrainingRequirementRange() {
        when(myClock.getCurrentTime()).thenReturn(0L);
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(14, 20)));
        availabilities.add(new Availability(3,
                LocalTime.of(12, 30), LocalTime.of(12, 20)));
        final User user = new User(1L, "matei", "matei", "matei", "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Date date = new Date(3600000L);
        Activity activity = new Training(2, date,
                LocalTime.of(11, 45), LocalTime.of(13, 0),
                positions, BoatType.C4);

        Validator validator = new StartTimeValidator();
        ((StartTimeValidator) validator).setMyClock(myClock);

        assertTrue(validator.handle(user, activity));
    }

    @Test
    public void testActivityStartTimeInCompetitionRequirementRangeLimit() {
        when(myClock.getCurrentTime()).thenReturn(0L);
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(14, 20)));
        availabilities.add(new Availability(3,
                LocalTime.of(12, 30), LocalTime.of(12, 20)));
        final User user = new User(1L, "matei", "matei", "matei", "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Date date = new Date(86400000L - (11 * 60 + 45) * 60 * 1000);
        Activity activity = new Competition(2, date,
                LocalTime.of(11, 45), LocalTime.of(13, 0), positions,
                Level.PROFESSIONAL, Gender.MALE, BoatType.C4, "org");

        Validator validator = new StartTimeValidator();
        ((StartTimeValidator) validator).setMyClock(myClock);

        assertTrue(validator.handle(user, activity));
    }

    @Test
    public void testActivityStartTimeInTrainingRequirementRangeLimit() {
        when(myClock.getCurrentTime()).thenReturn(0L);
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(14, 20)));
        availabilities.add(new Availability(3,
                LocalTime.of(12, 30), LocalTime.of(12, 20)));
        final User user = new User(1L, "matei", "matei", "matei", "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Date date = new Date(1800000L - (11 * 60 + 45) * 60 * 1000);
        Activity activity = new Training(2, date,
                LocalTime.of(11, 45), LocalTime.of(13, 0),
                positions, BoatType.C4);

        Validator validator = new StartTimeValidator();
        ((StartTimeValidator) validator).setMyClock(myClock);

        assertTrue(validator.handle(user, activity));
    }

    @Test
    public void testActivityStartTimeNotInCompetitionRequirementRangeLimit() {
        when(myClock.getCurrentTime()).thenReturn(1L);
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(14, 20)));
        availabilities.add(new Availability(3,
                LocalTime.of(12, 30), LocalTime.of(12, 20)));
        final User user = new User(1L, "matei", "matei", "matei", "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Date date = new Date(86400000L - (11 * 60 + 45) * 60 * 1000);
        Activity activity = new Competition(2, date,
                LocalTime.of(11, 45), LocalTime.of(13, 0), positions,
                Level.PROFESSIONAL, Gender.MALE, BoatType.C4, "org");

        Validator validator = new StartTimeValidator();
        ((StartTimeValidator) validator).setMyClock(myClock);

        assertFalse(validator.handle(user, activity));
    }

    @Test
    public void testActivityStartTimeNotInTrainingRequirementRangeLimit() {
        when(myClock.getCurrentTime()).thenReturn(1L);
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(14, 20)));
        availabilities.add(new Availability(3,
                LocalTime.of(12, 30), LocalTime.of(12, 20)));
        final User user = new User(1L, "matei", "matei", "matei",  "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Date date = new Date(1800000L - (11 * 60 + 45) * 60 * 1000);
        Activity activity = new Training(2, date,
                LocalTime.of(11, 45), LocalTime.of(13, 0),
                positions, BoatType.C4);

        Validator validator = new StartTimeValidator();
        ((StartTimeValidator) validator).setMyClock(myClock);

        assertFalse(validator.handle(user, activity));
    }
}
