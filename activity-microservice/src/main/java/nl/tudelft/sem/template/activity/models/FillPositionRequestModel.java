package nl.tudelft.sem.template.activity.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.activity.domain.enums.Positions;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FillPositionRequestModel {

    private Positions position;
    private int activityId;
}
