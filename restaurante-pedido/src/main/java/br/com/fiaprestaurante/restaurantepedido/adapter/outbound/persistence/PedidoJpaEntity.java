package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA que materializa um {@link br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido}
 * no banco de dados.
 *
 * <p>Vive na camada de adapter de saída — não é referenciada por domain ou
 * application; a conversão é feita pelo {@link PedidoMapper}.
 *
 * <p>Detalhes de schema:
 * <ul>
 *   <li>Tabela {@code pedidos};</li>
 *   <li>{@code id}, {@code cliente_id}, {@code restaurante_id} e
 *       {@code pagamento_id} armazenados como {@code BINARY(16)} (UUID compacto);</li>
 *   <li>Índice em {@code cliente_id} acelera a listagem por cliente;</li>
 *   <li>{@code itens} mapeados como coleção JPA com cascade ALL e
 *       {@code orphanRemoval=true} — o ciclo de vida dos itens segue o do pedido.</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
@Entity
@Table(
        name = "pedidos",
        indexes = {
                @Index(name = "idx_pedidos_cliente_id", columnList = "cliente_id"),
                @Index(name = "idx_pedidos_status", columnList = "status")
        }
)
public class PedidoJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "cliente_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID clienteId;

    @Column(name = "restaurante_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID restauranteId;

    @Column(name = "valor_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "pagamento_id", columnDefinition = "BINARY(16)")
    private UUID pagamentoId;

    @Column(name = "motivo_pendencia", length = 500)
    private String motivoPendencia;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<ItemPedidoJpaEntity> itens = new ArrayList<>();

    /** Construtor sem argumentos exigido por JPA — não usar diretamente. */
    protected PedidoJpaEntity() {
    }

    /**
     * Construtor completo usado pelo {@link PedidoMapper}.
     *
     * @param id              identidade do pedido
     * @param clienteId       identificador do cliente
     * @param restauranteId   identificador do restaurante
     * @param valorTotal      valor total calculado
     * @param status          status atual
     * @param pagamentoId     identidade do pagamento (pode ser {@code null})
     * @param motivoPendencia descrição da pendência (pode ser {@code null})
     * @param createdAt       instante de criação
     * @param updatedAt       instante da última modificação
     * @param itens           lista de entidades JPA dos itens
     */
    public PedidoJpaEntity(UUID id,
                           UUID clienteId,
                           UUID restauranteId,
                           BigDecimal valorTotal,
                           StatusPedido status,
                           UUID pagamentoId,
                           String motivoPendencia,
                           Instant createdAt,
                           Instant updatedAt,
                           List<ItemPedidoJpaEntity> itens) {
        this.id = id;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.valorTotal = valorTotal;
        this.status = status;
        this.pagamentoId = pagamentoId;
        this.motivoPendencia = motivoPendencia;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.itens = itens == null ? new ArrayList<>() : new ArrayList<>(itens);
    }

    /** @return identidade do pedido */
    public UUID getId() {
        return id;
    }

    /** @return identificador do cliente */
    public UUID getClienteId() {
        return clienteId;
    }

    /** @return identificador do restaurante */
    public UUID getRestauranteId() {
        return restauranteId;
    }

    /** @return valor total calculado */
    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    /** @return status atual */
    public StatusPedido getStatus() {
        return status;
    }

    /** @return identidade do pagamento, ou {@code null} */
    public UUID getPagamentoId() {
        return pagamentoId;
    }

    /** @return motivo da pendência, ou {@code null} */
    public String getMotivoPendencia() {
        return motivoPendencia;
    }

    /** @return instante de criação */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** @return instante da última modificação */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** @return lista de entidades JPA dos itens */
    public List<ItemPedidoJpaEntity> getItens() {
        return itens;
    }
}
