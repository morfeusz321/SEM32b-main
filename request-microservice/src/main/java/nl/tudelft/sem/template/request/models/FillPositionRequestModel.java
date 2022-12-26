package nl.tudelft.sem.template.request.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.request.domain.Positions;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FillPositionRequestModel {

    private Positions position;
    private Long activityId;
}
