package rs.banka4.user_service.security;

import java.util.UUID;
import rs.banka4.rafeisen.common.security.UserType;

/**
 * Answers the question "which user is this token talking about".
 *
 * @see AuthenticatedBankUserAuthentication, the token using this principal
 */
public record AuthenticatedBankUserPrincipal(
    UserType userType,
    UUID userId
) {
}
