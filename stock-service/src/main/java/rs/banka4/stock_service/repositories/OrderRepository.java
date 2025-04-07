package rs.banka4.stock_service.repositories;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.orders.db.Status;

import java.util.List;
import java.util.UUID;

/**
 * Repository for order data operations.
 */
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query(
        value = "select count(*) from Order o where o.asset.id = ?1 and o.createdAt between ?2 and ?3"
    )
    int findCountOfMadeOrdersToday(UUID assetId, OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * Finds completed and approved orders for a user.
     *
     * @param userId User identifier
     * @param status Order status (typically APPROVED)
     * @param isDone Completion flag (typically true)
     * @return List of matching orders
     */
    List<Order> findByUserIdAndStatusAndIsDone(UUID userId, Status status, boolean isDone);
}
