package br.com.fiaprestaurante.pagamento.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado no tópico Kafka {@code pagamento.pendente} quando o
 * gateway externo está indisponível (falha de rede, timeout ou circuit
 * breaker aberto) e o pagamento ficou aguardando reprocessamento.
 *
 * <p>É consumido pelo serviço {@code restaurante-pedido} para marcar o pedido
 * como PENDENTE_PAGAMENTO (requisito 4.5) e também pelo próprio worker de
 * reprocessamento do {@code pagamento} no próximo ciclo.
 *
 * @param pedidoId    identificador do pedido pendente
 * @param pagamentoId identidade do registro de pagamento criado/atualizado
 * @param motivo      descrição da falha mais recente do gateway
 * @param timestamp   instante em que o pendente foi registrado
 *
 * @author Danilo Fernando
 */
public record PagamentoPendenteEvent(UUID pedidoId, UUID pagamentoId, String motivo, Instant timestamp) {
}
