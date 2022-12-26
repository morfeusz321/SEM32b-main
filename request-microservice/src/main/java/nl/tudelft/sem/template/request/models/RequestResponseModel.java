package nl.tudelft.sem.template.request.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.request.domain.Positions;
import nl.tudelft.sem.template.request.domain.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestResponseModel {
    private Long id;
    private Long ownerId;

    private Long activityId;

    private Long requesterId;
    private Status status;

    private Positions positions;

}
