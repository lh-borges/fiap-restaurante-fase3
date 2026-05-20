package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusPagamentoUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Consumer Kafka do tópico {@code pagamento.pendente} — atende ao requisito
 * 4.5 da fase 3 (marcar pedido como PENDENTE_PAGAMENTO quando o gateway está
 * indisponível).
 *
 * <p>Payload esperado:
 * <pre>{@code
 * {
 *   "pedidoId":   "<uuid>",
 *   "pagamentoId":"<uuid>",
 *   "motivo":     "<string>",
 *   "timestamp":  "<ISO-8601>"
 * }
 * }</pre>
 *
 * @author Danilo Fernando
 */
@Component
public class PagamentoPendenteConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagamentoPendenteConsumer.class);

    private final AtualizarStatusPagamentoUseCase atualizarStatusPagamento;

    /**
     * @param atualizarStatusPagamento porta de entrada que aplica a transição de status
     */
    public PagamentoPendenteConsumer(AtualizarStatusPagamentoUseCase atualizarStatusPagamento) {
        this.atualizarStatusPagamento = atualizarStatusPagamento;
    }

    /**
     * Recebe e processa um evento {@code pagamento.pendente}.
     *
     * <p>Estratégia de erro idêntica à do {@link PagamentoAprovadoConsumer}:
     * payload inválido e pedido inexistente são descartados; demais falhas
     * propagam.
     *
     * @param event payload Kafka deserializado em {@link Map}
     */
    @KafkaListener(topics = "${pedido.topics.pagamento-pendente}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(Map<String, Object> event) {
        log.info("Evento recebido em pagamento.pendente: {}", event);
        try {
            UUID pedidoId = UUID.fromString(String.valueOf(event.get("pedidoId")));
            String motivo = String.valueOf(event.getOrDefault("motivo", "gateway indisponível"));
            atualizarStatusPagamento.marcarComoPendente(pedidoId, motivo);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Payload inválido em pagamento.pendente, descartando: {} | erro={}", event, e.getMessage());
        } catch (PedidoNaoEncontradoException e) {
            log.warn("Pedido não encontrado para pagamento.pendente, descartando: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao processar pagamento.pendente: {}", e.getMessage(), e);
            throw e;
        }
    }
}
