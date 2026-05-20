package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dados de um item recebido na criação de um pedido (sub-comando de
 * {@link CriarPedidoCommand}).
 *
 * <p>Reflete fielmente a estrutura especificada no requisito 4.2 (id do
 * produto, nome, quantidade, preço). A validação dos invariantes é feita ao
 * construir o {@link br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido}
 * correspondente.
 *
 * @param produtoId  identificador do produto
 * @param nome       nome do produto
 * @param quantidade quantidade desejada (deve ser maior que zero)
 * @param preco      preço unitário (deve ser positivo)
 *
 * @author Danilo Fernando
 */
public record ItemPedidoCommand(UUID produtoId, String nome, int quantidade, BigDecimal preco) {
}
