package nl.tudelft.sem.template.user.models;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAvailability {

    private int dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;
}
