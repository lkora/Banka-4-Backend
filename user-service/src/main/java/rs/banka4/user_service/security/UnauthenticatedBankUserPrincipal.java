package rs.banka4.user_service.security;

import rs.banka4.rafeisen.common.security.UserType;

public record UnauthenticatedBankUserPrincipal(
    UserType userType,
    String email
) {
}
