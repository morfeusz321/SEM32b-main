package nl.tudelft.sem.template.authentication.domain;

import java.time.LocalTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Availability {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private int dayOfWeek;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

}
