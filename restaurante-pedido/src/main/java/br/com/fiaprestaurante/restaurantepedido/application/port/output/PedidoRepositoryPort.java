package br.com.fiaprestaurante.restaurantepedido.application.port.output;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para persistência de pedidos.
 * Define o contrato que o adapter de persistência deve implementar.
 */

public interface PedidoRepositoryPort {

    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(UUID id);
    List<Pedido> buscarPorClienteId(String clienteId);
    List<Pedido> listarTodos();

}
