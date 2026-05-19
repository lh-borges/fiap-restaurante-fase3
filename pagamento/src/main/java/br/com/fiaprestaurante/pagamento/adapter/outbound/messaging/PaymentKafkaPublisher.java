package br.com.fiaprestaurante.pagamento.adapter.outbound.messaging;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoAprovadoEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoPendenteEvent;
import br.com.fiaprestaurante.pagamento.application.port.output.PaymentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentKafkaPublisher implements PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaPublisher.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String topicAprovado;
    private final String topicPendente;

    public PaymentKafkaPublisher(KafkaTemplate<Object, Object> kafkaTemplate,
                                 @Value("${pagamento.topics.pagamento-aprovado}") String topicAprovado,
                                 @Value("${pagamento.topics.pagamento-pendente}") String topicPendente) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicAprovado = topicAprovado;
        this.topicPendente = topicPendente;
    }

    @Override
    public void publicarPagamentoAprovado(PagamentoAprovadoEvent event) {
        log.info("Publicando {} no tópico {}: {}", event.getClass().getSimpleName(), topicAprovado, event);
        kafkaTemplate.send(topicAprovado, event.pedidoId().toString(), event);
    }

    @Override
    public void publicarPagamentoPendente(PagamentoPendenteEvent event) {
        log.info("Publicando {} no tópico {}: {}", event.getClass().getSimpleName(), topicPendente, event);
        kafkaTemplate.send(topicPendente, event.pedidoId().toString(), event);
    }
}
