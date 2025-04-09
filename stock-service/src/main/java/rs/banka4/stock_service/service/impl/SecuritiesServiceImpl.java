package rs.banka4.stock_service.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.responses.SecurityOwnershipResponse;
import rs.banka4.stock_service.domain.security.responses.TotalProfitResponse;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.OrderRepository;
import rs.banka4.stock_service.service.abstraction.ListingService;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;

@Service
@RequiredArgsConstructor
public class SecuritiesServiceImpl implements SecuritiesService {
    private final OrderRepository orderRepository;
    private final ListingService listingService;

    @Override
    public ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    ) {
        return null;
    }

    @Override
    public List<SecurityOwnershipResponse> getMySecurities() {
        UUID userId = getCurrentUserId();
        List<Order> holdings = orderRepository.findByUserId(userId);

        return holdings.stream()
            .map(this::mapToOrderOwnershipResponse)
            .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<TotalProfitResponse> getTotalUnrealizedProfit() {
        List<SecurityOwnershipResponse> holdings = getMySecurities();

        BigDecimal totalProfit = holdings.stream()
            .filter(h -> "Stock".equals(h.type()))
            .map(SecurityOwnershipResponse::profit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(new TotalProfitResponse(
            totalProfit,
            "USD"
        ));
    }

    private SecurityOwnershipResponse mapToOrderOwnershipResponse(Order order) {
        if (order.getAsset() instanceof Security security) {
            int amount = order.getQuantity();
            BigDecimal currentPrice = getCurrentPrice(security);
            BigDecimal profit = calculateProfit(security, amount, currentPrice);

            return new SecurityOwnershipResponse(
                determineAssetType(security),
                security.getTicker(),
                amount,
                currentPrice,
                profit,
                getLastModified(security)
            );
        }
        return null;
    }

    private String determineAssetType(Asset asset) {
        if (asset instanceof Stock) return "Stock";
        if (asset instanceof Future) return "Future";
        if (asset instanceof ForexPair) return "Forex";
        throw new IllegalArgumentException("Unsupported asset type");
    }

    private BigDecimal getCurrentPrice(Security security) {
        if (security instanceof ForexPair) {
            return ((ForexPair) security).getExchangeRate();
        }
        return listingService.getListingDetails(UUID.fromString(security.getTicker()))
            .getPrice();
    }

    private BigDecimal calculateProfit(Security security, int amount, BigDecimal currentPrice) {
        List<Order> buyOrders =
            orderRepository.findByUserIdAndAssetAndDirectionAndIsDone(
                getCurrentUserId(),
                security,
                Direction.BUY,
                true
            );

        BigDecimal totalBuyCost = BigDecimal.ZERO;
        BigDecimal totalBuyQuantity = BigDecimal.ZERO;

        for (Order order : buyOrders) {
            BigDecimal quantity = BigDecimal.valueOf(order.getQuantity());
            BigDecimal price =
                order.getPricePerUnit()
                    .getAmount();
            totalBuyCost = totalBuyCost.add(price.multiply(quantity));
            totalBuyQuantity = totalBuyQuantity.add(quantity);
        }

        if (totalBuyQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal averageCost = totalBuyCost.divide(totalBuyQuantity, RoundingMode.HALF_UP);
        return currentPrice.subtract(averageCost)
            .multiply(BigDecimal.valueOf(amount));
    }

    private OffsetDateTime getLastModified(Security security) {
        Order newestOrder =
            orderRepository.findNewestOrder(getCurrentUserId(), security, Direction.BUY, true);
        return newestOrder != null ? newestOrder.getLastModified() : null;
    }

    private UUID getCurrentUserId() {
        Authentication authentication =
            SecurityContextHolder.getContext()
                .getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

    private long totalQuantity(List<Order> orders) {
        return orders.stream()
            .mapToLong(Order::getQuantity)
            .sum();
    }

}
