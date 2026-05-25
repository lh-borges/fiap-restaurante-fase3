package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.CriarPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ItemPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ModuloRestaurantePedidoPayload;
import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConfirmarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarModuloRestaurantePedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.CriarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * Adapter de entrada GraphQL — expõe as mutations e queries de pedido.
 *
 * <p>Schema correspondente em {@code src/main/resources/graphql/schema.graphqls}.
 *
 * <p>Autorização via {@link PreAuthorize} usando a claim {@code groups} do
 * JWT emitido pelo {@code usuario-autenticacao}. O {@code clienteId} é
 * sempre extraído do token via {@link AuthenticatedUser}, atendendo ao
 * requisito 5.2.
 *
 * @author Danilo Fernando
 */
@Controller
public class RestaurantePedidoGraphQLController {

    private final CriarPedidoUseCase criarPedido;
    private final ConfirmarPedidoUseCase confirmarPedido;
    private final ConsultarPedidoUseCase consultarPedido;
    private final ConsultarModuloRestaurantePedidoUseCase consultarModulo;

    /**
     * @param criarPedido     porta de entrada para criação de pedido
     * @param confirmarPedido porta de entrada para confirmação
     * @param consultarPedido porta de entrada para consultas
     * @param consultarModulo porta de entrada para status do módulo (legado)
     */
    public RestaurantePedidoGraphQLController(CriarPedidoUseCase criarPedido,
                                              ConfirmarPedidoUseCase confirmarPedido,
                                              ConsultarPedidoUseCase consultarPedido,
                                              ConsultarModuloRestaurantePedidoUseCase consultarModulo) {
        this.criarPedido = criarPedido;
        this.confirmarPedido = confirmarPedido;
        this.consultarPedido = consultarPedido;
        this.consultarModulo = consultarModulo;
    }

    /**
     * Mutation GraphQL {@code criarPedido(input: CriarPedidoInput!): Pedido!}.
     *
     * <p>Cria o pedido no status {@code CRIADO} e devolve o ID e o valor
     * total calculado para o cliente confirmar (requisito 4.2).
     *
     * @param input dados recebidos do cliente
     * @return o pedido recém-criado
     */
    @MutationMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public PedidoResponse criarPedido(@Argument CriarPedidoInput input) {
        List<ItemPedidoCommand> itens = input.itens().stream()
                .map(RestaurantePedidoGraphQLController::toCommand)
                .toList();
        CriarPedidoCommand command = new CriarPedidoCommand(
                AuthenticatedUser.clienteId(),
                input.restauranteId(),
                itens
        );
        return criarPedido.executar(command);
    }

    /**
     * Mutation GraphQL {@code confirmarPedido(pedidoId: ID!): Pedido!}.
     *
     * <p>Confirma o pedido e publica o evento {@code pedido.criado} no Kafka
     * para iniciar o fluxo de pagamento (requisitos 4.2 e 5.3).
     *
     * @param pedidoId identificador do pedido a confirmar
     * @return o pedido atualizado
     */
    @MutationMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public PedidoResponse confirmarPedido(@Argument String pedidoId) {
        return confirmarPedido.executar(UUID.fromString(pedidoId), AuthenticatedUser.clienteId());
    }

    /**
     * Query GraphQL {@code pedidoPorId(pedidoId: ID!): Pedido}.
     *
     * @param pedidoId identificador do pedido
     * @return o pedido correspondente
     * @throws PedidoNaoEncontradoException quando o pedido não existe
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public PedidoResponse pedidoPorId(@Argument String pedidoId) {
        return consultarPedido.porId(UUID.fromString(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(
                        "Pedido não encontrado para o identificador informado."));
    }

    /**
     * Query GraphQL {@code meusPedidos: [Pedido!]!}.
     *
     * <p>Lista todos os pedidos do cliente autenticado, do mais recente para
     * o mais antigo (requisito 4.3).
     *
     * @return lista (possivelmente vazia) de pedidos
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public List<PedidoResponse> meusPedidos() {
        return consultarPedido.porCliente(AuthenticatedUser.clienteId());
    }

    /**
     * Query GraphQL {@code statusModuloRestaurantePedido} — health check
     * legado do módulo, mantido para retrocompatibilidade da coleção Postman.
     *
     * @return payload com o status atual do módulo
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public ModuloRestaurantePedidoPayload statusModuloRestaurantePedido() {
        return new ModuloRestaurantePedidoPayload(consultarModulo.executar());
    }

    /**
     * Converte um {@link ItemPedidoInput} (GraphQL) em
     * {@link ItemPedidoCommand} (application DTO).
     *
     * @param input item recebido na requisição
     * @return DTO da camada de aplicação
     */
    private static ItemPedidoCommand toCommand(ItemPedidoInput input) {
        return new ItemPedidoCommand(input.produtoId(), input.nome(), input.quantidade(), input.preco());
    }
}
