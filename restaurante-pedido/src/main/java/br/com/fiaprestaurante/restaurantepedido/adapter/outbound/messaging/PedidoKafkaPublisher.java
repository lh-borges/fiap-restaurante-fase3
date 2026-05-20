package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter de saída que publica eventos de pedido no Kafka.
 *
 * <p>Implementa {@link PedidoEventPublisher} usando o {@link KafkaTemplate}
 * configurado em {@code KafkaConfig} com {@code JsonSerializer} +
 * {@code JavaTimeModule} (necessário para serializar {@code Instant}).
 *
 * <p>A chave da mensagem é o {@code pedidoId.toString()}, garantindo que
 * todos os eventos do mesmo pedido caem na mesma partição (ordenação
 * preservada).
 *
 * @author Danilo Fernando
 */
@Component
public class PedidoKafkaPublisher implements PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoKafkaPublisher.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topicPedidoCriado;

    /**
     * @param kafkaTemplate      template Kafka injetado pelo {@code KafkaConfig}
     * @param topicPedidoCriado  nome do tópico (vem de {@code application.properties})
     */
    public PedidoKafkaPublisher(KafkaTemplate<Object, Object> kafkaTemplate,
                                @Value("${pedido.topics.pedido-criado}") String topicPedidoCriado) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicPedidoCriado = topicPedidoCriado;
    }

    /** {@inheritDoc} */
    @Override
    public void publicarPedidoCriado(PedidoCriadoEvent event) {
        log.info("Publicando {} no tópico {}: {}", event.getClass().getSimpleName(), topicPedidoCriado, event);
        kafkaTemplate.send(topicPedidoCriado, event.pedidoId().toString(), event);
    }
}
