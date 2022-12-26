package nl.tudelft.sem.template.user.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.user.domain.enums.Position;
import nl.tudelft.sem.template.user.domain.enums.Status;

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

    private Position positions;

}
