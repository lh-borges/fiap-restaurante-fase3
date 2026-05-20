package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utilitário estático para extrair o identificador do cliente autenticado a
 * partir do token JWT presente no {@link SecurityContextHolder}.
 *
 * <p>Atende ao requisito 5.2 da fase 3: "o ID do cliente deve vir do token".
 * O {@code subject} do JWT contém o {@code publicId} (UUID) do usuário,
 * conforme emitido pelo {@code JwtTokenProvider} do serviço
 * {@code usuario-autenticacao}.
 *
 * @author Danilo Fernando
 */
public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    /**
     * Retorna o {@code clienteId} do usuário autenticado.
     *
     * @return UUID do cliente extraído do {@code subject} do JWT
     * @throws BusinessException se não houver autenticação JWT válida no contexto
     */
    public static UUID clienteId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new BusinessException("Usuário não autenticado");
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Token JWT com subject inválido: " + jwt.getSubject());
        }
    }
}
