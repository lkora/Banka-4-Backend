package rs.banka4.stock_service.domain.security.responses;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The different types of securities available.")
public enum SecurityType {
    STOCK("Stock"),
    FUTURE("Future"),
    FOREX("Forex"),
    ;

    private final String displayName;

    SecurityType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
