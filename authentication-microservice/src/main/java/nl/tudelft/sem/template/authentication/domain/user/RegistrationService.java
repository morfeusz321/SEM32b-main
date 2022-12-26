package nl.tudelft.sem.template.authentication.domain.user;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A DDD service for registering a new user.
 */
@Service
public class RegistrationService {
    private final transient UserRepository userRepository;
    private final transient RoleRepository roleRepository;
    private final transient PasswordHashingService passwordHashingService;

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository  the user repository
     * @param passwordHashingService the password encoder
     * @param roleRepository  the role repository
     */
    public RegistrationService(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordHashingService passwordHashingService) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.roleRepository = roleRepository;
    }

    /**
     * Register a new user.
     *
     * @param memberId    The MemberID of the user
     * @param password The password of the user
     * @throws Exception if the user already exists
     */
    public AppUser registerUser(MemberId memberId, Password password) throws Exception {

        if (checkMemberIdIsUnique(memberId)) {
            // Hash password
            HashedPassword hashedPassword = passwordHashingService.hash(password);

            // Create new account
            AppUser user = new AppUser(memberId, hashedPassword);
            userRepository.save(user);

            return user;
        }

        throw new MemberIdAlreadyInUseException(memberId);
    }

    /**
     * Change the password of a user.
     *
     * @param memberId The MemberID of the user
     * @param newPassword The new password of the user
     * @throws Exception if the new password is the same as the old one
     */
    public void changeUserPassword(MemberId memberId, Password oldPassword, Password newPassword) throws Exception {

        if (userRepository.existsByMemberId(memberId)) {
            AppUser user = userRepository.findByMemberId(memberId).get();
            if (passwordHashingService.match(user.getPassword(), oldPassword)) {
                HashedPassword hashedNewPassword = passwordHashingService.hash(newPassword);
                user.changePassword(hashedNewPassword);
                deleteUser(memberId);
                userRepository.save(user);
                return;
            }
        }
        throw new Exception("Bad request: user credentials invalid");
    }

    /**
     * Delete a user from the database.
     *
     * @param memberId The MemberID of the user
     * @throws Exception if the user doesn't exist
     */
    public void deleteUser(MemberId memberId) throws Exception {

        if (userRepository.existsByMemberId(memberId)) {
            userRepository.delete(userRepository.findByMemberId(memberId).get());
        } else {
            throw new Exception("Tried to delete AppUser but the MemberId does not exist in the database");
        }
    }

    /**
     * Register a new role.
     *
     * @throws Exception if the role already exists
     */
    public Role registerRole(String name) throws Exception {

        if (checkNameIsUnique(name)) {

            // Create new role
            Role role = new Role(name);
            roleRepository.save(role);

            return role;
        }

        throw new RoleNameAlreadyExistsException(name);
    }

    /**
     * Add a role to the users role list.
     *
     */
    @Transactional
    public void addRoleToAppUser(MemberId memberId, String roleName) {
        if (userRepository.existsByMemberId(memberId) && roleRepository.existsByName(roleName)) {
            AppUser user = userRepository.findByMemberId(memberId).get();
            Role role = roleRepository.findByName(roleName).get();
            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
            }
        }
    }

    /**
     * list all roles of a specific user.
     *
     */
    public Collection<Role> listUserRoles(MemberId memberId) {
        if (userRepository.existsByMemberId(memberId)) {
            return userRepository.findByMemberId(memberId).get().getRoles();
        }
        return new ArrayList<>();
    }

    /**
     * list all usernames.
     *
     */
    public String listUsers() {
        String out = "";
        for (AppUser x : userRepository.findAll()) {
            out = out + x.getMemberId().toString();
            for (Role role : x.getRoles()) {
                out = out + " " + role.getRole();
            }
            out = out + "\n";
        }
        return out;
    }

    /**
     * list all roles.
     *
     */
    public String listRoles() {
        String out = "";
        for (Role x : roleRepository.findAll()) {
            out = out + x.getRole() + "\n";
        }
        return out;
    }

    public boolean checkMemberIdIsUnique(MemberId memberId) {
        return !userRepository.existsByMemberId(memberId);
    }

    public boolean checkNameIsUnique(String name) {
        return !roleRepository.existsByName(name);
    }
}
