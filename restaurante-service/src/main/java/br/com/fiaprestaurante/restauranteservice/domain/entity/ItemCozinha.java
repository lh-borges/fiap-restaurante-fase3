package br.com.fiaprestaurante.restauranteservice.domain.entity;

import br.com.fiaprestaurante.shared.exception.BusinessException;

import java.util.UUID;

/**
 * Value object imutavel que representa um item na fila da cozinha.
 *
 * <p>Snapshot dos dados que a cozinha precisa enxergar: {@code produtoId},
 * {@code nome} e {@code quantidade}. <strong>Preco fica fora</strong> — eh
 * informacao do bounded context de pedido/pagamento, nao da cozinha.
 *
 * @author Danilo Fernando
 */
public class ItemCozinha {

    private final UUID produtoId;
    private final String nome;
    private final int quantidade;

    /**
     * @param produtoId  identificador do produto; obrigatorio
     * @param nome       nome do produto exibido para a cozinha; obrigatorio
     * @param quantidade quantidade a preparar; deve ser positiva
     * @throws BusinessException se algum invariante for violado
     */
    public ItemCozinha(UUID produtoId, String nome, int quantidade) {
        if (produtoId == null) {
            throw new BusinessException("produtoId eh obrigatorio");
        }
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("nome do item eh obrigatorio");
        }
        if (quantidade <= 0) {
            throw new BusinessException("quantidade deve ser positiva");
        }
        this.produtoId = produtoId;
        this.nome = nome;
        this.quantidade = quantidade;
    }

    /** @return identificador do produto */
    public UUID getProdutoId() {
        return produtoId;
    }

    /** @return nome do produto */
    public String getNome() {
        return nome;
    }

    /** @return quantidade a preparar */
    public int getQuantidade() {
        return quantidade;
    }
}
