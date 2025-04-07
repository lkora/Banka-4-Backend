package rs.banka4.stock_service.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.banka4.stock_service.domain.options.db.Asset;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
}
