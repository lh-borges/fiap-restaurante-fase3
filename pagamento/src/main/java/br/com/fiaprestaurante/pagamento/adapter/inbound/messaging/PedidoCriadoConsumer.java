package br.com.fiaprestaurante.pagamento.adapter.inbound.messaging;

import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;
import br.com.fiaprestaurante.pagamento.application.port.input.ProcessarPagamentoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Consumer Kafka do tópico {@code pedido.criado} — ponto de entrada
 * assíncrono do microsserviço.
 *
 * <p>Atende o requisito 5.3 da fase 3: quando o {@code restaurante-pedido}
 * publica um novo pedido, este consumer dispara o fluxo de processamento
 * de pagamento.
 *
 * <p>Payload esperado (campos mínimos):
 * <pre>{@code
 * {
 *   "pedidoId":   "<uuid>",
 *   "valorTotal": <decimal>
 *   // outros campos como clienteId, restauranteId são ignorados se presentes
 * }
 * }</pre>
 *
 * @author Danilo Fernando
 */
@Component
public class PedidoCriadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCriadoConsumer.class);

    private final ProcessarPagamentoUseCase processarPagamento;

    /**
     * @param processarPagamento porta de entrada que executa a lógica de pagamento
     */
    public PedidoCriadoConsumer(ProcessarPagamentoUseCase processarPagamento) {
        this.processarPagamento = processarPagamento;
    }

    /**
     * Recebe e processa um evento {@code pedido.criado}.
     *
     * <p>Estratégia de erro:
     * <ul>
     *   <li>Payload inválido (campos faltando ou formato errado) é
     *       <strong>descartado</strong> com log de erro — não faz sentido
     *       reprocessar, o produtor enviou dado quebrado;</li>
     *   <li>Falha do gateway ou de persistência <strong>propaga</strong> —
     *       o Spring Kafka aplicará retry/DLQ conforme configurado.</li>
     * </ul>
     *
     * @param event payload Kafka deserializado em {@link Map} (Jackson JSR310 já registrado)
     */
    @KafkaListener(topics = "${pagamento.topics.pedido-criado}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(Map<String, Object> event) {
        log.info("Evento recebido em pedido.criado: {}", event);
        try {
            UUID pedidoId = UUID.fromString(String.valueOf(event.get("pedidoId")));
            BigDecimal valorTotal = new BigDecimal(String.valueOf(event.get("valorTotal")));

            var response = processarPagamento.executar(new ProcessarPagamentoCommand(pedidoId, valorTotal));
            log.info("Pagamento processado: pedidoId={} status={}", pedidoId, response.status());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Payload inválido em pedido.criado, descartando: {} | erro={}", event, e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao processar pedido.criado: {}", e.getMessage(), e);
            throw e;
        }
    }
}
