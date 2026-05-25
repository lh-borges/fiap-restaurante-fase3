package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusCozinhaUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Consumer Kafka do topico {@code pedido.pronto} — publicado pelo
 * {@code restaurante-service} quando a cozinha finaliza o preparo.
 *
 * <p>Payload esperado: identico ao do {@link PedidoEmPreparoConsumer}.
 *
 * @author Danilo Fernando
 */
@Component
public class PedidoProntoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoProntoConsumer.class);

    private final AtualizarStatusCozinhaUseCase atualizarStatusCozinha;

    public PedidoProntoConsumer(AtualizarStatusCozinhaUseCase atualizarStatusCozinha) {
        this.atualizarStatusCozinha = atualizarStatusCozinha;
    }

    @KafkaListener(topics = "${pedido.topics.pedido-pronto}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(Map<String, Object> event) {
        log.info("Evento recebido em pedido.pronto: {}", event);
        try {
            UUID pedidoId = UUID.fromString(String.valueOf(event.get("pedidoId")));
            atualizarStatusCozinha.marcarComoPronto(pedidoId);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Payload invalido em pedido.pronto, descartando: {} | erro={}", event, e.getMessage());
        } catch (PedidoNaoEncontradoException e) {
            log.warn("Pedido nao encontrado para pedido.pronto, descartando: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao processar pedido.pronto: {}", e.getMessage(), e);
            throw e;
        }
    }
}
