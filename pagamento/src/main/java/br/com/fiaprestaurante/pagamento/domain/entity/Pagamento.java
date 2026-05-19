package br.com.fiaprestaurante.pagamento.domain.entity;

import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import br.com.fiaprestaurante.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Pagamento {

    private final UUID id;
    private final UUID pedidoId;
    private final BigDecimal valor;
    private StatusPagamento status;
    private int tentativas;
    private String motivoFalha;
    private final Instant createdAt;
    private Instant updatedAt;

    public Pagamento(UUID pedidoId, BigDecimal valor) {
        if (pedidoId == null) {
            throw new BusinessException("pedidoId é obrigatório");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new BusinessException("valor do pagamento deve ser positivo");
        }
        Instant agora = Instant.now();
        this.id = UUID.randomUUID();
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = StatusPagamento.PENDENTE;
        this.tentativas = 0;
        this.motivoFalha = null;
        this.createdAt = agora;
        this.updatedAt = agora;
    }

    public Pagamento(UUID id,
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

    public void aprovar() {
        if (this.status == StatusPagamento.APROVADO) {
            return;
        }
        this.status = StatusPagamento.APROVADO;
        this.motivoFalha = null;
        this.updatedAt = Instant.now();
    }

    public void marcarComoPendente(String motivo) {
        this.status = StatusPagamento.PENDENTE;
        this.motivoFalha = motivo;
        this.updatedAt = Instant.now();
    }

    public void incrementarTentativas() {
        this.tentativas++;
        this.updatedAt = Instant.now();
    }

    public boolean estaAprovado() {
        return this.status == StatusPagamento.APROVADO;
    }

    public boolean estaPendente() {
        return this.status == StatusPagamento.PENDENTE;
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
