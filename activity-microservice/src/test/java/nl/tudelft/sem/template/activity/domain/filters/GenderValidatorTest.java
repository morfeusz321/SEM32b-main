package nl.tudelft.sem.template.activity.domain.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.tudelft.sem.template.activity.domain.Activity;
import nl.tudelft.sem.template.activity.domain.Availability;
import nl.tudelft.sem.template.activity.domain.Competition;
import nl.tudelft.sem.template.activity.domain.RequiredPositions;
import nl.tudelft.sem.template.activity.domain.User;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import nl.tudelft.sem.template.activity.domain.enums.Gender;
import nl.tudelft.sem.template.activity.domain.enums.Level;
import nl.tudelft.sem.template.activity.domain.enums.Positions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GenderValidatorTest {
    @Test
    public void testSameGender() {
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(12, 0)));
        User user = new User(1L, "matei", "matei", "matei", "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Activity activity = new Competition(2, new Date(1234),
                LocalTime.of(10, 30), LocalTime.of(12, 0), positions,
                Level.PROFESSIONAL, Gender.MALE, BoatType.C4, "org");

        Validator validator = new GenderValidator();

        assertTrue(validator.handle(user, activity));
    }

    @Test
    public void testDifferentGender() {
        List<Availability> availabilities = new ArrayList<>();
        availabilities.add(new Availability(1,
                LocalTime.of(10, 30), LocalTime.of(12, 0)));
        User user = new User(1L, "matei", "matei", "matei", "org", BoatType.C4, Gender.MALE,
                Level.PROFESSIONAL, Positions.COX, availabilities);

        Set<RequiredPositions> positions = new HashSet<>();
        positions.add(new RequiredPositions(Positions.COX, 2));
        positions.add(new RequiredPositions(Positions.COACH, 3));
        Activity activity = new Competition(2, new Date(1234),
                LocalTime.of(10, 30), LocalTime.of(12, 0), positions,
                Level.AMATEUR, Gender.FEMALE, BoatType.C4, "org");

        Validator validator = new GenderValidator();

        assertFalse(validator.handle(user, activity));
    }
}
