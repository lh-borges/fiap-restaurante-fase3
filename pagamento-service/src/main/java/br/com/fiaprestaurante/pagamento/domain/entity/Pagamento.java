package br.com.fiaprestaurante.pagamento.domain.entity;

import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import br.com.fiaprestaurante.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidade central do domínio de pagamento.
 *
 * <p>Representa o registro de uma cobrança associada a um pedido, com seu
 * estado atual ({@link StatusPagamento}), número de tentativas já realizadas
 * contra o gateway externo, motivo da última falha (se houver) e marcas de
 * tempo de criação e última atualização.
 *
 * <p>Esta classe é pura: <strong>não conhece JPA, Spring, Kafka ou qualquer
 * framework</strong>. A persistência é responsabilidade do adapter
 * {@code PagamentoRepositoryAdapter} via {@code PagamentoMapper}.
 *
 * <p>Regras de negócio expressas como métodos:
 * <ul>
 *   <li>{@link #aprovar()} — transita para APROVADO e limpa o motivo de falha;</li>
 *   <li>{@link #marcarComoPendente(String)} — define motivo de falha e mantém
 *       o status PENDENTE para reprocessamento;</li>
 *   <li>{@link #incrementarTentativas()} — registra mais uma chamada ao gateway.</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
public class Pagamento {

    private final UUID id;
    private final UUID pedidoId;
    private final BigDecimal valor;
    private StatusPagamento status;
    private int tentativas;
    private String motivoFalha;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Cria um novo pagamento PENDENTE para o pedido informado.
     *
     * <p>Gera um {@link UUID} aleatório como identidade, marca {@code createdAt}
     * e {@code updatedAt} com o instante atual e zera o contador de tentativas.
     *
     * @param pedidoId identificador do pedido que originou este pagamento; obrigatório
     * @param valor    valor monetário do pagamento; deve ser positivo
     * @throws BusinessException se {@code pedidoId} for nulo ou {@code valor} não for positivo
     */
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

    /**
     * Construtor de hidratação — usado pelo adapter de persistência ao
     * reconstruir a entidade a partir do banco de dados.
     *
     * <p>Não aplica validações; assume que os dados vêm de uma fonte confiável
     * (a entidade JPA correspondente).
     *
     * @param id          identidade do pagamento (UUID v4)
     * @param pedidoId    identificador do pedido associado
     * @param valor       valor monetário
     * @param status      estado atual do pagamento
     * @param tentativas  número de chamadas já feitas ao gateway externo
     * @param motivoFalha descrição da última falha ou {@code null} se não houve
     * @param createdAt   instante da criação original do pagamento
     * @param updatedAt   instante da última modificação
     */
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

    /**
     * Transita o pagamento para o estado {@link StatusPagamento#APROVADO}.
     *
     * <p>Idempotente: se já estiver APROVADO, nada acontece. Caso contrário,
     * o motivo da falha é limpo e {@code updatedAt} é atualizado.
     */
    public void aprovar() {
        if (this.status == StatusPagamento.APROVADO) {
            return;
        }
        this.status = StatusPagamento.APROVADO;
        this.motivoFalha = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Mantém o pagamento como {@link StatusPagamento#PENDENTE} e registra o
     * motivo da última falha de comunicação com o gateway.
     *
     * @param motivo descrição da falha (será exibida em logs e no evento Kafka)
     */
    public void marcarComoPendente(String motivo) {
        this.status = StatusPagamento.PENDENTE;
        this.motivoFalha = motivo;
        this.updatedAt = Instant.now();
    }

    /**
     * Incrementa o contador de tentativas e atualiza o {@code updatedAt}.
     *
     * <p>Deve ser chamado <strong>antes</strong> de cada invocação ao gateway,
     * independentemente do resultado, para refletir o esforço realizado.
     */
    public void incrementarTentativas() {
        this.tentativas++;
        this.updatedAt = Instant.now();
    }

    /**
     * @return {@code true} se o status atual for {@link StatusPagamento#APROVADO}
     */
    public boolean estaAprovado() {
        return this.status == StatusPagamento.APROVADO;
    }

    /**
     * @return {@code true} se o status atual for {@link StatusPagamento#PENDENTE}
     */
    public boolean estaPendente() {
        return this.status == StatusPagamento.PENDENTE;
    }

    /** @return identidade única do pagamento (UUID v4) */
    public UUID getId() {
        return id;
    }

    /** @return identificador do pedido associado a este pagamento */
    public UUID getPedidoId() {
        return pedidoId;
    }

    /** @return valor monetário do pagamento */
    public BigDecimal getValor() {
        return valor;
    }

    /** @return estado atual do pagamento */
    public StatusPagamento getStatus() {
        return status;
    }

    /** @return número de chamadas já feitas ao gateway externo */
    public int getTentativas() {
        return tentativas;
    }

    /** @return motivo da última falha, ou {@code null} se nunca falhou ou já foi aprovado */
    public String getMotivoFalha() {
        return motivoFalha;
    }

    /** @return instante da criação original do pagamento */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** @return instante da última modificação de estado */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
