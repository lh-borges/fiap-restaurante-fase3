package br.com.fiaprestaurante.restaurantepedido.application.port.output;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;

/**
 * Porta de saída para publicação de eventos de pedido em mensageria.
 *
 * <p>Abstrai o broker (Kafka, RabbitMQ, etc.) do use case. A implementação
 * padrão é {@code PedidoKafkaPublisher} e publica no tópico
 * {@code pedido.criado}, conforme o requisito 5.3 da fase 3.
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
}
