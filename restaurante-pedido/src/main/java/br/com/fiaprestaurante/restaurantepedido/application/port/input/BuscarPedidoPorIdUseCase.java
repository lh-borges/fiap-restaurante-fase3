package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoOutput;

import java.util.UUID;

/**
 * Porta de entrada para busca de pedido por ID.
 * Retorna apenas pedidos pertencentes ao cliente autenticado.
 */

public interface BuscarPedidoPorIdUseCase {

    PedidoOutput buscarPorId(UUID id, String clienteId);

}
