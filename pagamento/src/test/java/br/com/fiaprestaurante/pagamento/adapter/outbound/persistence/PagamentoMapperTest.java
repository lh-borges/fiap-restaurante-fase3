package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PagamentoMapperTest {

    @Test
    void deveConverterDomainParaEntity() {
        Pagamento pagamento = pagamento();

        PagamentoJpaEntity entity = PagamentoMapper.toEntity(pagamento);

        assertThat(entity.getId()).isEqualTo(pagamento.getId());
        assertThat(entity.getPedidoId()).isEqualTo(pagamento.getPedidoId());
        assertThat(entity.getValor()).isEqualByComparingTo(pagamento.getValor());
        assertThat(entity.getStatus()).isEqualTo(pagamento.getStatus());
        assertThat(entity.getTentativas()).isEqualTo(pagamento.getTentativas());
        assertThat(entity.getMotivoFalha()).isEqualTo(pagamento.getMotivoFalha());
        assertThat(entity.getCreatedAt()).isEqualTo(pagamento.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(pagamento.getUpdatedAt());
    }

    @Test
    void deveConverterEntityParaDomain() {
        UUID id = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-21T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-05-21T10:01:00Z");
        PagamentoJpaEntity entity = new PagamentoJpaEntity(
                id,
                pedidoId,
                new BigDecimal("50.00"),
                StatusPagamento.APROVADO,
                3,
                null,
                createdAt,
                updatedAt
        );

        Pagamento pagamento = PagamentoMapper.toDomain(entity);

        assertThat(pagamento.getId()).isEqualTo(id);
        assertThat(pagamento.getPedidoId()).isEqualTo(pedidoId);
        assertThat(pagamento.getValor()).isEqualByComparingTo("50.00");
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(pagamento.getTentativas()).isEqualTo(3);
        assertThat(pagamento.getMotivoFalha()).isNull();
        assertThat(pagamento.getCreatedAt()).isEqualTo(createdAt);
        assertThat(pagamento.getUpdatedAt()).isEqualTo(updatedAt);
    }

    private static Pagamento pagamento() {
        return new Pagamento(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                StatusPagamento.PENDENTE,
                2,
                "timeout",
                Instant.parse("2026-05-21T10:00:00Z"),
                Instant.parse("2026-05-21T10:01:00Z")
        );
    }
}
