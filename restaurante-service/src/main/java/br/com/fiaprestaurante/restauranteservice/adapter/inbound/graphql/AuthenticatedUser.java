package br.com.fiaprestaurante.restauranteservice.adapter.inbound.graphql;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utilitario estatico para extrair a identidade do usuario autenticado a
 * partir do token JWT no {@link SecurityContextHolder}.
 *
 * <p>Mesma estrategia usada no {@code restaurante-pedido}: o {@code subject}
 * do JWT contem o {@code publicId} (UUID) do usuario, conforme emitido pelo
 * {@code JwtTokenProvider} do {@code usuario-autenticacao}.
 *
 * @author Danilo Fernando
 */
public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    /**
     * @return UUID do usuario autenticado, extraido do {@code subject} do JWT
     * @throws BusinessException se nao houver autenticacao JWT valida no contexto
     */
    public static UUID id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessException("Usuario nao autenticado");
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Token JWT com subject invalido: " + jwt.getSubject());
        }
    }
}
