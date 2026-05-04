package br.com.fiaprestaurante.restaurantepedido.domain.entity;

import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Entidade central do domínio representando um pedido de restaurante.
 * Contém as regras de negócio relacionadas ao ciclo de vida do pedido,
 * como cálculo do valor total e mudança de status.
 * Não possui dependências de frameworks externos.
 */
public class Pedido {

    private UUID id;
    private String clienteId;
    private String clienteNome;
    private String clienteEmail;
    private UUID restauranteId;
    private List<ItemPedido> itens;
    private BigDecimal valorTotal;
    private StatusPedido status;
    private LocalDateTime criadoEm;

    // construtor
    public Pedido(String clienteId, String clienteNome, String clienteEmail,
                  UUID restauranteId, List<ItemPedido> itens) {
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
        this.clienteEmail = clienteEmail;
        this.restauranteId = restauranteId;
        this.itens = itens;
        this.valorTotal = calcularTotal();
        this.status = StatusPedido.AGUARDANDO_CONFIRMACAO;
        this.criadoEm = LocalDateTime.now();
    }

    private BigDecimal calcularTotal() {
        return itens.stream()
                .map(ItemPedido::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirmar() {
        this.status = StatusPedido.CONFIRMADO;
    }

    public void cancelar() {
        this.status = StatusPedido.CANCELADO;
    }

    // Getters
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() { return id; }
    public String getClienteId() { return clienteId; }
    public String getClienteNome() { return clienteNome; }
    public String getClienteEmail() { return clienteEmail; }
    public UUID getRestauranteId() { return restauranteId; }
    public List<ItemPedido> getItens() { return itens; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public StatusPedido getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
