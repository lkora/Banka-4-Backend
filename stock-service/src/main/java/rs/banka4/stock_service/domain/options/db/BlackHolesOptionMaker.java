package rs.banka4.stock_service.domain.options.db;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.stereotype.Component;
import rs.banka4.stock_service.domain.actuaries.db.MonetaryAmount;
import rs.banka4.stock_service.domain.security.forex.db.CurrencyCode;
import rs.banka4.stock_service.domain.security.stock.db.Stock;


@Component
@RequiredArgsConstructor
public class BlackHolesOptionMaker {

    private static final NormalDistribution N = new NormalDistribution();

    public static double calculateOptionPrice(
        double S, // current stock price
        double K, // strike price
        double T, // time to expiration (in years)
        double r, // risk-free rate (annualized)
        double sigma, // implied volatility (annualized)
        OptionType type // CALL or PUT
    ) {
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);

        if (type == OptionType.CALL) {
            return S * N.cumulativeProbability(d1)
                - K * Math.exp(-r * T) * N.cumulativeProbability(d2);
        } else {
            return K * Math.exp(-r * T) * N.cumulativeProbability(-d2)
                - S * N.cumulativeProbability(-d1);
        }
    }

    public static double calculateOptionPriceFromOption(Option option, double currentStockPrice) {
        double riskFreeRate = 0.02;
        double S = currentStockPrice;
        double K =
            option.getStrikePrice()
                .getAmount()
                .doubleValue();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        double T =
            ChronoUnit.SECONDS.between(now, option.getSettlementDate()) / (365.25 * 24 * 60 * 60);

        if (T <= 0) {
            return 0.0;
        }

        double r = riskFreeRate;
        double sigma = option.getImpliedVolatility();
        OptionType type = option.getOptionType();

        return calculateOptionPrice(S, K, T, r, sigma, type);
    }


    public static String makeOptionTicker(
        OffsetDateTime expiry,
        String ticker,
        OptionType type,
        int strikePrice
    ) {
        String datePart = expiry.format(DateTimeFormatter.ofPattern("yyMMdd"));
        String optionTypeChar = type == OptionType.CALL ? "C" : "P";
        String strikeFormatted = String.format("%08d", strikePrice * 100);
        return ticker.toUpperCase() + datePart + optionTypeChar + strikeFormatted;
    }

    public List<Option> generateOptions(Stock stock, BigDecimal currentPrice) {
        List<Option> options = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        int basePrice =
            currentPrice.setScale(0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        List<Integer> strikePrices = new ArrayList<>();
        for (int i = -5; i <= 5; i++) {
            strikePrices.add(basePrice + i);
        }

        List<OffsetDateTime> expiries = new ArrayList<>();
        for (int i = 6; i <= 30; i += 6) {
            expiries.add(now.plusDays(i));
        }
        for (int i = 60; i <= 210; i += 30) {
            expiries.add(now.plusDays(i));
        }

        for (OffsetDateTime expiry : expiries) {
            for (Integer strike : strikePrices) {
                MonetaryAmount strikeAmount =
                    new MonetaryAmount(BigDecimal.valueOf(strike), CurrencyCode.USD);

                String tickerC =
                    makeOptionTicker(expiry, stock.getTicker(), OptionType.CALL, strike);
                String tickerP =
                    makeOptionTicker(expiry, stock.getTicker(), OptionType.PUT, strike);

                int daysToExpiry = (int) ChronoUnit.DAYS.between(now, expiry);
                int openInterest = Math.max(0, 500 - daysToExpiry * 5);

                Option callOption =
                    Option.builder()
                        .stock(stock)
                        .optionType(OptionType.CALL)
                        .strikePrice(strikeAmount)
                        .impliedVolatility(0.4)
                        .openInterest(openInterest)
                        .settlementDate(expiry)
                        .name(stock.getTicker() + "-CALL-" + strike + "-" + expiry.toLocalDate())
                        .ticker(tickerC)
                        .build();

                Option putOption =
                    Option.builder()
                        .stock(stock)
                        .optionType(OptionType.PUT)
                        .strikePrice(strikeAmount)
                        .impliedVolatility(0.4)
                        .openInterest(openInterest)
                        .settlementDate(expiry)
                        .name(stock.getTicker() + "-PUT-" + strike + "-" + expiry.toLocalDate())
                        .ticker(tickerP)
                        .build();

                options.add(callOption);
                options.add(putOption);
            }
        }

        return options;
    }
}
