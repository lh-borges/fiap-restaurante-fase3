package br.com.fiaprestaurante.restauranteservice.domain.entity;

import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import br.com.fiaprestaurante.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entidade central do dominio da cozinha — espelha um pedido aprovado
 * pronto para ser preparado.
 *
 * <p>Cada {@code PedidoCozinha} eh criado a partir do evento Kafka
 * {@code pedido.pronto-para-cozinha}, emitido pelo {@code restaurante-pedido}
 * quando o pagamento eh confirmado.
 *
 * <p>Vida do agregado: {@link StatusCozinha#RECEBIDO} ->
 * {@link StatusCozinha#EM_PREPARO} -> {@link StatusCozinha#PRONTO}.
 * Transicoes sao disparadas via mutations GraphQL pelo perfil DONO_RESTAURANTE
 * e geram eventos Kafka que o {@code restaurante-pedido} consome para refletir
 * no status do {@code Pedido} principal.
 *
 * <p>Esta classe eh pura: <strong>nao conhece JPA, Spring, Kafka ou qualquer
 * framework</strong>. Persistencia eh responsabilidade do adapter
 * {@code PedidoCozinhaRepositoryAdapter} via {@code PedidoCozinhaMapper}.
 *
 * @author Danilo Fernando
 */
public class PedidoCozinha {

    private final UUID id;
    private final UUID pedidoId;
    private final UUID restauranteId;
    private final List<ItemCozinha> itens;
    private StatusCozinha status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant iniciadoEm;
    private Instant finalizadoEm;

    /**
     * Cria um novo {@code PedidoCozinha} no status {@link StatusCozinha#RECEBIDO}.
     * Usado pelo consumer Kafka ao receber {@code pedido.pronto-para-cozinha}.
     *
     * @param pedidoId       identificador do pedido original no restaurante-pedido; obrigatorio
     * @param restauranteId  identificador do restaurante; obrigatorio
     * @param itens          lista de itens a preparar; obrigatoria e nao-vazia
     * @throws BusinessException se algum invariante for violado
     */
    public PedidoCozinha(UUID pedidoId, UUID restauranteId, List<ItemCozinha> itens) {
        if (pedidoId == null) {
            throw new BusinessException("pedidoId eh obrigatorio");
        }
        if (restauranteId == null) {
            throw new BusinessException("restauranteId eh obrigatorio");
        }
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("pedido deve conter ao menos um item");
        }
        Instant agora = Instant.now();
        this.id = UUID.randomUUID();
        this.pedidoId = pedidoId;
        this.restauranteId = restauranteId;
        this.itens = List.copyOf(itens);
        this.status = StatusCozinha.RECEBIDO;
        this.createdAt = agora;
        this.updatedAt = agora;
    }

    /**
     * Construtor de hidratacao — usado pelo adapter de persistencia ao
     * reconstruir a entidade a partir do banco. Nao aplica validacoes.
     */
    public PedidoCozinha(UUID id,
                         UUID pedidoId,
                         UUID restauranteId,
                         List<ItemCozinha> itens,
                         StatusCozinha status,
                         Instant createdAt,
                         Instant updatedAt,
                         Instant iniciadoEm,
                         Instant finalizadoEm) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.restauranteId = restauranteId;
        this.itens = itens == null ? List.of() : List.copyOf(itens);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.iniciadoEm = iniciadoEm;
        this.finalizadoEm = finalizadoEm;
    }

    /**
     * Transita {@link StatusCozinha#RECEBIDO} -> {@link StatusCozinha#EM_PREPARO}.
     *
     * @throws BusinessException se o status atual nao for RECEBIDO
     */
    public void iniciarPreparo() {
        if (this.status != StatusCozinha.RECEBIDO) {
            throw new BusinessException("pedido nao pode iniciar preparo no status " + this.status);
        }
        Instant agora = Instant.now();
        this.status = StatusCozinha.EM_PREPARO;
        this.iniciadoEm = agora;
        this.updatedAt = agora;
    }

    /**
     * Transita {@link StatusCozinha#EM_PREPARO} -> {@link StatusCozinha#PRONTO}.
     *
     * @throws BusinessException se o status atual nao for EM_PREPARO
     */
    public void marcarComoPronto() {
        if (this.status != StatusCozinha.EM_PREPARO) {
            throw new BusinessException("pedido nao pode ser marcado como pronto no status " + this.status);
        }
        Instant agora = Instant.now();
        this.status = StatusCozinha.PRONTO;
        this.finalizadoEm = agora;
        this.updatedAt = agora;
    }

    /** @return identidade unica do pedido na cozinha */
    public UUID getId() {
        return id;
    }

    /** @return identificador do pedido original no restaurante-pedido */
    public UUID getPedidoId() {
        return pedidoId;
    }

    /** @return identificador do restaurante */
    public UUID getRestauranteId() {
        return restauranteId;
    }

    /** @return lista imutavel dos itens */
    public List<ItemCozinha> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /** @return status atual */
    public StatusCozinha getStatus() {
        return status;
    }

    /** @return instante da criacao */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** @return instante da ultima modificacao */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** @return instante em que o preparo foi iniciado, ou {@code null} */
    public Instant getIniciadoEm() {
        return iniciadoEm;
    }

    /** @return instante em que o pedido ficou pronto, ou {@code null} */
    public Instant getFinalizadoEm() {
        return finalizadoEm;
    }
}
