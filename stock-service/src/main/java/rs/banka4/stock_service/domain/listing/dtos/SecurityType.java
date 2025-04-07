package rs.banka4.stock_service.domain.listing.dtos;

import lombok.Getter;
import rs.banka4.stock_service.domain.security.Security;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.future.db.Future;
import rs.banka4.stock_service.domain.security.stock.db.Stock;

@Getter
public enum SecurityType {
    FUTURE(Future.class),
    STOCK(Stock.class),
    FOREX_PAIR(ForexPair.class);

    private final Class<? extends Security> typeClass;

    SecurityType(Class<? extends Security> typeClass) {
        this.typeClass = typeClass;
    }
}
