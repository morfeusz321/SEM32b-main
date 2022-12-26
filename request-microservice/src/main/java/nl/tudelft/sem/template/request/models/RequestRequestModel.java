package nl.tudelft.sem.template.request.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.tudelft.sem.template.request.domain.Positions;
import nl.tudelft.sem.template.request.domain.Status;

@Data
@AllArgsConstructor
public class RequestRequestModel {
    private Long ownerId;

    private Long activityId;

    private Long requesterId;

    private Status status;

    private Positions positions;
}
