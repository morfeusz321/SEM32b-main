package nl.tudelft.sem.template.activity.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public abstract class Activity {

    // ID of the activity
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private int activityId;

    // The ID of the owner of the activity
    @Column
    private int ownerId;

    // The date when the activity takes place
    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date activityDate;

    // The hour when the activity starts.
    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    // The hour when the activity ends.
    @Column
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    // A set of positions that still need to be filled
    @OneToMany(cascade = CascadeType.ALL,
                orphanRemoval = true)
    @OrderColumn
    private Set<RequiredPositions> positions;

    // The type of boat in which the current activity takes place.
    @Column
    private BoatType boatType;

    /**
     * Constructor method.
     *
     * @param ownerId The ID of the owner of the activity
     * @param activityDate The date when the activity takes place
     * @param positions A set of positions that still need to be filled
     * @param boatType The type of boat in which the current activity takes place.
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

    public void removePosition(RequiredPositions position) {
        positions.remove(position);
    }

    public void addPosition(RequiredPositions position) {
        positions.add(position);
    }

}
