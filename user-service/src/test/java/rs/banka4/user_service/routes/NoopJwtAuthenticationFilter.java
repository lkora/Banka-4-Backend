package rs.banka4.user_service.routes;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import rs.banka4.user_service.config.filters.JwtAuthenticationFilter;

/**
 * Hack filter to replace {@link JwtAuthenticationFilter} in tests that don't rely on
 * authentication.
 */
public class NoopJwtAuthenticationFilter extends JwtAuthenticationFilter {
    public NoopJwtAuthenticationFilter() {
        super(null);
    }

    @Override
    public void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException,
        ServletException {
        /* Skip the JWT auth filter. */
        filterChain.doFilter(request, response);
    }
}
