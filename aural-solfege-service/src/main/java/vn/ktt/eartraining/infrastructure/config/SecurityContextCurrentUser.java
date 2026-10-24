package vn.ktt.eartraining.infrastructure.config;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import vn.ktt.eartraining.application.outbound.ICurrentUserPort;

@Component
public class SecurityContextCurrentUser implements ICurrentUserPort {
    @Override
    public String userId() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
            return token.getToken().getSubject();
        }
        throw new AuthenticationCredentialsNotFoundException("No authenticated user");
    }
}
