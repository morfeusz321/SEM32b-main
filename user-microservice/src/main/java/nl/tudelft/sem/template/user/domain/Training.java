package nl.tudelft.sem.template.user.domain;

import java.time.LocalTime;
import java.util.Date;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.user.domain.enums.BoatType;


@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Training extends Activity {
    /**
     * Constructor method.
     *
     * @param ownerId      The ID of the owner of the activity
     * @param activityDate The date when the activity takes place
     * @param positions    A set of positions that still need to be filled
     * @param boatType     The type of boat in which the current activity takes place.
     */
    public Training(int ownerId, Date activityDate, LocalTime startTime, LocalTime endTime,
                    Set<RequiredPositions> positions, BoatType boatType) {
        super(ownerId, activityDate, startTime, endTime, positions, boatType);
    }

}
