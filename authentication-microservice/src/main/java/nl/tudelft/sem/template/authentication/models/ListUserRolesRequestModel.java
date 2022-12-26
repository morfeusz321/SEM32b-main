package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

/**
 * Model representing an authentication request.
 */
@Data
public class ListUserRolesRequestModel {
    private String memberId;
}