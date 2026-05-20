package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado no tópico Kafka {@code pedido.criado} quando um pedido é
 * confirmado pelo cliente — atende ao requisito 5.3 da fase 3.
 *
 * <p>É consumido pelo microsserviço {@code pagamento}, que dispara o fluxo
 * de processamento contra o gateway externo.
 *
 * <p>O payload contém apenas o mínimo necessário ao consumer (id, valor,
 * cliente). Dados detalhados (lista de itens, restaurante) ficam na entidade
 * persistida e são consultáveis via GraphQL.
 *
 * @param pedidoId   identificador do pedido
 * @param clienteId  identificador do cliente
 * @param valorTotal valor total calculado
 * @param timestamp  instante da publicação
 *
 * @author Danilo Fernando
 */
public record PedidoCriadoEvent(UUID pedidoId, UUID clienteId, BigDecimal valorTotal, Instant timestamp) {
}
