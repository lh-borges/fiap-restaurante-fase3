package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;

/**
 * Mapper estático entre a entidade de domínio {@link Pagamento} e a entidade
 * JPA {@link PagamentoJpaEntity}.
 *
 * <p>Mantém a regra de ouro do hexagonal: domain não importa JPA. Em vez
 * de usar uma lib (MapStruct/ModelMapper) — que adicionaria peso — fazemos
 * a conversão manualmente, simples e auditável.
 *
 * @author Danilo Fernando
 */
public final class PagamentoMapper {

    private PagamentoMapper() {
    }

    /**
     * Converte uma entidade de domínio para a entidade JPA correspondente.
     *
     * @param pagamento entidade de domínio (não pode ser {@code null})
     * @return entidade JPA pronta para persistência
     */
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

    /**
     * Converte uma entidade JPA recuperada do banco em entidade de domínio.
     *
     * <p>Usa o construtor de hidratação do {@link Pagamento} (que não aplica
     * validações), pois os dados vêm de uma fonte confiável.
     *
     * @param entity entidade JPA recém-carregada (não pode ser {@code null})
     * @return entidade de domínio reidratada
     */
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
