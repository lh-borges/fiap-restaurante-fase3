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

@Component
public class PedidoCriadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCriadoConsumer.class);

    private final ProcessarPagamentoUseCase processarPagamento;

    public PedidoCriadoConsumer(ProcessarPagamentoUseCase processarPagamento) {
        this.processarPagamento = processarPagamento;
    }

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
