package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoOutput;

import java.util.List;

/**
 * Porta de entrada para listagem de pedidos do cliente autenticado.
 * Retorna apenas os pedidos pertencentes ao cliente que fez a requisição.
 */

public interface BuscarPedidosPorClienteUseCase {

    List<PedidoOutput> buscarPorCliente(String clienteId);
}
