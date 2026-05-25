package br.com.fiaprestaurante.restauranteservice.adapter.inbound.graphql;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.port.input.ConsultarFilaCozinhaUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.input.IniciarPreparoUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.input.MarcarComoProntoUseCase;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link RestauranteServiceGraphQLController} — mocka
 * os use cases e valida a orquestracao (extracao do JWT, parsing de status,
 * delegacao ao use case).
 *
 * @author Danilo Fernando
 */
class RestauranteServiceGraphQLControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID PEDIDO_COZINHA_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");

    private ConsultarFilaCozinhaUseCase consultarFila;
    private IniciarPreparoUseCase iniciarPreparo;
    private MarcarComoProntoUseCase marcarComoPronto;
    private RestauranteServiceGraphQLController controller;

    @BeforeEach
    void setUp() {
        consultarFila = mock(ConsultarFilaCozinhaUseCase.class);
        iniciarPreparo = mock(IniciarPreparoUseCase.class);
        marcarComoPronto = mock(MarcarComoProntoUseCase.class);
        controller = new RestauranteServiceGraphQLController(consultarFila, iniciarPreparo, marcarComoPronto);

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"), Map.of("sub", USER_ID.toString()));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private PedidoCozinhaResponse exemplo(String status) {
        return new PedidoCozinhaResponse(
                PEDIDO_COZINHA_ID, UUID.randomUUID(), UUID.randomUUID(),
                List.of(), status, "2026-05-25T12:00:00Z", "2026-05-25T12:00:00Z", null, null);
    }

    @Test
    void filaCozinhaSemStatusDevePassarNullParaUseCase() {
        when(consultarFila.listar(null)).thenReturn(List.of(exemplo("RECEBIDO")));
        List<PedidoCozinhaResponse> result = controller.filaCozinha(null);
        assertThat(result).hasSize(1);
        verify(consultarFila).listar(null);
    }

    @Test
    void filaCozinhaComStatusDeveConverterParaEnum() {
        when(consultarFila.listar(StatusCozinha.EM_PREPARO)).thenReturn(List.of());
        controller.filaCozinha("EM_PREPARO");
        verify(consultarFila).listar(StatusCozinha.EM_PREPARO);
    }

    @Test
    void filaCozinhaComStatusInvalidoDeveLancarBusinessException() {
        assertThatThrownBy(() -> controller.filaCozinha("XPTO"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Status invalido");
    }

    @Test
    void pedidoCozinhaPorIdDeveDelegarERetornarOptionalOuNull() {
        when(consultarFila.porId(PEDIDO_COZINHA_ID)).thenReturn(Optional.of(exemplo("EM_PREPARO")));
        assertThat(controller.pedidoCozinhaPorId(PEDIDO_COZINHA_ID.toString())).isNotNull();

        UUID outro = UUID.randomUUID();
        when(consultarFila.porId(outro)).thenReturn(Optional.empty());
        assertThat(controller.pedidoCozinhaPorId(outro.toString())).isNull();
    }

    @Test
    void iniciarPreparoDeveDelegarAoUseCase() {
        when(iniciarPreparo.executar(PEDIDO_COZINHA_ID)).thenReturn(exemplo("EM_PREPARO"));
        PedidoCozinhaResponse response = controller.iniciarPreparo(PEDIDO_COZINHA_ID.toString());
        assertThat(response.status()).isEqualTo("EM_PREPARO");
        verify(iniciarPreparo).executar(eq(PEDIDO_COZINHA_ID));
    }

    @Test
    void marcarComoProntoDeveDelegarAoUseCase() {
        when(marcarComoPronto.executar(PEDIDO_COZINHA_ID)).thenReturn(exemplo("PRONTO"));
        PedidoCozinhaResponse response = controller.marcarComoPronto(PEDIDO_COZINHA_ID.toString());
        assertThat(response.status()).isEqualTo("PRONTO");
        verify(marcarComoPronto).executar(eq(PEDIDO_COZINHA_ID));
    }
}
