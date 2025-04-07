package rs.banka4.rafeisen.common;

import static org.assertj.core.api.Assertions.*;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.EnumSet;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import rs.banka4.rafeisen.common.security.Privilege;
import rs.banka4.rafeisen.common.security.UserType;
import rs.banka4.rafeisen.common.utils.jwt.JwtParseFailed;
import rs.banka4.rafeisen.common.utils.jwt.JwtUtil;
import rs.banka4.rafeisen.common.utils.jwt.UnverifiedToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedAccessToken;
import rs.banka4.rafeisen.common.utils.jwt.VerifiedRefreshToken;
import rs.banka4.testlib.utils.JwtPlaceholders;

public class JwtUtilTests {
    private static final byte[] TERRIBLE_SECRET = new byte[64];

    private static JwtParser getJwtParser() {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(TERRIBLE_SECRET))
            .build();
    }

    private static JwtBuilder getJwtBuilder() {
        return Jwts.builder()
            .signWith(Keys.hmacShaKeyFor(TERRIBLE_SECRET));
    }

    @Test
    public void test_access_token_survives_round_trip() throws JwtParseFailed {
        final var jwtUtil = new JwtUtil(getJwtParser());
        final var accessToken =
            new VerifiedAccessToken(
                UserType.EMPLOYEE,
                UUID.fromString("a4bf370e-2129-4116-9243-0c4ead0fe43e"),
                EnumSet.of(Privilege.ADMIN)
            );
        final var exampleJwt =
            getJwtBuilder().claims(accessToken.getClaims())
                .compact();

        final var roundTripAccessToken = jwtUtil.parseToken(new UnverifiedToken(exampleJwt));

        assertThat(roundTripAccessToken).isEqualTo(accessToken);
    }

    @Test
    public void test_refresh_token_survives_round_trip() throws JwtParseFailed {
        final var jwtUtil = new JwtUtil(getJwtParser());
        final var accessToken =
            new VerifiedRefreshToken(
                UserType.CLIENT,
                UUID.fromString("71e6a6ca-88c8-4396-9a23-2a2e406a9e78")
            );
        final var exampleJwt =
            getJwtBuilder().claims(accessToken.getClaims())
                .compact();

        final var roundTripAccessToken = jwtUtil.parseToken(new UnverifiedToken(exampleJwt));

        assertThat(roundTripAccessToken).isEqualTo(accessToken);
    }

    @Test
    public void test_out_of_date_version_is_rejected() {
        final var outOfDateToken =
            "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhNGJmMzcwZS0yMTI5LTQxMTYtOTI0My0wYzRlYWQwZmU0M2UiLCJwcml2aWxlZ2VzIjpbIkFETUlOIl0sInR5cCI6IkFDQ0VTUyIsInZlciI6Miwicm9sZSI6IkVNUExPWUVFIn0.MlnOauHLFn4pmj79pU7bzdSnlT8D3dV8nh-2ORv9pXPTrncOSylLCx8107xCDWVvCm9ojvFjNqlmuhgq8WWhgg";
        final var jwtUtil = new JwtUtil(getJwtParser());

        assertThatThrownBy(() -> {
            jwtUtil.parseToken(new UnverifiedToken(outOfDateToken));
        }).isInstanceOf(JwtParseFailed.class)
            .cause()
            .hasMessageContaining("version unsupported");
    }

    @Test
    public void test_empty_privilege_parse_passes() throws JwtParseFailed {
        final var jwtUtil = new JwtUtil(getJwtParser());

        assertThat(jwtUtil.parseToken(new UnverifiedToken(JwtPlaceholders.CLIENT_TOKEN)))
            .asInstanceOf(InstanceOfAssertFactories.type(VerifiedAccessToken.class))
            .extracting(
                VerifiedAccessToken::getPrivileges,
                as(InstanceOfAssertFactories.COLLECTION)
            )
            .isEmpty();
    }
}
