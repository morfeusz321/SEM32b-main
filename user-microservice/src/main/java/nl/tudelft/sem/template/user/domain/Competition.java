package nl.tudelft.sem.template.user.domain;

import java.time.LocalTime;
import java.util.Date;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.user.domain.enums.BoatType;
import nl.tudelft.sem.template.user.domain.enums.Gender;
import nl.tudelft.sem.template.user.domain.enums.Level;


@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Competition extends Activity {
    // the minimum required level to participate in the competition
    private Level allowedLevel;

    // the gender required for the people in the boat
    private Gender allowedGender;

    // The organization from which the user has to be in order to participate
    // It is the same as the organization of the owner
    private String organization;

    /**
     * Constructor method.
     *
     * @param ownerId       The ID of the owner of the activity
     * @param activityDate  The date when the activity takes place
     * @param positions     A set of positions that still need to be filled
     * @param allowedLevel  The minimum required level to participate in the competition
     * @param allowedGender The gender required for the people in the boat
     * @param boatType      The type of boat in which the current activity takes place.
     */
    public Competition(int ownerId, Date activityDate, LocalTime startTime, LocalTime endTime,
                       Set<RequiredPositions> positions, Level allowedLevel, Gender allowedGender,
                       BoatType boatType, String organization) {
        super(ownerId, activityDate, startTime, endTime, positions, boatType);
        this.allowedLevel = allowedLevel;
        this.allowedGender = allowedGender;
        this.organization = organization;
    }
}
