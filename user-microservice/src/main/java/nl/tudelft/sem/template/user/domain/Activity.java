package nl.tudelft.sem.template.user.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalTime;
import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.user.domain.enums.BoatType;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public abstract class Activity {

    // ID of the activity
    private int activityId;

    // The ID of the owner of the activity
    private int ownerId;

    // The date when the activity takes place
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date activityDate;

    // The hour when the activity starts.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    // The hour when the activity ends.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    // A set of positions that still need to be filled
    @JsonIgnore
    private Set<RequiredPositions> positions;

    // The type of boat in which the current activity takes place.
    private BoatType boatType;

    /**
     * Constructor method.
     *
     * @param ownerId      The ID of the owner of the activity
     * @param activityDate The date when the activity takes place
     * @param positions    A set of positions that still need to be filled
     * @param boatType     The type of boat in which the current activity takes place.
     */
    public Activity(int ownerId, Date activityDate, LocalTime startTime, LocalTime endTime,
                    Set<RequiredPositions> positions, BoatType boatType) {
        this.ownerId = ownerId;
        this.activityDate = activityDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.positions = positions;
        this.boatType = boatType;
    }
}
