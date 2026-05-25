package br.com.fiaprestaurante.restauranteservice.adapter.outbound.messaging;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoEmPreparoEvent;
import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.output.CozinhaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter de saida que publica eventos da cozinha em topicos Kafka.
 *
 * <p>Implementa {@link CozinhaEventPublisher} usando o {@link KafkaTemplate}
 * configurado em {@code KafkaConfig}. A chave da mensagem eh o
 * {@code pedidoId.toString()}, garantindo ordenacao por particao.
 *
 * @author Danilo Fernando
 */
@Component
public class CozinhaKafkaPublisher implements CozinhaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CozinhaKafkaPublisher.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topicEmPreparo;
    private final String topicPronto;

    public CozinhaKafkaPublisher(KafkaTemplate<Object, Object> kafkaTemplate,
                                 @Value("${cozinha.topics.pedido-em-preparo}") String topicEmPreparo,
                                 @Value("${cozinha.topics.pedido-pronto}") String topicPronto) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicEmPreparo = topicEmPreparo;
        this.topicPronto = topicPronto;
    }

    /** {@inheritDoc} */
    @Override
    public void publicarEmPreparo(PedidoEmPreparoEvent event) {
        log.info("Publicando PedidoEmPreparoEvent no topico {}: {}", topicEmPreparo, event);
        kafkaTemplate.send(topicEmPreparo, event.pedidoId().toString(), event);
    }

    /** {@inheritDoc} */
    @Override
    public void publicarPronto(PedidoProntoEvent event) {
        log.info("Publicando PedidoProntoEvent no topico {}: {}", topicPronto, event);
        kafkaTemplate.send(topicPronto, event.pedidoId().toString(), event);
    }
}
