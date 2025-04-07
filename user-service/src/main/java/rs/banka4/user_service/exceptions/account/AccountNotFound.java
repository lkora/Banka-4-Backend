package rs.banka4.user_service.exceptions.account;

import java.util.Map;
import org.springframework.http.HttpStatus;
import rs.banka4.rafeisen.common.exceptions.BaseApiException;

public class AccountNotFound extends BaseApiException {
    public AccountNotFound() {
        super(HttpStatus.NOT_FOUND, null);
    }

    public AccountNotFound(String accountNumber) {
        super(HttpStatus.NOT_FOUND, Map.of("accountNumber", accountNumber));
    }
}
