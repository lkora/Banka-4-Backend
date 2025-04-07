package rs.banka4.user_service.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.user_service.domain.authenticator.db.UserTotpSecret;

public interface UserTotpSecretRepository extends JpaRepository<UserTotpSecret, UUID> {
    Optional<UserTotpSecret> findByEmployee_Id(UUID id);

    Optional<UserTotpSecret> findByClient_Id(UUID id);

    boolean existsByClient_Id(UUID uuid);

    boolean existsByEmployee_Id(UUID uuid);
}
