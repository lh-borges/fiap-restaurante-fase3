package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoOutput;

import java.util.List;

/**
 * Porta de entrada para listagem de todos os pedidos do sistema.
 * Exclusivo para usuários com perfil DONO_RESTAURANTE.
 */

public interface ListarTodosPedidosUseCase {
    List<PedidoOutput> listarTodos();
}
