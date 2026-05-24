package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.restaurantepedido.TestFixtures;
import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.CriarPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ItemPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConfirmarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarModuloRestaurantePedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.CriarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiaprestaurante.restaurantepedido.infrastructure.exception.RestaurantePedidoGraphQLExceptionHandler;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphQlSupportTest {

    @Mock
    private CriarPedidoUseCase criarPedido;

    @Mock
    private ConfirmarPedidoUseCase confirmarPedido;

    @Mock
    private ConsultarPedidoUseCase consultarPedido;

    @Mock
    private ConsultarModuloRestaurantePedidoUseCase consultarModulo;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveExtrairClienteIdDoJwtAutenticado() {
        autenticar(TestFixtures.CLIENTE_ID.toString());

        assertThat(AuthenticatedUser.clienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
    }

    @Test
    void deveFalharSemJwtOuComSubjectInvalido() {
        assertThatThrownBy(AuthenticatedUser::clienteId)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Usuário não autenticado");

        autenticar("subject-invalido");

        assertThatThrownBy(AuthenticatedUser::clienteId)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token JWT com subject inválido");
    }

    @Test
    void deveCriarPedidoUsandoClienteDoToken() {
        RestaurantePedidoGraphQLController controller = controller();
        PedidoResponse esperado = PedidoResponse.from(TestFixtures.pedidoCriado());
        when(criarPedido.executar(org.mockito.ArgumentMatchers.any(CriarPedidoCommand.class))).thenReturn(esperado);
        autenticar(TestFixtures.CLIENTE_ID.toString());

        PedidoResponse response = controller.criarPedido(new CriarPedidoInput(
                TestFixtures.RESTAURANTE_ID,
                List.of(new ItemPedidoInput(TestFixtures.PRODUTO_ID, "Pizza", 2, new BigDecimal("25.50")))
        ));

        assertThat(response).isEqualTo(esperado);
        ArgumentCaptor<CriarPedidoCommand> commandCaptor = ArgumentCaptor.forClass(CriarPedidoCommand.class);
        verify(criarPedido).executar(commandCaptor.capture());
        assertThat(commandCaptor.getValue().clienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
        assertThat(commandCaptor.getValue().restauranteId()).isEqualTo(TestFixtures.RESTAURANTE_ID);
        assertThat(commandCaptor.getValue().itens()).singleElement()
                .extracting(ItemPedidoCommand::nome)
                .isEqualTo("Pizza");
    }

    @Test
    void deveConfirmarPedidoUsandoClienteDoToken() {
        RestaurantePedidoGraphQLController controller = controller();
        PedidoResponse esperado = PedidoResponse.from(TestFixtures.pedidoConfirmado());
        when(confirmarPedido.executar(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID)).thenReturn(esperado);
        autenticar(TestFixtures.CLIENTE_ID.toString());

        PedidoResponse response = controller.confirmarPedido(TestFixtures.PEDIDO_ID.toString());

        assertThat(response).isEqualTo(esperado);
    }

    @Test
    void deveConsultarPedidoPorIdMeusPedidosEStatusDoModulo() {
        RestaurantePedidoGraphQLController controller = controller();
        PedidoResponse esperado = PedidoResponse.from(TestFixtures.pedidoCriado());
        when(consultarPedido.porId(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID)).thenReturn(Optional.of(esperado));
        when(consultarPedido.porCliente(TestFixtures.CLIENTE_ID)).thenReturn(List.of(esperado));
        when(consultarModulo.executar()).thenReturn("operacional");
        autenticar(TestFixtures.CLIENTE_ID.toString());

        assertThat(controller.pedidoPorId(TestFixtures.PEDIDO_ID.toString())).isEqualTo(esperado);
        assertThat(controller.meusPedidos()).containsExactly(esperado);
        assertThat(controller.statusModuloRestaurantePedido().getDescricao()).isEqualTo("operacional");
    }

    @Test
    void deveRetornarNullQuandoPedidoPorIdNaoForEncontrado() {
        RestaurantePedidoGraphQLController controller = controller();
        when(consultarPedido.porId(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID)).thenReturn(Optional.empty());
        autenticar(TestFixtures.CLIENTE_ID.toString());

        assertThat(controller.pedidoPorId(TestFixtures.PEDIDO_ID.toString())).isNull();
    }

    @Test
    void deveResolverExcecoesParaErrosGraphQlClassificados() {
        RestaurantePedidoGraphQLExceptionHandler handler = new RestaurantePedidoGraphQLExceptionHandler();
        DataFetchingEnvironment env = ambienteGraphQl();

        GraphQLError notFound = handler.handlePedidoNaoEncontrado(
                new PedidoNaoEncontradoException("pedido não encontrado"), env);
        GraphQLError badRequest = handler.handleBusinessException(new BusinessException("regra inválida"), env);
        GraphQLError invalidArgument = handler.handleIllegalArgument(new IllegalArgumentException("uuid inválido"), env);
        GraphQLError unexpected = handler.handleUnexpected(new IllegalStateException("erro técnico"), null);

        assertThat(notFound.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(notFound.getMessage()).isEqualTo("pedido não encontrado");
        assertThat(badRequest.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(badRequest.getMessage()).isEqualTo("regra inválida");
        assertThat(invalidArgument.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(invalidArgument.getMessage()).isEqualTo("Argumento invalido: uuid inválido");
        assertThat(unexpected.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
        assertThat(unexpected.getMessage()).isEqualTo("Erro interno ao processar a requisicao.");
    }

    private RestaurantePedidoGraphQLController controller() {
        return new RestaurantePedidoGraphQLController(criarPedido, confirmarPedido, consultarPedido, consultarModulo);
    }

    private static void autenticar(String subject) {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", subject));
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("USUARIO"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static DataFetchingEnvironment ambienteGraphQl() {
        DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);
        ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
        when(env.getExecutionStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getPath()).thenReturn(ResultPath.rootPath().segment("pedidoPorId"));
        when(env.getField()).thenReturn(Field.newField("pedidoPorId").build());
        return env;
    }
}
