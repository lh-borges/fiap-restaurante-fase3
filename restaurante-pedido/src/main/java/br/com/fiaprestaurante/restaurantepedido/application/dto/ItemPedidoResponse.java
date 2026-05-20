package br.com.fiaprestaurante.restaurantepedido.application.dto;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO imutável de saída de um {@link ItemPedido} — usado nas respostas GraphQL.
 *
 * @param produtoId  identificador do produto
 * @param nome       nome do produto (snapshot)
 * @param quantidade quantidade
 * @param preco      preço unitário (snapshot)
 * @param subtotal   subtotal pré-calculado ({@code preço × quantidade})
 *
 * @author Danilo Fernando
 */
public record ItemPedidoResponse(UUID produtoId, String nome, int quantidade, BigDecimal preco, BigDecimal subtotal) {

    /**
     * Constrói um {@code ItemPedidoResponse} a partir de uma entidade de domínio.
     *
     * @param item entidade fonte; não pode ser {@code null}
     * @return DTO contendo os mesmos dados em forma imutável
     */
    public static ItemPedidoResponse from(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getProdutoId(),
                item.getNome(),
                item.getQuantidade(),
                item.getPreco(),
                item.subtotal()
        );
    }
}
