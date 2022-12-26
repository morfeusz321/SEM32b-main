package nl.tudelft.sem.template.authentication.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.MemberId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.domain.user.Role;
import nl.tudelft.sem.template.authentication.models.AddRoleToUserRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.ChangeUserPasswordRequestModel;
import nl.tudelft.sem.template.authentication.models.ListUserRolesRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModelTest;
import nl.tudelft.sem.template.authentication.models.RoleRegistrationRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("authentication")
public class AuthenticationController {

    private final transient AuthenticationManager authenticationManager;

    private final transient JwtTokenGenerator jwtTokenGenerator;

    private final transient JwtUserDetailsService jwtUserDetailsService;

    private final transient RegistrationService registrationService;

    private final transient RestTemplate restTemplate;


    /**
     * Instantiates a new UsersController.
     *
     * @param authenticationManager the authentication manager
     * @param jwtTokenGenerator     the token generator
     * @param jwtUserDetailsService the user service
     * @param registrationService   the registration service
     */
    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenGenerator jwtTokenGenerator,
                                    JwtUserDetailsService jwtUserDetailsService,
                                    RegistrationService registrationService,
                                    RestTemplate restTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.registrationService = registrationService;
        this.restTemplate = restTemplate;
    }

    /**
     * Endpoint for authentication.
     *
     * @param request The login model
     * @return JWT token if the login is successful
     * @throws Exception if the user does not exist or the password is incorrect
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseModel> authenticate(@RequestBody AuthenticationRequestModel request)
            throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getMemberId(),
                            request.getPassword()));
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e);
        }

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(request.getMemberId());
        final String jwtToken = jwtTokenGenerator.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponseModel(jwtToken));
    }

    /**
     * Endpoint for registration.
     *
     * @param request The registration model
     * @return 200 OK if the registration is successful
     * @throws Exception if a user with this memberId already exists
     */
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegistrationRequestModel request) throws Exception {
        MemberId memberId;
        Password password;
        try {
            memberId = new MemberId(request.getMemberId());
            password = new Password(request.getPassword());
            registrationService.registerUser(memberId, password);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // Send http request to user service in order to save the userdata
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(request.getMemberId());
        final String jwtToken = jwtTokenGenerator.generateToken(userDetails);
        headers.setBearerAuth(jwtToken);

        // map for post parameters
        Map<String, Object> map = new HashMap<>();
        map.put("positions", request.getPositions());
        map.put("username", request.getMemberId());
        map.put("gender", request.getGender());
        map.put("firstName", request.getFirstName());
        map.put("lastName", request.getLastName());
        map.put("level", request.getLevel());
        map.put("organisation", request.getOrganization());
        map.put("certificate", request.getCertificate());
        map.put("availabilities", request.getAvailabilities());

        String url = "http://localhost:8082/saveUser";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!(response.getStatusCode() == HttpStatus.OK)) {
            try {
                registrationService.deleteUser(memberId);
                return response;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for registration testing without accessing other services.
     *
     * @param request The registration model
     * @return 200 OK if the registration is successful
     * @throws Exception if a user with this memberId already exists
     */
    @PostMapping("/registerTest")
    public ResponseEntity registerWithoutUserInfo(@RequestBody RegistrationRequestModelTest request) throws Exception {

        MemberId memberId;
        Password password;
        try {
            memberId = new MemberId(request.getMemberId());
            password = new Password(request.getPassword());
            registrationService.registerUser(memberId, password);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for registering roles.
     *
     * @param request The registration model
     * @return 200 OK if the registration is successful
     * @throws Exception if a role with this name already exists
     */
    @PostMapping("/registerRole")
    public ResponseEntity registerRole(@RequestBody RoleRegistrationRequestModel request) throws Exception {

        try {
            String name = request.getName();
            registrationService.registerRole(name);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }


    /**
     * Endpoint for adding roles to users.
     *
     * @param request The registration model
     * @return 200 OK if the registration is successful
     * @throws Exception if the provided role or user does not exist
     */
    @PostMapping("/addRoleToUser")
    public ResponseEntity addRoleToUser(@RequestBody AddRoleToUserRequestModel request) throws Exception {

        try {
            MemberId memberId = new MemberId(request.getMemberId());
            String roleName = request.getRoleName();
            registrationService.addRoleToAppUser(memberId, roleName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for changing password of users.
     *
     * @param request The password change model
     * @return 200 OK if the operation was successful
     * @throws Exception if the provided oldPassword does not match or user does not exist
     */
    @PostMapping("/changeUserPassword")
    public ResponseEntity changeUserPassword(@RequestBody ChangeUserPasswordRequestModel request) throws Exception {

        try {
            MemberId memberId = new MemberId(request.getMemberId());
            Password oldPassword = new Password(request.getOldPassword());
            Password newPassword = new Password(request.getNewPassword());
            registrationService.changeUserPassword(memberId, oldPassword, newPassword);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for viewing all usernames.
     *
     * @return String representation of all usernames
     * @throws Exception if operation fails
     */
    @GetMapping("/listUsers")
    public String listUsers() throws Exception {
        try {
            return registrationService.listUsers();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint for viewing a specific user's roles.
     *
     * @return String representation of all roles of a user
     * @throws Exception if operation fails
     */
    @GetMapping("/listUserRoles")
    public Collection<Role> listUserRoles(@RequestBody ListUserRolesRequestModel request) throws Exception {
        try {
            MemberId memberId = new MemberId(request.getMemberId());
            return registrationService.listUserRoles(memberId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Endpoint for viewing all roles.
     *
     * @return String representation of all role names
     * @throws Exception if operation fails
     */
    @GetMapping("/listRoles")
    public String listRoles() throws Exception {
        try {
            return registrationService.listRoles();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
