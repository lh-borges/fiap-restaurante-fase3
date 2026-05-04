package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ModuloRestaurantePedidoPayload;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarModuloRestaurantePedidoUseCase;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.CriarPedidoGraphQLInput;
import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ModuloRestaurantePedidoPayload;
import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoOutput;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResumoCriadoOutput;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * Controller GraphQL responsável por expor as operações de pedidos e status do módulo.
 * Atua como adaptador de entrada (inbound adapter) na arquitetura hexagonal,
 * recebendo as requisições GraphQL, extraindo o cliente autenticado via JWT
 * e delegando para os casos de uso da camada de aplicação.
 */

@Controller
public class RestaurantePedidoGraphQLController {

    private final ConsultarModuloRestaurantePedidoUseCase moduloUseCase;
    private final CriarPedidoUseCase criarPedidoUseCase;
    private final ConfirmarPedidoUseCase confirmarPedidoUseCase;
    private final BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase;
    private final BuscarPedidosPorClienteUseCase buscarPedidosPorClienteUseCase;
    private final ListarTodosPedidosUseCase listarTodosPedidosUseCase;

    public RestaurantePedidoGraphQLController(
            ConsultarModuloRestaurantePedidoUseCase moduloUseCase,
            CriarPedidoUseCase criarPedidoUseCase,
            ConfirmarPedidoUseCase confirmarPedidoUseCase,
            BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase,
            BuscarPedidosPorClienteUseCase buscarPedidosPorClienteUseCase,
            ListarTodosPedidosUseCase listarTodosPedidosUseCase) {

        this.moduloUseCase = moduloUseCase;
        this.criarPedidoUseCase = criarPedidoUseCase;
        this.confirmarPedidoUseCase = confirmarPedidoUseCase;
        this.buscarPedidoPorIdUseCase = buscarPedidoPorIdUseCase;
        this.buscarPedidosPorClienteUseCase = buscarPedidosPorClienteUseCase;
        this.listarTodosPedidosUseCase = listarTodosPedidosUseCase;
    }


    /**
     * Retorna o status do módulo restaurante-pedido.
     * Utilizado para verificar se o módulo está ativo e funcionando.
     * Requer autenticação com perfil USUARIO ou DONO_RESTAURANTE.
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public ModuloRestaurantePedidoPayload statusModuloRestaurantePedido() {
        return new ModuloRestaurantePedidoPayload(moduloUseCase.executar());
    }


    /**
     * Cria um novo pedido para o cliente autenticado.
     * O ID do cliente é extraído automaticamente do token JWT.
     * O valor total é calculado com base nos itens informados.
     * O pedido é criado com status AGUARDANDO_CONFIRMACAO.
     * Requer autenticação com perfil USUARIO ou DONO_RESTAURANTE.
     */
    @MutationMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public PedidoResumoCriadoOutput criarPedido(
            @Argument CriarPedidoGraphQLInput input,
            @AuthenticationPrincipal Jwt jwt) {

        String clienteId = jwt.getSubject();

        List<ItemPedidoInput> itens = input.itens().stream()
                .map(i -> new ItemPedidoInput(i.produtoId(), i.nome(), i.quantidade(), i.preco()))
                .toList();

        CriarPedidoInput appInput = new CriarPedidoInput(input.restauranteId(), itens);
        return criarPedidoUseCase.criar(appInput, clienteId);
    }

    /**
     * Confirma um pedido existente, atualizando seu status para CONFIRMADO.
     * Somente o cliente dono do pedido pode confirmá-lo.
     * Requer autenticação com perfil USUARIO ou DONO_RESTAURANTE.
     */
    @MutationMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public Boolean confirmarPedido(
            @Argument UUID pedidoId,
            @AuthenticationPrincipal Jwt jwt) {

        String clienteId = jwt.getSubject();
        confirmarPedidoUseCase.confirmar(pedidoId, clienteId);
        return true;
    }


    /**
     * Busca um pedido pelo seu ID.
     * Retorna apenas pedidos pertencentes ao cliente autenticado.
     * Requer autenticação com perfil USUARIO ou DONO_RESTAURANTE.
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public PedidoOutput buscarPedidoPorId(
            @Argument UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        String clienteId = jwt.getSubject();
        return buscarPedidoPorIdUseCase.buscarPorId(id, clienteId);
    }

    /**
     * Lista todos os pedidos do cliente autenticado.
     * Retorna apenas os pedidos pertencentes ao cliente que fez a requisição.
     * Requer autenticação com perfil USUARIO ou DONO_RESTAURANTE.
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public List<PedidoOutput> meusPedidos(@AuthenticationPrincipal Jwt jwt) {
        String clienteId = jwt.getSubject();
        return buscarPedidosPorClienteUseCase.buscarPorCliente(clienteId);
    }

    /**
     * Lista todos os pedidos do sistema.
     * Exclusivo para usuários com perfil DONO_RESTAURANTE.
     * Permite ao administrador visualizar todos os pedidos realizados.
     */
    @QueryMapping
    @PreAuthorize("hasAuthority('DONO_RESTAURANTE')")
    public List<PedidoOutput> todosPedidos() {
        return listarTodosPedidosUseCase.listarTodos();
    }

}
