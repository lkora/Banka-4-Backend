package rs.banka4.rafeisen.common.utils.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;

/** Effectively a JWT parser helper function. */
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtParser jwtParser;

    /**
     * Given an unverified token, verifies and validates it, returning a proper, parsed token.
     *
     * @throws JwtParseFailed If it is not possible to parse this token. The is included.
     */
    public VerifiedToken parseToken(UnverifiedToken unverifiedToken) throws JwtParseFailed {
        final Jwt<?, Claims> jwt;
        try {
            jwt = jwtParser.parseSignedClaims(unverifiedToken.rawJwt());
        } catch (Exception e) {
            throw new JwtParseFailed("Could not validate raw token", e);
        }

        JwtTokenType type;
        try {
            type =
                JwtTokenType.valueOf(
                    jwt.getPayload()
                        .get("typ", String.class)
                );
        } catch (Exception e) {
            throw new JwtParseFailed("Failed to get JWT type", e);
        }

        return switch (type) {
        case ACCESS -> new VerifiedAccessToken(jwt.getPayload());
        case REFRESH -> new VerifiedRefreshToken(jwt.getPayload());
        };
    }
}
