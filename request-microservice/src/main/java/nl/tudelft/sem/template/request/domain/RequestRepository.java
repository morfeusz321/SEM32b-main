package nl.tudelft.sem.template.request.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByOwnerIdAndStatus(Long ownerId, @Nullable Status status);

    List<Request> findByOwnerId(Long ownerId);

}
