package br.com.fiaprestaurante.pagamento.domain.entity;

import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagamentoTest {

    @Test
    void deveCriarPagamentoPendente() {
        UUID pedidoId = UUID.randomUUID();

        Pagamento pagamento = new Pagamento(pedidoId, new BigDecimal("10.00"));

        assertThat(pagamento.getId()).isNotNull();
        assertThat(pagamento.getPedidoId()).isEqualTo(pedidoId);
        assertThat(pagamento.getValor()).isEqualByComparingTo("10.00");
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.PENDENTE);
        assertThat(pagamento.getTentativas()).isZero();
        assertThat(pagamento.getMotivoFalha()).isNull();
        assertThat(pagamento.estaPendente()).isTrue();
    }

    @Test
    void deveValidarCamposObrigatorios() {
        assertThatThrownBy(() -> new Pagamento(null, new BigDecimal("10.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("pedidoId é obrigatório");

        assertThatThrownBy(() -> new Pagamento(UUID.randomUUID(), BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("valor do pagamento deve ser positivo");
    }

    @Test
    void deveAprovarPagamentoLimpandoMotivoFalha() {
        Pagamento pagamento = new Pagamento(UUID.randomUUID(), new BigDecimal("10.00"));
        pagamento.marcarComoPendente("timeout");

        pagamento.aprovar();

        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(pagamento.getMotivoFalha()).isNull();
        assertThat(pagamento.estaAprovado()).isTrue();
    }

    @Test
    void deveIncrementarTentativas() {
        Pagamento pagamento = new Pagamento(UUID.randomUUID(), new BigDecimal("10.00"));
        Instant updatedAtAntes = pagamento.getUpdatedAt();

        pagamento.incrementarTentativas();

        assertThat(pagamento.getTentativas()).isEqualTo(1);
        assertThat(pagamento.getUpdatedAt()).isAfterOrEqualTo(updatedAtAntes);
    }
}
