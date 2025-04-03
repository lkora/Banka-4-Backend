package rs.banka4.stock_service.service.impl;

import org.springframework.stereotype.Service;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.options.db.Asset;
import rs.banka4.stock_service.domain.options.db.BlackHolesOptionMaker;
import rs.banka4.stock_service.domain.options.db.Option;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.forex.db.ForexPair;
import rs.banka4.stock_service.domain.security.stock.db.Stock;

import java.math.BigDecimal;
import java.util.concurrent.Future;

@Service
public class SecurityPriceService {

    public MonetaryAmount getCurrentPrice(Asset asset) {
        // TODO: Assuming USD as currency
        MonetaryAmount monetaryAmount = new MonetaryAmount();
        monetaryAmount.setCurrency(CurrencyCode.USD);

        if (asset instanceof Stock) {
            monetaryAmount.setAmount(BigDecimal.valueOf(172.50));
            return monetaryAmount;
        } else if (asset instanceof Option option) {
            monetaryAmount.setAmount(calculateOptionPrice(option));
            return monetaryAmount;
        } else if (asset instanceof ForexPair forex) {
            monetaryAmount.setAmount(forex.getExchangeRate());
            return monetaryAmount;
        } else if (asset instanceof Future) {
            monetaryAmount.setAmount(BigDecimal.valueOf(100.00));
            return monetaryAmount;
        }

        monetaryAmount.setAmount(BigDecimal.ZERO);
        return monetaryAmount;
    }

    private BigDecimal calculateOptionPrice(Option option) {
        Stock stock = option.getStock();
        BigDecimal stockPrice = getCurrentPrice(stock).getAmount();
        return BigDecimal.valueOf(BlackHolesOptionMaker.calculateOptionPriceFromOption(option, stockPrice.doubleValue()));
    }
}
