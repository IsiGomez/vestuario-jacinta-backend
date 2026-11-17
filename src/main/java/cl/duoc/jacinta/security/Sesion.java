package cl.duoc.jacinta.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class Sesion {
    private Sesion() {}

    public static Long usuarioId(Authentication a) {
        if (a == null || a instanceof AnonymousAuthenticationToken) return null;
        try {
            return Long.valueOf(a.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean esStaff(Authentication a) {
        return a != null && a.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMINISTRADOR") || r.equals("ROLE_VENDEDOR"));
    }
}