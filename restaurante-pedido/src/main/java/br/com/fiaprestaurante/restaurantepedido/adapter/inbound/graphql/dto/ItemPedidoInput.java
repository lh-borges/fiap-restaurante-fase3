package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Input GraphQL de um item ao criar um pedido — espelha o tipo
 * {@code ItemPedidoInput} declarado em {@code schema.graphqls}.
 *
 * <p>É convertido para {@link br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoCommand}
 * pelo controller antes de chamar o use case.
 *
 * @param produtoId  identificador do produto (string UUID na requisição)
 * @param nome       nome do produto
 * @param quantidade quantidade
 * @param preco      preço unitário
 *
 * @author Danilo Fernando
 */
public record ItemPedidoInput(UUID produtoId, String nome, int quantidade, BigDecimal preco) {
}
