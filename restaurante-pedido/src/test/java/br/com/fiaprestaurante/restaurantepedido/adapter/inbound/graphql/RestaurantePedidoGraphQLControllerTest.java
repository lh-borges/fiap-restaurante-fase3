package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.CriarPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ItemPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ModuloRestaurantePedidoPayload;
import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConfirmarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarModuloRestaurantePedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.CriarPedidoUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link RestaurantePedidoGraphQLController} - mocka
 * todos os use cases e valida a orquestracao (extracao do JWT,
 * conversao de inputs e delegacao para os use cases).
 *
 * @author Danilo Fernando
 */
class RestaurantePedidoGraphQLControllerTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID PEDIDO_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");

    private CriarPedidoUseCase criarPedido;
    private ConfirmarPedidoUseCase confirmarPedido;
    private ConsultarPedidoUseCase consultarPedido;
    private ConsultarModuloRestaurantePedidoUseCase consultarModulo;
    private RestaurantePedidoGraphQLController controller;

    @BeforeEach
    void setUp() {
        criarPedido = mock(CriarPedidoUseCase.class);
        confirmarPedido = mock(ConfirmarPedidoUseCase.class);
        consultarPedido = mock(ConsultarPedidoUseCase.class);
        consultarModulo = mock(ConsultarModuloRestaurantePedidoUseCase.class);
        controller = new RestaurantePedidoGraphQLController(
                criarPedido, confirmarPedido, consultarPedido, consultarModulo);

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"), Map.of("sub", CLIENTE_ID.toString()));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private PedidoResponse pedidoExemplo(String status) {
        return new PedidoResponse(
                PEDIDO_ID, CLIENTE_ID, RESTAURANTE_ID,
                List.of(new ItemPedidoResponse(PRODUTO_ID, "X-Burger", 2,
                        new BigDecimal("25.90"), new BigDecimal("51.80"))),
                new BigDecimal("51.80"), status, null, null,
                Instant.now(), Instant.now());
    }

    @Test
    void criarPedidoDeveExtrairClienteDoJwtEConverterInput() {
        CriarPedidoInput input = new CriarPedidoInput(RESTAURANTE_ID, List.of(
                new ItemPedidoInput(PRODUTO_ID, "X-Burger", 2, new BigDecimal("25.90"))));
        when(criarPedido.executar(any(CriarPedidoCommand.class)))
                .thenReturn(pedidoExemplo("CRIADO"));

        PedidoResponse resposta = controller.criarPedido(input);

        assertThat(resposta.status()).isEqualTo("CRIADO");
        ArgumentCaptor<CriarPedidoCommand> captor = ArgumentCaptor.forClass(CriarPedidoCommand.class);
        verify(criarPedido).executar(captor.capture());
        assertThat(captor.getValue().clienteId()).isEqualTo(CLIENTE_ID);
        assertThat(captor.getValue().restauranteId()).isEqualTo(RESTAURANTE_ID);
        assertThat(captor.getValue().itens()).hasSize(1);
    }

    @Test
    void confirmarPedidoDeveDelegarComClienteDoJwt() {
        when(confirmarPedido.executar(PEDIDO_ID, CLIENTE_ID))
                .thenReturn(pedidoExemplo("CONFIRMADO"));

        PedidoResponse resposta = controller.confirmarPedido(PEDIDO_ID.toString());

        assertThat(resposta.status()).isEqualTo("CONFIRMADO");
        verify(confirmarPedido).executar(PEDIDO_ID, CLIENTE_ID);
    }

    @Test
    void pedidoPorIdDeveRetornarRespostaQuandoExistir() {
        when(consultarPedido.porId(PEDIDO_ID))
                .thenReturn(Optional.of(pedidoExemplo("PAGO")));

        PedidoResponse resposta = controller.pedidoPorId(PEDIDO_ID.toString());

        assertThat(resposta).isNotNull();
        assertThat(resposta.status()).isEqualTo("PAGO");
    }

    @Test
    void pedidoPorIdDeveRetornarNullQuandoNaoExistir() {
        when(consultarPedido.porId(PEDIDO_ID)).thenReturn(Optional.empty());

        PedidoResponse resposta = controller.pedidoPorId(PEDIDO_ID.toString());

        assertThat(resposta).isNull();
    }

    @Test
    void meusPedidosDeveUsarClienteDoJwt() {
        when(consultarPedido.porCliente(CLIENTE_ID))
                .thenReturn(List.of(pedidoExemplo("PAGO"), pedidoExemplo("CRIADO")));

        List<PedidoResponse> resposta = controller.meusPedidos();

        assertThat(resposta).hasSize(2);
        verify(consultarPedido).porCliente(eq(CLIENTE_ID));
    }

    @Test
    void statusModuloDeveDelegarParaUseCase() {
        when(consultarModulo.executar()).thenReturn("Modulo ok");

        ModuloRestaurantePedidoPayload payload = controller.statusModuloRestaurantePedido();

        assertThat(payload.getNome()).isEqualTo("restaurante-pedido");
        assertThat(payload.isImplementado()).isTrue();
        assertThat(payload.getDescricao()).isEqualTo("Modulo ok");
    }
}
