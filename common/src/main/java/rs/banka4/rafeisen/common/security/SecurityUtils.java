package rs.banka4.rafeisen.common.security;

import org.springframework.security.core.GrantedAuthority;

public class SecurityUtils {
    public static GrantedAuthority asGrantedAuthority(Privilege privilege) {
        return privilege::name;
    }
}
