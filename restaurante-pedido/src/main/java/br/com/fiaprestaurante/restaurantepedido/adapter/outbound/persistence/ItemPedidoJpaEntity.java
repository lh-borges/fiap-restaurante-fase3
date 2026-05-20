package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade JPA que materializa um
 * {@link br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido}
 * no banco de dados.
 *
 * <p>É filha de {@link PedidoJpaEntity} via {@code @OneToMany +
 * @JoinColumn("pedido_id")}, com cascade ALL e {@code orphanRemoval = true}.
 * A chave primária ({@code id}) é gerada pelo banco (auto-incremento).
 *
 * <p>O campo {@code ordem} (gerado pelo {@code @OrderColumn} no pai) preserva
 * a sequência dos itens conforme enviados pelo cliente.
 *
 * @author Danilo Fernando
 */
@Entity
@Table(name = "pedido_itens")
public class ItemPedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "produto_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID produtoId;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "preco", nullable = false, precision = 19, scale = 2)
    private BigDecimal preco;

    /** Construtor sem argumentos exigido por JPA — não usar diretamente. */
    protected ItemPedidoJpaEntity() {
    }

    /**
     * Construtor completo usado pelo {@link PedidoMapper}.
     *
     * @param produtoId  identificador do produto
     * @param nome       nome do produto
     * @param quantidade quantidade
     * @param preco      preço unitário
     */
    public ItemPedidoJpaEntity(UUID produtoId, String nome, int quantidade, BigDecimal preco) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    /** @return identidade gerada pelo banco */
    public Long getId() {
        return id;
    }

    /** @return identificador do produto */
    public UUID getProdutoId() {
        return produtoId;
    }

    /** @return nome do produto */
    public String getNome() {
        return nome;
    }

    /** @return quantidade pedida */
    public int getQuantidade() {
        return quantidade;
    }

    /** @return preço unitário */
    public BigDecimal getPreco() {
        return preco;
    }
}
