package br.com.fiaprestaurante.pagamento.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado no tópico Kafka {@code pagamento.aprovado} quando o
 * gateway externo autoriza com sucesso uma cobrança.
 *
 * <p>É consumido pelo serviço {@code restaurante-pedido}, que atualiza o
 * status do pedido correspondente para PAGO (requisito 4.7 da fase 3).
 *
 * @param pedidoId    identificador do pedido aprovado
 * @param pagamentoId identidade do registro de pagamento criado/atualizado
 * @param timestamp   instante em que a aprovação foi confirmada
 *
 * @author Danilo Fernando
 */
public record PagamentoAprovadoEvent(UUID pedidoId, UUID pagamentoId, Instant timestamp) {
}
