package br.com.fiaprestaurante.restauranteservice.adapter.outbound.persistence;

import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA que materializa um
 * {@link br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha}
 * no banco {@code cozinha_db}.
 *
 * <p>Vive no adapter de saida — nao eh referenciada por domain ou application;
 * a conversao eh feita pelo {@link PedidoCozinhaMapper}.
 *
 * @author Danilo Fernando
 */
@Entity
@Table(
        name = "pedidos_cozinha",
        indexes = {
                @Index(name = "idx_pedidos_cozinha_pedido_id", columnList = "pedido_id"),
                @Index(name = "idx_pedidos_cozinha_status", columnList = "status"),
                @Index(name = "idx_pedidos_cozinha_restaurante_id", columnList = "restaurante_id")
        }
)
public class PedidoCozinhaJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "pedido_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID pedidoId;

    @Column(name = "restaurante_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID restauranteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusCozinha status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "iniciado_em")
    private Instant iniciadoEm;

    @Column(name = "finalizado_em")
    private Instant finalizadoEm;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_cozinha_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<ItemCozinhaJpaEntity> itens = new ArrayList<>();

    /** Construtor sem argumentos exigido por JPA — nao usar diretamente. */
    protected PedidoCozinhaJpaEntity() {
    }

    public PedidoCozinhaJpaEntity(UUID id,
                                  UUID pedidoId,
                                  UUID restauranteId,
                                  StatusCozinha status,
                                  Instant createdAt,
                                  Instant updatedAt,
                                  Instant iniciadoEm,
                                  Instant finalizadoEm,
                                  List<ItemCozinhaJpaEntity> itens) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.restauranteId = restauranteId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.iniciadoEm = iniciadoEm;
        this.finalizadoEm = finalizadoEm;
        this.itens = itens == null ? new ArrayList<>() : new ArrayList<>(itens);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public UUID getRestauranteId() {
        return restauranteId;
    }

    public StatusCozinha getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getIniciadoEm() {
        return iniciadoEm;
    }

    public Instant getFinalizadoEm() {
        return finalizadoEm;
    }

    public List<ItemCozinhaJpaEntity> getItens() {
        return itens;
    }
}
