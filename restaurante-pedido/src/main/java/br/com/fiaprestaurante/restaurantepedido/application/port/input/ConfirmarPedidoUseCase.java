package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import java.util.UUID;

/**
 * Porta de entrada para confirmação de pedidos.
 * Atualiza o status do pedido de AGUARDANDO_CONFIRMACAO para CONFIRMADO.
 */

public interface ConfirmarPedidoUseCase {

    void confirmar(UUID pedidoId, String clienteId);

}
