package br.com.fiaprestaurante.restauranteservice.adapter.inbound.messaging;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoParaCozinhaEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.input.ReceberPedidoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consumer Kafka do topico {@code pedido.pronto-para-cozinha} — publicado
 * pelo {@code restaurante-pedido} apos a confirmacao do pagamento.
 *
 * <p>Payload esperado:
 * <pre>{@code
 * {
 *   "pedidoId":      "<uuid>",
 *   "restauranteId": "<uuid>",
 *   "itens": [
 *     { "produtoId": "<uuid>", "nome": "<string>", "quantidade": <int> },
 *     ...
 *   ],
 *   "timestamp":     "<ISO-8601>"
 * }
 * }</pre>
 *
 * <p>Estrategia de erro identica ao {@code PagamentoAprovadoConsumer} do
 * restaurante-pedido: payload invalido eh descartado com log; demais falhas
 * propagam para o Spring Kafka aplicar retry.
 *
 * @author Danilo Fernando
 */
@Component
public class PedidoProntoParaCozinhaConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoProntoParaCozinhaConsumer.class);

    private final ReceberPedidoUseCase receberPedido;

    public PedidoProntoParaCozinhaConsumer(ReceberPedidoUseCase receberPedido) {
        this.receberPedido = receberPedido;
    }

    /**
     * Recebe e processa um evento {@code pedido.pronto-para-cozinha}.
     *
     * @param event payload Kafka deserializado em {@link Map}
     */
    @KafkaListener(topics = "${cozinha.topics.pedido-pronto-para-cozinha}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consumir(Map<String, Object> event) {
        log.info("Evento recebido em pedido.pronto-para-cozinha: {}", event);
        try {
            UUID pedidoId = UUID.fromString(String.valueOf(event.get("pedidoId")));
            UUID restauranteId = UUID.fromString(String.valueOf(event.get("restauranteId")));
            List<PedidoProntoParaCozinhaEvent.Item> itens = extrairItens(event.get("itens"));
            Instant timestamp = parseTimestamp(event.get("timestamp"));
            receberPedido.executar(new PedidoProntoParaCozinhaEvent(pedidoId, restauranteId, itens, timestamp));
        } catch (IllegalArgumentException | NullPointerException | ClassCastException e) {
            log.error("Payload invalido em pedido.pronto-para-cozinha, descartando: {} | erro={}",
                    event, e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao processar pedido.pronto-para-cozinha: {}", e.getMessage(), e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private List<PedidoProntoParaCozinhaEvent.Item> extrairItens(Object raw) {
        if (!(raw instanceof List<?> rawList)) {
            throw new IllegalArgumentException("Campo 'itens' ausente ou nao eh lista");
        }
        List<PedidoProntoParaCozinhaEvent.Item> result = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
            Map<String, Object> itemMap = (Map<String, Object>) o;
            UUID produtoId = UUID.fromString(String.valueOf(itemMap.get("produtoId")));
            String nome = String.valueOf(itemMap.get("nome"));
            int quantidade = ((Number) itemMap.get("quantidade")).intValue();
            result.add(new PedidoProntoParaCozinhaEvent.Item(produtoId, nome, quantidade));
        }
        return result;
    }

    private Instant parseTimestamp(Object raw) {
        if (raw == null) {
            return Instant.now();
        }
        try {
            return Instant.parse(String.valueOf(raw));
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
