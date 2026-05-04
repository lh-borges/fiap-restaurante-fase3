package br.com.fiaprestaurante.restaurantepedido.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Entidade de domínio representando um item dentro de um pedido.
 * Contém as informações do produto e a regra de negócio
 * para cálculo do subtotal do item.
 * Não possui dependências de frameworks externos.
 */
public class ItemPedido {

    private UUID produtoId;
    private String nome;
    private Integer quantidade;
    private BigDecimal preco;

    public ItemPedido(UUID produtoId, String nome, Integer quantidade, BigDecimal preco) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public BigDecimal calcularSubtotal() {
        return preco.multiply(BigDecimal.valueOf(quantidade));
    }

    // Getters
    public UUID getProdutoId() { return produtoId; }
    public String getNome() { return nome; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getPreco() { return preco; }
}
