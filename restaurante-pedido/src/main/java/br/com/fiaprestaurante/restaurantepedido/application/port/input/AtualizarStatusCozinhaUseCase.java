package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import java.util.UUID;

/**
 * Porta de entrada para atualizacao automatica do status do pedido em
 * resposta a eventos do restaurante-service (cozinha).
 *
 * <p>Invocada pelos consumers Kafka dos topicos {@code pedido.em-preparo}
 * e {@code pedido.pronto}.
 *
 * @author Danilo Fernando
 */
public interface AtualizarStatusCozinhaUseCase {

    /**
     * Marca o pedido como EM_PREPARO quando a cozinha inicia o preparo.
     *
     * <p>Idempotente: chamadas repetidas nao causam efeito.
     *
     * @param pedidoId identificador do pedido em preparo
     */
    void marcarComoEmPreparo(UUID pedidoId);

    /**
     * Marca o pedido como PRONTO quando a cozinha finaliza o preparo.
     *
     * <p>Idempotente: chamadas repetidas nao causam efeito.
     *
     * @param pedidoId identificador do pedido pronto
     */
    void marcarComoPronto(UUID pedidoId);
}
