package br.com.fiaprestaurante.restaurantepedido.domain.entity;

import br.com.fiaprestaurante.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item de um {@link Pedido} — produto + quantidade + preço unitário.
 *
 * <p>Entidade de domínio pura: não conhece JPA, Spring ou qualquer framework.
 * A persistência é responsabilidade dos adapters de saída.
 *
 * <p>O subtotal é calculado dinamicamente em {@link #subtotal()}; não armazenamos
 * para evitar inconsistência caso preço ou quantidade sejam alterados.
 *
 * @author Danilo Fernando
 */
public class ItemPedido {

    private final UUID produtoId;
    private final String nome;
    private final int quantidade;
    private final BigDecimal preco;

    /**
     * Cria um novo item de pedido aplicando todas as validações de negócio.
     *
     * @param produtoId  identificador do produto; obrigatório
     * @param nome       nome do produto; obrigatório e não-vazio
     * @param quantidade quantidade desejada; deve ser maior que zero
     * @param preco      preço unitário; deve ser positivo (maior que zero)
     * @throws BusinessException se qualquer um dos invariantes for violado
     */
    public ItemPedido(UUID produtoId, String nome, int quantidade, BigDecimal preco) {
        if (produtoId == null) {
            throw new BusinessException("produtoId é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("nome do produto é obrigatório");
        }
        if (quantidade <= 0) {
            throw new BusinessException("quantidade deve ser maior que zero");
        }
        if (preco == null || preco.signum() <= 0) {
            throw new BusinessException("preço deve ser positivo");
        }
        this.produtoId = produtoId;
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    /**
     * Calcula o subtotal deste item ({@code preço × quantidade}).
     *
     * @return valor monetário do item
     */
    public BigDecimal subtotal() {
        return preco.multiply(BigDecimal.valueOf(quantidade));
    }

    /** @return identificador do produto */
    public UUID getProdutoId() {
        return produtoId;
    }

    /** @return nome do produto (snapshot no momento da criação do pedido) */
    public String getNome() {
        return nome;
    }

    /** @return quantidade pedida */
    public int getQuantidade() {
        return quantidade;
    }

    /** @return preço unitário (snapshot no momento da criação do pedido) */
    public BigDecimal getPreco() {
        return preco;
    }
}
