package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;

public final class PagamentoMapper {

    private PagamentoMapper() {
    }

    public static PagamentoJpaEntity toEntity(Pagamento pagamento) {
        return new PagamentoJpaEntity(
                pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getTentativas(),
                pagamento.getMotivoFalha(),
                pagamento.getCreatedAt(),
                pagamento.getUpdatedAt()
        );
    }

    public static Pagamento toDomain(PagamentoJpaEntity entity) {
        return new Pagamento(
                entity.getId(),
                entity.getPedidoId(),
                entity.getValor(),
                entity.getStatus(),
                entity.getTentativas(),
                entity.getMotivoFalha(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
