package br.com.fiaprestaurante.restauranteservice.application.port.input;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoParaCozinhaEvent;

/**
 * Porta de entrada — disparada pelo consumer Kafka de
 * {@code pedido.pronto-para-cozinha}. Recebe o pedido aprovado e cria o
 * agregado {@code PedidoCozinha} no status RECEBIDO.
 *
 * @author Danilo Fernando
 */
public interface ReceberPedidoUseCase {

    /**
     * @param event payload deserializado do topico Kafka
     */
    void executar(PedidoProntoParaCozinhaEvent event);
}
