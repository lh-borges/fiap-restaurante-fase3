package br.com.fiaprestaurante.pagamento.domain.entity;

import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitarios da entidade de dominio {@link Pagamento} - cobrem
 * invariantes do construtor, transicoes de status (aprovar idempotente,
 * marcar como pendente), incrementar tentativas e o construtor de
 * hidratacao.
 *
 * @author Danilo Fernando
 */
class PagamentoTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Test
    void deveCriarPagamentoNoStatusPendente() {
        Pagamento p = new Pagamento(PEDIDO_ID, new BigDecimal("59.30"));

        assertThat(p.getId()).isNotNull();
        assertThat(p.getPedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(p.getValor()).isEqualByComparingTo("59.30");
        assertThat(p.getStatus()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(p.getTentativas()).isZero();
        assertThat(p.getMotivoFalha()).isNull();
        assertThat(p.getCreatedAt()).isNotNull();
        assertThat(p.getUpdatedAt()).isNotNull();
        assertThat(p.estaPendente()).isTrue();
        assertThat(p.estaAprovado()).isFalse();
    }

    @Test
    void deveRejeitarPedidoIdNulo() {
        assertThatThrownBy(() -> new Pagamento(null, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pedidoId");
    }

    @Test
    void deveRejeitarValorNulo() {
        assertThatThrownBy(() -> new Pagamento(PEDIDO_ID, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("valor");
    }

    @Test
    void deveRejeitarValorZero() {
        assertThatThrownBy(() -> new Pagamento(PEDIDO_ID, BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("valor");
    }

    @Test
    void deveRejeitarValorNegativo() {
        assertThatThrownBy(() -> new Pagamento(PEDIDO_ID, new BigDecimal("-1.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("valor");
    }

    @Test
    void aprovarDeveMudarStatusELimparMotivoFalha() {
        Pagamento p = new Pagamento(PEDIDO_ID, BigDecimal.TEN);
        p.marcarComoPendente("gateway off");

        p.aprovar();

        assertThat(p.getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(p.getMotivoFalha()).isNull();
        assertThat(p.estaAprovado()).isTrue();
    }

    @Test
    void aprovarDeveSerIdempotenteSemAlterarUpdatedAt() {
        Pagamento p = new Pagamento(PEDIDO_ID, BigDecimal.TEN);
        p.aprovar();
        Instant updatedAposPrimeira = p.getUpdatedAt();

        p.aprovar();

        assertThat(p.getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(p.getUpdatedAt()).isEqualTo(updatedAposPrimeira);
    }

    @Test
    void marcarComoPendenteDeveDefinirMotivoFalha() {
        Pagamento p = new Pagamento(PEDIDO_ID, BigDecimal.TEN);

        p.marcarComoPendente("timeout no procpag");

        assertThat(p.getStatus()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(p.getMotivoFalha()).isEqualTo("timeout no procpag");
        assertThat(p.estaPendente()).isTrue();
    }

    @Test
    void incrementarTentativasDeveAumentarContador() {
        Pagamento p = new Pagamento(PEDIDO_ID, BigDecimal.TEN);

        p.incrementarTentativas();
        p.incrementarTentativas();
        p.incrementarTentativas();

        assertThat(p.getTentativas()).isEqualTo(3);
    }

    @Test
    void construtorDeHidratacaoDevePreservarTodosOsCampos() {
        UUID id = UUID.randomUUID();
        Instant criado = Instant.parse("2026-01-01T10:00:00Z");
        Instant atualizado = Instant.parse("2026-01-02T11:00:00Z");

        Pagamento p = new Pagamento(id, PEDIDO_ID, new BigDecimal("99.99"),
                StatusPagamento.APROVADO, 5, "motivo antigo", criado, atualizado);

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getPedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(p.getValor()).isEqualByComparingTo("99.99");
        assertThat(p.getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(p.getTentativas()).isEqualTo(5);
        assertThat(p.getMotivoFalha()).isEqualTo("motivo antigo");
        assertThat(p.getCreatedAt()).isEqualTo(criado);
        assertThat(p.getUpdatedAt()).isEqualTo(atualizado);
    }
}
