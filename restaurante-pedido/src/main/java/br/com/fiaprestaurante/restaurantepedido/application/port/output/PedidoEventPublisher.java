package br.com.fiaprestaurante.restaurantepedido.application.port.output;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoProntoParaCozinhaEvent;

/**
 * Porta de saída para publicação de eventos de pedido em mensageria.
 *
 * <p>Abstrai o broker (Kafka, RabbitMQ, etc.) do use case. A implementação
 * padrão é {@code PedidoKafkaPublisher} e publica nos tópicos
 * {@code pedido.criado} (requisito 5.3) e {@code pedido.pronto-para-cozinha}
 * (modulo opcional 5.1, integracao com restaurante-service).
 *
 * @author Danilo Fernando
 */
public interface PedidoEventPublisher {

    /**
     * Publica o evento {@code pedido.criado} para que o microsserviço
     * {@code pagamento} dispare o processamento da cobrança.
     *
     * @param event evento contendo {@code pedidoId}, {@code clienteId}, valor e timestamp
     */
    void publicarPedidoCriado(PedidoCriadoEvent event);

    /**
     * Publica o evento {@code pedido.pronto-para-cozinha} apos a confirmacao
     * do pagamento, para que o {@code restaurante-service} (cozinha) inicie
     * o preparo.
     *
     * @param event evento contendo {@code pedidoId}, {@code restauranteId}, itens e timestamp
     */
    void publicarProntoParaCozinha(PedidoProntoParaCozinhaEvent event);
}
