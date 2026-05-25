package br.com.fiaprestaurante.restauranteservice.application.port.output;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoEmPreparoEvent;
import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoEvent;

/**
 * Porta de saida — publicacao de eventos da cozinha em mensageria assincrona.
 *
 * <p>Implementada pelo adapter {@code CozinhaKafkaPublisher} usando o
 * {@code KafkaTemplate} configurado em {@code KafkaConfig}.
 *
 * @author Danilo Fernando
 */
public interface CozinhaEventPublisher {

    /**
     * Publica o evento {@code pedido.em-preparo}.
     *
     * @param event payload com identificadores e timestamp
     */
    void publicarEmPreparo(PedidoEmPreparoEvent event);

    /**
     * Publica o evento {@code pedido.pronto}.
     *
     * @param event payload com identificadores e timestamp
     */
    void publicarPronto(PedidoProntoEvent event);
}
