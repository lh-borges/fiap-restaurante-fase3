package br.com.fiaprestaurante.pagamento.application.dto;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PagamentoDtoTest {

    @Test
    void deveCriarCommandComCamposInformados() {
        UUID pedidoId = UUID.randomUUID();
        BigDecimal valor = new BigDecimal("42.90");

        ProcessarPagamentoCommand command = new ProcessarPagamentoCommand(pedidoId, valor);

        assertThat(command.pedidoId()).isEqualTo(pedidoId);
        assertThat(command.valorTotal()).isEqualByComparingTo(valor);
    }

    @Test
    void deveCriarEventosComCamposInformados() {
        UUID pedidoId = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        Instant timestamp = Instant.parse("2026-05-21T12:00:00Z");

        PagamentoAprovadoEvent aprovado = new PagamentoAprovadoEvent(pedidoId, pagamentoId, timestamp);
        PagamentoPendenteEvent pendente = new PagamentoPendenteEvent(pedidoId, pagamentoId, "gateway fora", timestamp);

        assertThat(aprovado.pedidoId()).isEqualTo(pedidoId);
        assertThat(aprovado.pagamentoId()).isEqualTo(pagamentoId);
        assertThat(aprovado.timestamp()).isEqualTo(timestamp);
        assertThat(pendente.pedidoId()).isEqualTo(pedidoId);
        assertThat(pendente.pagamentoId()).isEqualTo(pagamentoId);
        assertThat(pendente.motivo()).isEqualTo("gateway fora");
        assertThat(pendente.timestamp()).isEqualTo(timestamp);
    }

    @Test
    void deveMapearPagamentoParaResponse() {
        UUID id = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-21T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-05-21T10:01:00Z");
        Pagamento pagamento = new Pagamento(
                id,
                pedidoId,
                new BigDecimal("99.90"),
                StatusPagamento.PENDENTE,
                2,
                "timeout",
                createdAt,
                updatedAt
        );

        PagamentoResponse response = PagamentoResponse.from(pagamento);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.pedidoId()).isEqualTo(pedidoId);
        assertThat(response.valor()).isEqualByComparingTo("99.90");
        assertThat(response.status()).isEqualTo("PENDENTE");
        assertThat(response.tentativas()).isEqualTo(2);
        assertThat(response.motivoFalha()).isEqualTo("timeout");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }
}
