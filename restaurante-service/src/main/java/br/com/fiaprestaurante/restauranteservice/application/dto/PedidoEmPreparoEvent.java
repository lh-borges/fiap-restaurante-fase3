package br.com.fiaprestaurante.restauranteservice.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado no topico Kafka {@code pedido.em-preparo} quando a
 * cozinha inicia o preparo de um pedido.
 *
 * <p>Consumido pelo {@code restaurante-pedido} para atualizar o status do
 * {@code Pedido} principal para {@code EM_PREPARO}.
 *
 * @param pedidoId        identidade do pedido original (no restaurante-pedido)
 * @param pedidoCozinhaId identidade interna na cozinha
 * @param restauranteId   identificador do restaurante
 * @param timestamp       instante em que o preparo foi iniciado
 *
 * @author Danilo Fernando
 */
public record PedidoEmPreparoEvent(
        UUID pedidoId,
        UUID pedidoCozinhaId,
        UUID restauranteId,
        Instant timestamp
) {
}
