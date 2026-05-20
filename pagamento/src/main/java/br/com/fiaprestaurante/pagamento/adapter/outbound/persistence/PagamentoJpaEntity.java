package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA que materializa um {@link br.com.fiaprestaurante.pagamento.domain.entity.Pagamento}
 * no banco de dados.
 *
 * <p>Vive na camada de adapter de saída — não é referenciada por domain ou
 * application; a conversão é feita pelo {@link PagamentoMapper}.
 *
 * <p>Detalhes de schema:
 * <ul>
 *   <li>Tabela {@code pagamentos};</li>
 *   <li>{@code id} e {@code pedido_id} armazenados como {@code BINARY(16)}
 *       para economia de espaço (UUID compacto);</li>
 *   <li>Índice único em {@code pedido_id} garante a relação 1-para-1 com
 *       o pedido e suporta a busca rápida do use case;</li>
 *   <li>Índice em {@code status} acelera o query do worker de
 *       reprocessamento ({@code findByStatus...}).</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
@Entity
@Table(
        name = "pagamentos",
        indexes = {
                @Index(name = "idx_pagamentos_pedido_id", columnList = "pedido_id", unique = true),
                @Index(name = "idx_pagamentos_status", columnList = "status")
        }
)
public class PagamentoJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "pedido_id", nullable = false, columnDefinition = "BINARY(16)", unique = true)
    private UUID pedidoId;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPagamento status;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    @Column(name = "motivo_falha", length = 500)
    private String motivoFalha;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Construtor sem argumentos exigido por JPA — não usar diretamente. */
    protected PagamentoJpaEntity() {
    }

    /**
     * Construtor completo usado pelo {@link PagamentoMapper}.
     *
     * @param id          identidade do pagamento
     * @param pedidoId    identificador do pedido
     * @param valor       valor monetário
     * @param status      status atual
     * @param tentativas  contador de tentativas
     * @param motivoFalha mensagem da última falha (pode ser {@code null})
     * @param createdAt   instante de criação
     * @param updatedAt   instante da última atualização
     */
    public PagamentoJpaEntity(UUID id,
                              UUID pedidoId,
                              BigDecimal valor,
                              StatusPagamento status,
                              int tentativas,
                              String motivoFalha,
                              Instant createdAt,
                              Instant updatedAt) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = status;
        this.tentativas = tentativas;
        this.motivoFalha = motivoFalha;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** @return identidade do pagamento */
    public UUID getId() {
        return id;
    }

    /** @return identificador do pedido associado */
    public UUID getPedidoId() {
        return pedidoId;
    }

    /** @return valor monetário do pagamento */
    public BigDecimal getValor() {
        return valor;
    }

    /** @return status atual do pagamento */
    public StatusPagamento getStatus() {
        return status;
    }

    /** @return número de chamadas já feitas ao gateway externo */
    public int getTentativas() {
        return tentativas;
    }

    /** @return descrição da última falha ou {@code null} */
    public String getMotivoFalha() {
        return motivoFalha;
    }

    /** @return instante de criação original */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** @return instante da última modificação */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
