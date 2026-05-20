package br.com.fiaprestaurante.restaurantepedido.domain.entity;

import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
import br.com.fiaprestaurante.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entidade central do domínio de pedido.
 *
 * <p>Agrega cliente, restaurante e itens, calculando o valor total a partir
 * dos {@link ItemPedido itens}. Mantém o {@link StatusPedido status} ao longo
 * do ciclo de vida com transições controladas por métodos de negócio.
 *
 * <p>Esta classe é pura: <strong>não conhece JPA, Spring, Kafka ou qualquer
 * framework</strong>. A persistência é responsabilidade do adapter
 * {@code PedidoRepositoryAdapter} via {@code PedidoMapper}.
 *
 * <p>Regras de negócio expressas como métodos:
 * <ul>
 *   <li>{@link #confirmar()} — transita CRIADO → CONFIRMADO;</li>
 *   <li>{@link #marcarComoPendentePagamento(String)} — gateway indisponível;</li>
 *   <li>{@link #marcarComoPago(UUID)} — pagamento aprovado pelo gateway;</li>
 *   <li>{@link #cancelar()} — desistência do cliente.</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
public class Pedido {

    private final UUID id;
    private final UUID clienteId;
    private final UUID restauranteId;
    private final List<ItemPedido> itens;
    private final BigDecimal valorTotal;
    private StatusPedido status;
    private UUID pagamentoId;
    private String motivoPendencia;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Cria um novo pedido no status {@link StatusPedido#CRIADO}, calculando
     * automaticamente o valor total a partir dos itens.
     *
     * @param clienteId     identificador do cliente (extraído do token JWT); obrigatório
     * @param restauranteId identificador do restaurante; obrigatório
     * @param itens         lista de itens do pedido; obrigatória e não-vazia
     * @throws BusinessException se algum invariante for violado
     */
    public Pedido(UUID clienteId, UUID restauranteId, List<ItemPedido> itens) {
        if (clienteId == null) {
            throw new BusinessException("clienteId é obrigatório");
        }
        if (restauranteId == null) {
            throw new BusinessException("restauranteId é obrigatório");
        }
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("pedido deve conter ao menos um item");
        }
        Instant agora = Instant.now();
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.itens = List.copyOf(itens);
        this.valorTotal = this.itens.stream()
                .map(ItemPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.status = StatusPedido.CRIADO;
        this.createdAt = agora;
        this.updatedAt = agora;
    }

    /**
     * Construtor de hidratação — usado pelo adapter de persistência ao
     * reconstruir a entidade a partir do banco de dados.
     *
     * <p>Não aplica validações; assume que os dados vêm de uma fonte confiável.
     *
     * @param id              identidade do pedido
     * @param clienteId       identificador do cliente
     * @param restauranteId   identificador do restaurante
     * @param itens           lista de itens
     * @param valorTotal      valor total previamente calculado
     * @param status          status atual
     * @param pagamentoId     identidade do pagamento associado (ou {@code null})
     * @param motivoPendencia descrição da última pendência (ou {@code null})
     * @param createdAt       instante da criação original
     * @param updatedAt       instante da última modificação
     */
    public Pedido(UUID id,
                  UUID clienteId,
                  UUID restauranteId,
                  List<ItemPedido> itens,
                  BigDecimal valorTotal,
                  StatusPedido status,
                  UUID pagamentoId,
                  String motivoPendencia,
                  Instant createdAt,
                  Instant updatedAt) {
        this.id = id;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.itens = itens == null ? List.of() : List.copyOf(itens);
        this.valorTotal = valorTotal;
        this.status = status;
        this.pagamentoId = pagamentoId;
        this.motivoPendencia = motivoPendencia;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Confirma o pedido. Só é válido quando o status atual for {@link StatusPedido#CRIADO}.
     *
     * @throws BusinessException se o pedido já tiver sido confirmado, pago ou cancelado
     */
    public void confirmar() {
        if (this.status != StatusPedido.CRIADO) {
            throw new BusinessException("pedido não pode ser confirmado no status " + this.status);
        }
        this.status = StatusPedido.CONFIRMADO;
        this.updatedAt = Instant.now();
    }

    /**
     * Marca o pedido como {@link StatusPedido#PENDENTE_PAGAMENTO} quando o
     * gateway externo está indisponível (requisito 4.5).
     *
     * <p>Idempotente: se já estiver PAGO ou CANCELADO, nada acontece — evita
     * sobrescrever estado terminal devido a evento Kafka tardio.
     *
     * @param motivo descrição da pendência (logada e armazenada)
     */
    public void marcarComoPendentePagamento(String motivo) {
        if (this.status == StatusPedido.PAGO || this.status == StatusPedido.CANCELADO) {
            return;
        }
        this.status = StatusPedido.PENDENTE_PAGAMENTO;
        this.motivoPendencia = motivo;
        this.updatedAt = Instant.now();
    }

    /**
     * Marca o pedido como {@link StatusPedido#PAGO} após confirmação do
     * gateway de pagamento (requisitos 4.6 e 4.7).
     *
     * <p>Idempotente: chamadas subsequentes com o mesmo {@code pagamentoId}
     * não modificam o estado.
     *
     * @param pagamentoId identidade do pagamento que aprovou a cobrança
     */
    public void marcarComoPago(UUID pagamentoId) {
        if (this.status == StatusPedido.PAGO) {
            return;
        }
        if (this.status == StatusPedido.CANCELADO) {
            throw new BusinessException("pedido cancelado não pode ser marcado como pago");
        }
        this.status = StatusPedido.PAGO;
        this.pagamentoId = pagamentoId;
        this.motivoPendencia = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Cancela o pedido. Não permitido se já estiver pago.
     *
     * @throws BusinessException se o pedido já estiver no status {@link StatusPedido#PAGO}
     */
    public void cancelar() {
        if (this.status == StatusPedido.PAGO) {
            throw new BusinessException("pedido pago não pode ser cancelado");
        }
        this.status = StatusPedido.CANCELADO;
        this.updatedAt = Instant.now();
    }

    /** @return identidade única do pedido */
    public UUID getId() {
        return id;
    }

    /** @return identificador do cliente (extraído do JWT no momento da criação) */
    public UUID getClienteId() {
        return clienteId;
    }

    /** @return identificador do restaurante */
    public UUID getRestauranteId() {
        return restauranteId;
    }

    /** @return lista imutável dos itens do pedido */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /** @return valor total calculado a partir dos itens */
    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    /** @return status atual do pedido */
    public StatusPedido getStatus() {
        return status;
    }

    /** @return identidade do pagamento associado, ou {@code null} se ainda não pago */
    public UUID getPagamentoId() {
        return pagamentoId;
    }

    /** @return motivo da última pendência, ou {@code null} se não houver */
    public String getMotivoPendencia() {
        return motivoPendencia;
    }

    /** @return instante da criação original */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** @return instante da última modificação */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
