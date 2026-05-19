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

    protected PagamentoJpaEntity() {
    }

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

    public UUID getId() {
        return id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public int getTentativas() {
        return tentativas;
    }

    public String getMotivoFalha() {
        return motivoFalha;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
