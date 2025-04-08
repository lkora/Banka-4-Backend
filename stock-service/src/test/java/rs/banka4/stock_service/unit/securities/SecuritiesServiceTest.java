package rs.banka4.stock_service.unit.securities;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.options.db.Option;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.orders.db.Status;
import rs.banka4.stock_service.domain.security.dtos.SecurityResponse;
import rs.banka4.stock_service.domain.security.dtos.TotalProfitResponse;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.repositories.AssetRepository;
import rs.banka4.stock_service.repositories.OrderRepository;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;
import rs.banka4.stock_service.service.impl.SecuritiesServiceImpl;
import rs.banka4.stock_service.service.impl.SecurityPriceService;
import rs.banka4.stock_service.utils.AssetGenerator;
import rs.banka4.testlib.integration.DbEnabledTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DbEnabledTest
@AutoConfigureMockMvc
public class SecuritiesServiceTest {

    @Autowired
    private SecuritiesService securitiesService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private SecurityPriceService securityPriceService;

    @BeforeEach
    void setup() {
        // Clear previous test data
        orderRepository.deleteAll();
        assetRepository.deleteAll();

        // Setup base assets
        assetRepository.saveAll(AssetGenerator.makeExampleAssets());
    }


    @Test
    @DisplayName("getMySecurities - should return correct positions with profit calculation")
    public void getMySecurities_WithMixedOrders_ReturnsValidPositions() {
        // Arrange
        Stock stock = (Stock) assetRepository.findById(AssetGenerator.STOCK_EX1_UUID)
            .orElseThrow();
        Option option = (Option) assetRepository.findById(AssetGenerator.OPTION_EX1_CALL_UUID)
            .orElseThrow();

        UUID userId = UUID.randomUUID();
        List<Order> orders = List.of(
            createOrder(userId, stock, Direction.BUY, 10, "150.00"),
            createOrder(userId, stock, Direction.SELL, 5, "170.00"),
            createOrder(userId, option, Direction.BUY, 2, "20.00")
        );
        orderRepository.saveAll(orders);

        when(securityPriceService.getCurrentPrice(stock))
            .thenReturn(new MonetaryAmount(new BigDecimal("180.00"), CurrencyCode.USD));
        when(securityPriceService.getCurrentPrice(option))
            .thenReturn(new MonetaryAmount(new BigDecimal("25.00"), CurrencyCode.USD));

        // Act
        List<SecurityResponse> result = securitiesService.getMySecurities(createAuth(userId));

        // Assert
        assertThat(result).hasSize(2);

        SecurityResponse stockResponse = result.stream()
            .filter(r -> r.type().equals("Stock"))
            .findFirst()
            .orElseThrow();

        assertEquals(5, stockResponse.amount());
        assertEquals(new BigDecimal("150.00"), stockResponse.profit()); // (180-150)*5

        SecurityResponse optionResponse = result.stream()
            .filter(r -> r.type().equals("Option"))
            .findFirst()
            .orElseThrow();

        assertEquals(2, optionResponse.amount());
        assertEquals(new BigDecimal("10.00"), optionResponse.profit()); // (25-20)*2
    }

    @Test
    @DisplayName("calculateTotalStockProfit - should sum only stock profits")
    public void calculateTotalStockProfit_WithMixedAssets_ReturnsStockSum() {
        // Arrange
        Stock stock = (Stock) assetRepository.findById(AssetGenerator.STOCK_EX1_UUID)
            .orElseThrow();
        UUID userId = UUID.randomUUID();

        orderRepository.saveAll(List.of(
            createOrder(userId, stock, Direction.BUY, 10, "150.00"),
            createOrder(userId, stock, Direction.BUY, 5, "160.00")
        ));

        when(securityPriceService.getCurrentPrice(stock))
            .thenReturn(new MonetaryAmount(new BigDecimal("170.00"), CurrencyCode.USD));

        // Act
        TotalProfitResponse result = securitiesService.calculateTotalStockProfit(createAuth(userId));

        // Assert
        BigDecimal expectedProfit = new BigDecimal("15.00")  // (170-150)*10 + (170-160)*5
            .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedProfit, result.totalProfit());
    }

    private Order createOrder(UUID userId, Asset asset, Direction direction, int quantity, String price) {
        return Order.builder()
            .userId(userId)
            .asset(asset)
            .direction(direction)
            .quantity(quantity)
            .pricePerUnit(new MonetaryAmount(new BigDecimal(price), CurrencyCode.USD))
            .status(Status.APPROVED)
            .isDone(true)
            .accountId(UUID.randomUUID())
            .build();
    }

    private Authentication createAuth(UUID userId) {
        return new TestingAuthenticationToken(
            new AuthenticatedUser(userId, List.of("CLIENT")),
            null,
            List.of(new SimpleGrantedAuthority("CLIENT"))
        );
    }

    // Inner class for test authentication
    private static class AuthenticatedUser {
        private final UUID userId;
        private final List<String> roles;

        AuthenticatedUser(UUID userId, List<String> roles) {
            this.userId = userId;
            this.roles = roles;
        }

        public UUID getUserId() { return userId; }
        public List<String> getRoles() { return roles; }
    }
}
