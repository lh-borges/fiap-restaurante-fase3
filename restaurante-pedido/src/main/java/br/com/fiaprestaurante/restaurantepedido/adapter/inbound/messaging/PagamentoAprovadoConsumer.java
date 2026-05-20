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
 * Consumer Kafka do tópico {@code pagamento.aprovado} — atende ao requisito
 * 4.6/4.7 da fase 3 (atualização automática para PAGO).
 *
 * <p>Payload esperado:
 * <pre>{@code
 * {
 *   "pedidoId":   "<uuid>",
 *   "pagamentoId":"<uuid>",
 *   "timestamp":  "<ISO-8601>"
 * }
 * }</pre>
 *
 * @author Danilo Fernando
 */
@Component
public class PagamentoAprovadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagamentoAprovadoConsumer.class);

    private final AtualizarStatusPagamentoUseCase atualizarStatusPagamento;

    /**
     * @param atualizarStatusPagamento porta de entrada que aplica a transição de status
     */
    public PagamentoAprovadoConsumer(AtualizarStatusPagamentoUseCase atualizarStatusPagamento) {
        this.atualizarStatusPagamento = atualizarStatusPagamento;
    }

    /**
     * Recebe e processa um evento {@code pagamento.aprovado}.
     *
     * <p>Estratégia de erro:
     * <ul>
     *   <li>Payload inválido é <strong>descartado</strong> com log de erro;</li>
     *   <li>{@link PedidoNaoEncontradoException} é <strong>descartada</strong>
     *       — pode ocorrer se o evento chegar antes da persistência local
     *       terminar; o reprocessamento do pagamento manda o evento novamente;</li>
     *   <li>Demais falhas <strong>propagam</strong> para o Spring Kafka
     *       aplicar retry/DLQ.</li>
     * </ul>
     *
     * @param event payload Kafka deserializado em {@link Map}
     */
    @KafkaListener(topics = "${pedido.topics.pagamento-aprovado}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(Map<String, Object> event) {
        log.info("Evento recebido em pagamento.aprovado: {}", event);
        try {
            UUID pedidoId = UUID.fromString(String.valueOf(event.get("pedidoId")));
            UUID pagamentoId = UUID.fromString(String.valueOf(event.get("pagamentoId")));
            atualizarStatusPagamento.marcarComoPago(pedidoId, pagamentoId);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Payload inválido em pagamento.aprovado, descartando: {} | erro={}", event, e.getMessage());
        } catch (PedidoNaoEncontradoException e) {
            log.warn("Pedido não encontrado para pagamento.aprovado, descartando: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao processar pagamento.aprovado: {}", e.getMessage(), e);
            throw e;
        }
    }
}
