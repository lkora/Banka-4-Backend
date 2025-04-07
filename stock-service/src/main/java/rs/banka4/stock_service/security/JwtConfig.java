package rs.banka4.stock_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.banka4.rafeisen.common.utils.jwt.JwtUtil;

@Configuration
public class JwtConfig {
    /* TODO(arsen): Maybe use asymmetric encryption? */
    private final SecretKey verificationKey;

    public JwtConfig(@Value("${jwt.keys.verification}") String signingKeyB64) {
        this.verificationKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(signingKeyB64));
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(
            Jwts.parser()
                .verifyWith(verificationKey)
                .build()
        );
    }
}
