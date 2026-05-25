package br.com.fiaprestaurante.restauranteservice.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado no topico Kafka {@code pedido.pronto} quando a cozinha
 * finaliza o preparo de um pedido.
 *
 * <p>Consumido pelo {@code restaurante-pedido} para atualizar o status do
 * {@code Pedido} principal para {@code PRONTO}.
 *
 * @param pedidoId        identidade do pedido original (no restaurante-pedido)
 * @param pedidoCozinhaId identidade interna na cozinha
 * @param restauranteId   identificador do restaurante
 * @param timestamp       instante em que o pedido ficou pronto
 *
 * @author Danilo Fernando
 */
public record PedidoProntoEvent(
        UUID pedidoId,
        UUID pedidoCozinhaId,
        UUID restauranteId,
        Instant timestamp
) {
}
