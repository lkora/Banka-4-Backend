package rs.banka4.rafeisen.common.utils.jwt;

import io.jsonwebtoken.Claims;
import java.util.Map;
import java.util.UUID;
import rs.banka4.rafeisen.common.security.UserType;

public class VerifiedRefreshToken extends VerifiedToken {
    /** Construct a fresh refresh token for a given user by ID. */
    public VerifiedRefreshToken(UserType role, UUID userId) {
        super(role, userId, JwtTokenType.REFRESH);
    }

    /**
     * Given {@code claims}, construct a refresh token populated with information therein.
     */
    public VerifiedRefreshToken(Claims claims) throws JwtParseFailed {
        super(claims, JwtTokenType.REFRESH);
    }

    @Override
    public Map<String, ?> getClaims() {
        return super.populateBaseClaims();
    }
}
