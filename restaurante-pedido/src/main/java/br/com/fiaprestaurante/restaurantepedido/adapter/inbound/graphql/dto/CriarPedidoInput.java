package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto;

import java.util.List;
import java.util.UUID;

/**
 * Input GraphQL para a mutation {@code criarPedido} — espelha o tipo
 * {@code CriarPedidoInput} declarado em {@code schema.graphqls}.
 *
 * <p>Não inclui {@code clienteId}: ele é extraído do token JWT pelo
 * controller, conforme o requisito 5.2.
 *
 * @param restauranteId identificador do restaurante onde o pedido foi feito
 * @param itens         itens que compõem o pedido (mínimo 1)
 *
 * @author Danilo Fernando
 */
public record CriarPedidoInput(UUID restauranteId, List<ItemPedidoInput> itens) {
}
