package rs.banka4.stock_service.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.banka4.stock_service.domain.listing.db.ListingDailyPriceInfo;

public interface DailyListingsRepository extends JpaRepository<ListingDailyPriceInfo, UUID> {

}
