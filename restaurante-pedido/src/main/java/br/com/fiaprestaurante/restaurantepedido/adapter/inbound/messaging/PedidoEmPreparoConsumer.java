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
 * Consumer Kafka do topico {@code pedido.em-preparo} — publicado pelo
 * {@code restaurante-service} quando a cozinha inicia o preparo.
 *
 * <p>Payload esperado:
 * <pre>{@code
 * {
 *   "pedidoId":        "<uuid>",
 *   "pedidoCozinhaId": "<uuid>",
 *   "restauranteId":   "<uuid>",
 *   "timestamp":       "<ISO-8601>"
 * }
 * }</pre>
 *
 * @author Danilo Fernando
 */
@Component
public class PedidoEmPreparoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEmPreparoConsumer.class);

    private final AtualizarStatusCozinhaUseCase atualizarStatusCozinha;

    public PedidoEmPreparoConsumer(AtualizarStatusCozinhaUseCase atualizarStatusCozinha) {
        this.atualizarStatusCozinha = atualizarStatusCozinha;
    }

    @KafkaListener(topics = "${pedido.topics.pedido-em-preparo}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(Map<String, Object> event) {
        log.info("Evento recebido em pedido.em-preparo: {}", event);
        try {
            UUID pedidoId = UUID.fromString(String.valueOf(event.get("pedidoId")));
            atualizarStatusCozinha.marcarComoEmPreparo(pedidoId);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Payload invalido em pedido.em-preparo, descartando: {} | erro={}", event, e.getMessage());
        } catch (PedidoNaoEncontradoException e) {
            log.warn("Pedido nao encontrado para pedido.em-preparo, descartando: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao processar pedido.em-preparo: {}", e.getMessage(), e);
            throw e;
        }
    }
}
