package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

/**
 * Model representing a add role to user request.
 */
@Data
public class AddRoleToUserRequestModel {
    private String memberId;
    private String roleName;
}