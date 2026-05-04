package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoInput;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResumoCriadoOutput;

/**
 * Porta de entrada para criação de pedidos.
 * Recebe os dados do pedido e o ID do cliente extraído do token JWT.
 * Calcula o valor total automaticamente com base nos itens informados.
 */

public interface CriarPedidoUseCase {

    PedidoResumoCriadoOutput criar(CriarPedidoInput input, String clienteId);

}
