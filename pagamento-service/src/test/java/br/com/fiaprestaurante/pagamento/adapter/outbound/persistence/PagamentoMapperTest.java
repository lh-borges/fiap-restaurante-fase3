package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios do {@link PagamentoMapper} - valida conversao em
 * ambos os sentidos e o round-trip.
 *
 * @author Danilo Fernando
 */
class PagamentoMapperTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Test
    void toEntityDeveCopiarTodosOsCampos() {
        Pagamento p = new Pagamento(PEDIDO_ID, new BigDecimal("59.30"));

        PagamentoJpaEntity entity = PagamentoMapper.toEntity(p);

        assertThat(entity.getId()).isEqualTo(p.getId());
        assertThat(entity.getPedidoId()).isEqualTo(p.getPedidoId());
        assertThat(entity.getValor()).isEqualByComparingTo(p.getValor());
        assertThat(entity.getStatus()).isEqualTo(p.getStatus());
        assertThat(entity.getTentativas()).isEqualTo(p.getTentativas());
        assertThat(entity.getCreatedAt()).isEqualTo(p.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(p.getUpdatedAt());
    }

    @Test
    void toDomainDeveCopiarTodosOsCampos() {
        UUID id = UUID.randomUUID();
        Instant criado = Instant.parse("2026-01-01T10:00:00Z");
        Instant atualizado = Instant.parse("2026-01-02T11:00:00Z");
        PagamentoJpaEntity entity = new PagamentoJpaEntity(
                id, PEDIDO_ID, new BigDecimal("42.00"),
                StatusPagamento.APROVADO, 2, "motivo", criado, atualizado);

        Pagamento p = PagamentoMapper.toDomain(entity);

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getPedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(p.getValor()).isEqualByComparingTo("42.00");
        assertThat(p.getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(p.getTentativas()).isEqualTo(2);
        assertThat(p.getMotivoFalha()).isEqualTo("motivo");
        assertThat(p.getCreatedAt()).isEqualTo(criado);
        assertThat(p.getUpdatedAt()).isEqualTo(atualizado);
    }

    @Test
    void roundTripDevePreservarTodosOsCampos() {
        Pagamento original = new Pagamento(PEDIDO_ID, new BigDecimal("100.00"));
        original.incrementarTentativas();
        original.marcarComoPendente("falha temporaria");

        Pagamento reconvertido = PagamentoMapper.toDomain(PagamentoMapper.toEntity(original));

        assertThat(reconvertido.getId()).isEqualTo(original.getId());
        assertThat(reconvertido.getStatus()).isEqualTo(original.getStatus());
        assertThat(reconvertido.getTentativas()).isEqualTo(original.getTentativas());
        assertThat(reconvertido.getMotivoFalha()).isEqualTo(original.getMotivoFalha());
    }
}
