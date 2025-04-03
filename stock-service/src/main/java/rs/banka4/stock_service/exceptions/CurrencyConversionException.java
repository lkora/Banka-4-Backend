package rs.banka4.stock_service.exceptions;

import org.springframework.http.HttpStatus;
import rs.banka4.user_service.exceptions.BaseApiException;

public class CurrencyConversionException extends BaseApiException {
    public CurrencyConversionException() {
        super(HttpStatus.NOT_IMPLEMENTED, null);
    }
}
