package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;

import java.util.UUID;

/**
 * Porta de entrada para a confirmação de um pedido pelo cliente (requisito 4.2).
 *
 * <p>Após a confirmação, o pedido é publicado no tópico Kafka
 * {@code pedido.criado} para que o microsserviço {@code pagamento} processe
 * a cobrança.
 *
 * @author Danilo Fernando
 */
public interface ConfirmarPedidoUseCase {

    /**
     * Confirma o pedido identificado por {@code pedidoId}.
     *
     * <p>O {@code clienteId} é verificado contra o dono do pedido para evitar
     * que um usuário confirme pedido de outro.
     *
     * @param pedidoId  identificador do pedido a confirmar
     * @param clienteId identificador do cliente autenticado (do JWT)
     * @return o pedido atualizado
     */
    PedidoResponse executar(UUID pedidoId, UUID clienteId);
}
