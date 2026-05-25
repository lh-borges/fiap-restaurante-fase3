package br.com.fiaprestaurante.restauranteservice.adapter.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidade JPA dos itens da cozinha. Mantida em tabela proprio
 * ({@code itens_cozinha}) com chave artificial — sem expor identidade ao
 * dominio, que trata itens como value objects.
 *
 * @author Danilo Fernando
 */
@Entity
@Table(name = "itens_cozinha")
public class ItemCozinhaJpaEntity {

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

    /** Construtor sem argumentos exigido por JPA — nao usar diretamente. */
    protected ItemCozinhaJpaEntity() {
    }

    public ItemCozinhaJpaEntity(UUID produtoId, String nome, int quantidade) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.quantidade = quantidade;
    }

    public Long getId() {
        return id;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
