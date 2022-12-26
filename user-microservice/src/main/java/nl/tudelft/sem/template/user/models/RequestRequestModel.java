package nl.tudelft.sem.template.user.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.tudelft.sem.template.user.domain.enums.Position;
import nl.tudelft.sem.template.user.domain.enums.Status;

@Data
@AllArgsConstructor
public class RequestRequestModel {
    private Long ownerId;

    private Long activityId;

    private Long requesterId;

    private Status status;

    private Position positions;
}
