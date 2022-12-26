package nl.tudelft.sem.template.user.domain;


import java.util.List;
import nl.tudelft.sem.template.user.models.UserRequestModel;
import nl.tudelft.sem.template.user.models.UserResponseModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    /**
     * Find user by its netId.
     */
    User findById(Long id);

    /**
     * Check if an existing user already uses a netId.
     */
    boolean existsById(Long id);

    void deleteById(Long id);

    void delete(User user);

    UserResponseModel save(UserRequestModel userRequestModel);

    List<User> findAllById(Long id);

}
