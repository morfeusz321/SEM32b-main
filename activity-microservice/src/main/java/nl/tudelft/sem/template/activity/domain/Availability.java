package nl.tudelft.sem.template.activity.domain;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Availability {
    int dayOfWeek;
    LocalTime startTime;
    LocalTime endTime;
}
