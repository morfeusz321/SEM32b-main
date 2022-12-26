package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RegistrationServiceTests {

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient RoleRepository roleRepository;

    @Test
    public void createUser_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final MemberId testUser2 = new MemberId("SomeOtherUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        // Act
        registrationService.registerUser(testUser, testPassword);
        registrationService.registerUser(testUser2, testPassword);

        // Assert
        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);

        AppUser savedUser2 = userRepository.findByMemberId(testUser2).orElseThrow();

        assertThat(savedUser2.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser2.getPassword()).isEqualTo(testHashedPassword);
        assertThat(savedUser).isNotEqualTo(savedUser2);
    }

    @Test
    public void createUser_withExistingUser_throwsException() {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final HashedPassword existingTestPassword = new HashedPassword("password123");
        final Password newTestPassword = new Password("password456");

        AppUser existingAppUser = new AppUser(testUser, existingTestPassword);
        userRepository.save(existingAppUser);

        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword);

        // Assert
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);

        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();

        assertThat(savedUser.getMemberId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
    }

    @Test
    public void deleteUser_withExistingUser() {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        final HashedPassword testPassword = new HashedPassword("password123");

        AppUser existingAppUser = new AppUser(testUser, testPassword);
        userRepository.save(existingAppUser);
        assertThat(userRepository.existsByMemberId(testUser)).isTrue();

        // Act
        ThrowingCallable action = () -> registrationService.deleteUser(testUser);

        // Assert
        assertThatNoException().isThrownBy(action);
        assertThat(userRepository.existsByMemberId(testUser)).isFalse();

        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);
        assertThat(userRepository.existsByMemberId(testUser)).isFalse();
    }

    @Test
    public void deleteUser_withNonExistingUser_throwsException() {
        // Arrange
        final MemberId testUser = new MemberId("SomeUser");
        assertThat(userRepository.existsByMemberId(testUser)).isFalse();

        // Act
        ThrowingCallable action = () -> registrationService.deleteUser(testUser);

        // Assert
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);
        assertThat(userRepository.existsByMemberId(testUser)).isFalse();
    }

    @Test
    public void registerRole_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";

        // Act
        registrationService.registerRole(testRole);
        registrationService.registerRole(testRole2);

        // Assert
        Role savedRole = roleRepository.findByName(testRole).orElseThrow();

        assertThat(savedRole.getRole()).isEqualTo(testRole);

        Role savedRole2 = roleRepository.findByName(testRole2).orElseThrow();

        assertThat(savedRole2.getRole()).isEqualTo(testRole2);
        assertThat(savedRole).isNotEqualTo(savedRole2);
    }

    @Test
    public void registerRole_withExistingRole_throwsException() {
        // Arrange
        final String testRole = "SomeRole";

        // Act
        ThrowingCallable action = () -> registrationService.registerRole(testRole);

        // Assert
        assertThatNoException()
                .isThrownBy(action);
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);

        Role savedRole = roleRepository.findByName(testRole).orElseThrow();

        assertThat(savedRole.getRole()).isEqualTo(testRole);
    }

    @Test
    public void addRoleToAppUser_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";
        final String testNonExistentRole = "SomeNonExistentRole";
        final MemberId testUser = new MemberId("SomeUser");
        final MemberId testUser2 = new MemberId("SomeOtherUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        // Act
        registrationService.registerUser(testUser, testPassword);
        registrationService.registerUser(testUser2, testPassword);
        registrationService.registerRole(testRole);
        registrationService.registerRole(testRole2);

        // Assert
        Collection<Role> roleList;

        // test adding role to one user
        registrationService.addRoleToAppUser(testUser, testRole);
        roleList = userRepository.findByMemberId(testUser).orElseThrow().getRoles();
        assertThat(roleList).hasSize(1);
        assertThat(roleList.iterator().next().getRole()).isEqualTo(testRole);

        // test adding role to other user
        registrationService.addRoleToAppUser(testUser2, testRole);
        roleList = userRepository.findByMemberId(testUser2).orElseThrow().getRoles();
        assertThat(roleList).hasSize(1);
        assertThat(roleList.iterator().next().getRole()).isEqualTo(testRole);

        // test not adding duplicate roles to user
        registrationService.addRoleToAppUser(testUser, testRole);
        roleList = userRepository.findByMemberId(testUser).orElseThrow().getRoles();
        assertThat(roleList).hasSize(1);
        assertThat(roleList.iterator().next().getRole()).isEqualTo(testRole);

        // test not adding fake roles to user
        registrationService.addRoleToAppUser(testUser, testNonExistentRole);
        roleList = userRepository.findByMemberId(testUser).orElseThrow().getRoles();
        assertThat(roleList).hasSize(1);
        assertThat(roleList.iterator().next().getRole()).isEqualTo(testRole);

        // test adding two roles to one user
        registrationService.addRoleToAppUser(testUser, testRole2);
        roleList = userRepository.findByMemberId(testUser).orElseThrow().getRoles();
        Iterator<Role> role = roleList.iterator();
        assertThat(roleList).hasSize(2);
        assertThat(role.next().getRole()).isEqualTo(testRole);
        assertThat(role.next().getRole()).isEqualTo(testRole2);
    }

    @Test
    public void listRoles_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";

        // Act
        registrationService.registerRole(testRole);
        registrationService.registerRole(testRole2);

        // Assert
        assertThat(registrationService.listRoles()).contains("SomeRole", "SomeOtherRole");
    }

    @Test
    public void listUserRoles_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";
        final MemberId testUser = new MemberId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        // Act
        registrationService.registerUser(testUser, testPassword);
        registrationService.registerRole(testRole);
        registrationService.registerRole(testRole2);

        // Assert
        registrationService.addRoleToAppUser(testUser, testRole);
        registrationService.addRoleToAppUser(testUser, testRole2);
        Iterator<Role> roles = registrationService.listUserRoles(testUser).iterator();
        assertThat(registrationService.listUserRoles(testUser)).hasSize(2);
        assertThat(roles.next().getRole()).isEqualTo(testRole);
        assertThat(roles.next().getRole()).isEqualTo(testRole2);
    }

    @Test
    public void listUsers_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final String testRole2 = "SomeOtherRole";
        final MemberId testUser = new MemberId("SomeUser");
        final MemberId testUser2 = new MemberId("SomeOtherUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        // Act
        registrationService.registerUser(testUser, testPassword);
        registrationService.registerUser(testUser2, testPassword);
        registrationService.registerRole(testRole);
        registrationService.registerRole(testRole2);

        // Assert
        registrationService.addRoleToAppUser(testUser, testRole);
        registrationService.addRoleToAppUser(testUser, testRole2);
        registrationService.addRoleToAppUser(testUser2, testRole2);
        assertThat(registrationService.listUsers())
                .contains("SomeUser SomeRole SomeOtherRole", "SomeOtherUser SomeOtherRole");
    }

    @Test
    public void changeUserPassword_worksCorrectly() throws Exception {
        // Arrange
        final String testRole = "SomeRole";
        final MemberId testUser = new MemberId("SomeUser");
        final MemberId testUser2 = new MemberId("SomeOtherUser");
        final Password testOldPassword = new Password("oldPassword123");
        final Password testNewPassword = new Password("newPassword123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        final HashedPassword testHashedNewPassword = new HashedPassword("hashedNewTestPassword");
        when(mockPasswordEncoder.hash(testOldPassword)).thenReturn(testHashedPassword);
        when(mockPasswordEncoder.match(testHashedPassword, testOldPassword)).thenReturn(true);
        when(mockPasswordEncoder.hash(testNewPassword)).thenReturn(testHashedNewPassword);

        // Act
        registrationService.registerUser(testUser, testOldPassword);
        registrationService.registerRole(testRole);
        registrationService.addRoleToAppUser(testUser, testRole);
        ThrowingCallable action = () -> registrationService.changeUserPassword(testUser, testOldPassword, testNewPassword);
        ThrowingCallable action2 = () -> registrationService.changeUserPassword(testUser2, testOldPassword, testNewPassword);

        // Assert
        assertThatNoException().isThrownBy(action);
        AppUser savedUser = userRepository.findByMemberId(testUser).orElseThrow();
        assertThat(savedUser.getPassword()).isEqualTo(testHashedNewPassword);

        when(mockPasswordEncoder.match(testHashedPassword, testOldPassword)).thenReturn(false);
        assertThatException().isThrownBy(action);
        assertThatException().isThrownBy(action2);
    }
}
