package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de entrada para consultas de pedidos (requisito 4.3).
 *
 * <p>Duas operações: busca por ID e listagem de todos os pedidos do cliente
 * autenticado.
 *
 * @author Danilo Fernando
 */
public interface ConsultarPedidoUseCase {

    /**
     * Busca um pedido pelo seu identificador.
     *
     * @param pedidoId identificador do pedido
     * @return {@link Optional} com o pedido, ou vazio se não encontrado
     */
    Optional<PedidoResponse> porId(UUID pedidoId);

    /**
     * Lista todos os pedidos do cliente informado, em ordem decrescente de
     * criação (mais recentes primeiro).
     *
     * @param clienteId identificador do cliente (extraído do token JWT)
     * @return lista (possivelmente vazia) de pedidos
     */
    List<PedidoResponse> porCliente(UUID clienteId);
}
