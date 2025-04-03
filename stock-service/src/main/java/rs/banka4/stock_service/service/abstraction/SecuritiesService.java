package rs.banka4.stock_service.service.abstraction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.orders.db.Order;
import rs.banka4.stock_service.domain.security.SecurityDto;
import rs.banka4.stock_service.domain.security.dtos.SecurityResponse;
import rs.banka4.stock_service.domain.security.dtos.TotalProfitResponse;
import rs.banka4.stock_service.exceptions.CurrencyConversionException;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public interface SecuritiesService {
    // Object is StockDto, FutureDto or ForexDto
    ResponseEntity<Page<SecurityDto>> getSecurities(
        String securityType,
        String name,
        Pageable pageable
    );

    /**
     * Retrieves all securities with positive holdings for a user.
     *
     * @return List of security responses with financial metrics
     */
    List<SecurityResponse> getMySecurities(Authentication authentication);


    /**
     * Calculates total stock profit converted to user's account currency
     * @return Converted total with currency
     * @throws CurrencyConversionException if exchange rates unavailable
     */
    TotalProfitResponse calculateTotalStockProfit(Authentication authentication);
}
