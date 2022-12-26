package nl.tudelft.sem.template.user.models;

import java.time.LocalTime;
import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.user.domain.RequiredPositions;
import nl.tudelft.sem.template.user.domain.enums.BoatType;
import org.springframework.format.annotation.DateTimeFormat;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainingRequestModel {
    private int activityId;

    private int ownerId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private Set<RequiredPositions> requiredPositions;

    private BoatType boatType;

    private LocalTime startTime;

    private LocalTime endTime;
}
