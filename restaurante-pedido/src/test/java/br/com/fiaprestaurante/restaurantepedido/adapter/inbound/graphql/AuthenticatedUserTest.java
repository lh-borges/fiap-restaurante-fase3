package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitarios do {@link AuthenticatedUser} - cobre extracao do
 * clienteId do JWT presente no {@link SecurityContextHolder} e os
 * cenarios de erro.
 *
 * @author Danilo Fernando
 */
class AuthenticatedUserTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private Jwt buildJwt(String subject) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", subject)
        );
    }

    @Test
    void deveExtrairClienteIdDoSubjectDoJwt() {
        UUID esperado = UUID.fromString("11111111-1111-4111-8111-111111111111");
        Jwt jwt = buildJwt(esperado.toString());
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        UUID resultado = AuthenticatedUser.clienteId();

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void deveLancarBusinessExceptionSemAutenticacao() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(AuthenticatedUser::clienteId)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("autenticado");
    }

    @Test
    void deveLancarBusinessExceptionSeAutenticacaoNaoForJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "pass"));

        assertThatThrownBy(AuthenticatedUser::clienteId)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("autenticado");
    }

    @Test
    void deveLancarBusinessExceptionSeSubjectNaoForUuid() {
        Jwt jwt = buildJwt("nao-eh-uuid");
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThatThrownBy(AuthenticatedUser::clienteId)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("subject");
    }
}
