package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.grpc.UsuarioResponse;
import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoOutput;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResumoCriadoOutput;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.*;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepositoryPort;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.UsuarioGrpcPort;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoInput;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável por orquestrar os casos de uso relacionados a pedidos.
 * Implementa todas as portas de entrada da camada de aplicação.
 * Utiliza comunicação gRPC para buscar dados do cliente autenticado
 * e delega a persistência através da porta de saída do repositório.
 */

@Service
public class PedidoService implements
        CriarPedidoUseCase,
        ConfirmarPedidoUseCase,
        BuscarPedidoPorIdUseCase,
        BuscarPedidosPorClienteUseCase,
        ListarTodosPedidosUseCase
    {

    private final PedidoRepositoryPort pedidoRepository;
    private final UsuarioGrpcPort usuarioGrpcPort;

    public PedidoService(PedidoRepositoryPort pedidoRepository, UsuarioGrpcPort usuarioGrpcPort) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioGrpcPort = usuarioGrpcPort;
    }

    /**
    * Cria um novo pedido para o cliente autenticado.
    * Busca os dados do cliente via gRPC.
    * Calcula o valor total do pedido.
    * Persiste o pedido no repositório com o status aguardando confirmação.
    */

    @Override
    public PedidoResumoCriadoOutput criar(CriarPedidoInput input, String clienteId) {
        UsuarioResponse usuario = usuarioGrpcPort.buscarUsuario(clienteId);

        List<ItemPedido> itens = input.itens().stream()
                .map(i -> new ItemPedido(i.produtoId(), i.nome(), i.quantidade(), i.preco()))
                .toList();

        Pedido pedido = new Pedido(
                usuario.getPublicId(),
                usuario.getNome(),
                usuario.getEmail(),
                input.restauranteId(),
                itens
        );

        Pedido salvo = pedidoRepository.salvar(pedido);

        return new PedidoResumoCriadoOutput(
                salvo.getId(),
                salvo.getValorTotal(),
                salvo.getStatus().name()
        );
    }


    /**
    * Confirma um pedido existente, atualizando seu status para CONFIRMADO.
    * Lança PedidoNaoEncontradoException caso o pedido não seja encontrado.
    */
    @Override
    public void confirmar(UUID pedidoId, String clienteId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        pedido.confirmar();
        pedidoRepository.salvar(pedido);
    }

    /**
     * Busca um pedido por seu ID, retornando os dados do pedido.
     * Lança PedidoNaoEncontradoException caso o pedido não seja encontrado.
     */

    @Override
    public PedidoOutput buscarPorId(UUID id, String clienteId) {
        Pedido pedido = pedidoRepository.buscarPorId(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));

        return toOutput(pedido);
    }


    /**
     * Lista todos os pedidos do cliente autenticado.
     * Retorna apenas os pedidos pertencentes ao cliente que fez a requisição.
     */
    @Override
    public List<PedidoOutput> buscarPorCliente(String clienteId) {
        return pedidoRepository.buscarPorClienteId(clienteId)
                .stream()
                .map(this::toOutput)
                .toList();
    }

        /**
         * Lista todos os pedidos do sistema.
         * Exclusivo para usuários com perfil DONO_RESTAURANTE.
         */

        @Override
        public List<PedidoOutput> listarTodos() {
            return pedidoRepository.listarTodos()
                    .stream()
                    .map(this::toOutput)
                    .toList();
        }

    /**
     * Converte uma entidade de domínio Pedido para o DTO de saída PedidoOutput.
     */
    private PedidoOutput toOutput(Pedido pedido) {
        List<ItemPedidoInput> itens = pedido.getItens().stream()
                .map(i -> new ItemPedidoInput(i.getProdutoId(), i.getNome(),
                        i.getQuantidade(), i.getPreco()))
                .toList();

        return new PedidoOutput(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getClienteNome(),
                pedido.getRestauranteId(),
                itens,
                pedido.getValorTotal(),
                pedido.getStatus().name(),
                pedido.getCriadoEm()
        );
    }


    }
