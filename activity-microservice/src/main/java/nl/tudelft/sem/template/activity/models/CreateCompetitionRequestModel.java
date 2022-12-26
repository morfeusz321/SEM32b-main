package nl.tudelft.sem.template.activity.models;

import java.time.LocalTime;
import java.util.Date;
import java.util.Set;
import lombok.Data;
import nl.tudelft.sem.template.activity.domain.RequiredPositions;
import nl.tudelft.sem.template.activity.domain.enums.BoatType;
import nl.tudelft.sem.template.activity.domain.enums.Gender;
import nl.tudelft.sem.template.activity.domain.enums.Level;
import org.springframework.format.annotation.DateTimeFormat;


@Data
public class CreateCompetitionRequestModel {
    private int ownerId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private Set<RequiredPositions> requiredPositions;

    private Level allowedLevel;

    private Gender allowedGender;

    private BoatType boatType;

    private LocalTime startTime;

    private LocalTime endTime;

    private String organization;
}
