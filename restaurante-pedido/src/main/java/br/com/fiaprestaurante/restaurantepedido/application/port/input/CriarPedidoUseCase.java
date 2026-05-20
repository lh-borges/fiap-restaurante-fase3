package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;

/**
 * Porta de entrada para a criação de um novo pedido (requisito 4.2).
 *
 * <p>O retorno contém o ID gerado e o valor total calculado, para que o
 * cliente possa confirmar o pedido em uma chamada subsequente
 * ({@link ConfirmarPedidoUseCase}).
 *
 * @author Danilo Fernando
 */
public interface CriarPedidoUseCase {

    /**
     * Cria o pedido no status {@code CRIADO} (ainda não confirmado).
     *
     * @param command dados do pedido — o {@code clienteId} já deve ter sido
     *                extraído do token JWT pelo controller
     * @return o pedido recém-criado, com {@code id} e {@code valorTotal}
     */
    PedidoResponse executar(CriarPedidoCommand command);
}
