package rs.banka4.stock_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.options.db.Option;
import rs.banka4.stock_service.domain.orders.db.Direction;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.orders.db.Status;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.dtos.SecurityResponse;
import rs.banka4.stock_service.domain.security.dtos.TotalProfitResponse;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.stock.db.Stock;
import rs.banka4.stock_service.exceptions.CurrencyConversionException;
import rs.banka4.stock_service.repositories.OrderRepository;
import rs.banka4.stock_service.service.abstraction.SecuritiesService;
import rs.banka4.user_service.domain.currency.db.Currency;
import rs.banka4.user_service.service.impl.ExchangeRateService;
import rs.banka4.user_service.utils.JwtUtil;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecuritiesServiceImpl implements SecuritiesService {

    private JwtUtil jwtUtils;
    private final OrderRepository orderRepository;
    private final SecurityPriceService securityPriceService;
    private final ExchangeRateService exchangeRateService;

    @Override
    public ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    ) {
        return null;
    }

    public List<SecurityResponse> getMySecurities(Authentication authentication) {
        UUID userId = jwtUtils.extractUserId(authentication.getCredentials().toString());
        List<Order> orders = orderRepository.findByUserIdAndStatusAndIsDone(userId, Status.APPROVED, true);
        Map<Asset, Integer> assetQuantities = calculateNetQuantities(orders);
        return assetQuantities.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .map(entry -> createSecurityResponse(entry.getKey(), entry.getValue(), orders))
            .collect(Collectors.toList());
    }

    public TotalProfitResponse calculateTotalStockProfit(Authentication authentication) {
        UUID userId = jwtUtils.extractUserId(authentication.getCredentials().toString());

        CurrencyCode accountCurrency = getAccountCurrency(authentication);
        List<SecurityResponse> holdings = getMySecurities(authentication);

        BigDecimal total = holdings.stream()
            .filter(h -> "Stock".equals(h.type()))
            .map(h -> convertProfit(h, accountCurrency))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TotalProfitResponse(total, accountCurrency);
    }


    /**
     * Calculates net quantities for each asset from approved orders.
     *
     * @param orders List of user's approved orders
     * @return Map of assets to net quantities (positive holdings only)
     */
    private Map<Asset, Integer> calculateNetQuantities(List<Order> orders) {
        Map<Asset, Integer> quantities = new HashMap<>();
        // TODO: THIS `scheisse` inefficient! Better replace this with another table `AssetHolding` table and
        //  maintain the state there or use as cache
        for (Order order : orders) {
            Asset asset = order.getAsset();
            int qty = order.getDirection() == Direction.BUY ? order.getQuantity() : -order.getQuantity();
            quantities.merge(asset, qty, Integer::sum);
        }
        return quantities;
    }

    private SecurityResponse createSecurityResponse(Asset asset, int amount, List<Order> orders) {
        MonetaryAmount price = securityPriceService.getCurrentPrice(asset);
        BigDecimal profit = calculateProfit(asset, amount, price, orders);
        OffsetDateTime lastModified = getLastModified(asset, orders);
        return new SecurityResponse(
            getAssetType(asset),
            asset.getTicker(),
            amount,
            price.getAmount(),
            profit,
            lastModified,
            price.getCurrency()
        );
    }

    private BigDecimal calculateProfit(Asset asset, int amount, MonetaryAmount currentPrice, List<Order> orders) {
        MonetaryAmount totalCost = calculateTotalCost(asset, orders);
        BigDecimal currentValue = currentPrice.getAmount().multiply(BigDecimal.valueOf(amount));
        return currentValue.subtract(totalCost.getAmount());
    }

    private MonetaryAmount calculateTotalCost(Asset asset, List<Order> orders) {
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders.stream().filter(o -> o.getAsset().equals(asset)).toList()) {
            BigDecimal orderAmount = order.getPricePerUnit().getAmount().multiply(BigDecimal.valueOf(order.getQuantity()));
            total = order.getDirection() == Direction.BUY ? total.add(orderAmount) : total.subtract(orderAmount);
        }
        // TODO: Assuming all orders are in the same currency; adjust for currency conversions
        return new MonetaryAmount(total, orders.getFirst().getPricePerUnit().getCurrency());
    }

    private OffsetDateTime getLastModified(Asset asset, List<Order> orders) {
        return orders.stream()
            .filter(o -> o.getAsset().equals(asset))
            .map(Order::getLastModified)
            .max(OffsetDateTime::compareTo)
            .orElse(OffsetDateTime.now());
    }

    private String getAssetType(Asset asset) {
        if (asset instanceof Stock) return "Stock";
        if (asset instanceof Option) return "Option";
        if (asset instanceof ForexPair) return "Forex";
        if (asset instanceof Future) return "Future";
        return "Other";
    }

    private BigDecimal convertProfit(SecurityResponse holding, CurrencyCode targetCurrency) {
        try {
            return exchangeRateService.convertCurrency(
                holding.profit(),
                Currency.Code.USD, // Maybe add this: holding.currency(),, but conversion from CurrencyCode to CurrencyCode is necessary, or modify the exchangeRateService to accept CurrencyCode,
                Currency.Code.USD  // targetCurrency
            );
        } catch (NullPointerException e) {
            throw new CurrencyConversionException();
        }
    }

    private CurrencyCode getAccountCurrency(Authentication auth) {
        // TODO: How are we supposed to know what currency the profits are????
        return CurrencyCode.USD;
    }


}
