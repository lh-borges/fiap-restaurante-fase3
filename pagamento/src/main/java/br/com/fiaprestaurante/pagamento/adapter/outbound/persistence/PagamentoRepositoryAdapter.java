package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter de saída que implementa {@link PagamentoRepository} delegando para
 * o Spring Data JPA via {@link PagamentoJpaRepository} e convertendo entre
 * domínio e entidade JPA com {@link PagamentoMapper}.
 *
 * <p>É a única classe da camada de adapter que conhece tanto o domínio
 * quanto o ORM — exatamente o ponto de tradução previsto pelo hexagonal.
 *
 * @author Danilo Fernando
 */
@Component
public class PagamentoRepositoryAdapter implements PagamentoRepository {

    private final PagamentoJpaRepository jpaRepository;

    /**
     * @param jpaRepository repositório Spring Data subjacente
     */
    public PagamentoRepositoryAdapter(PagamentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Pagamento salvar(Pagamento pagamento) {
        PagamentoJpaEntity entity = PagamentoMapper.toEntity(pagamento);
        PagamentoJpaEntity salvo = jpaRepository.save(entity);
        return PagamentoMapper.toDomain(salvo);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Pagamento> buscarPorPedidoId(UUID pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId).map(PagamentoMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     *
     * <p>O parâmetro {@code limite} é fixado entre 1 e 1000 antes de ser
     * usado na paginação, evitando queries patológicas.
     */
    @Override
    public List<Pagamento> listarPendentes(int limite) {
        int pageSize = Math.max(1, Math.min(limite, 1000));
        return jpaRepository
                .findByStatusOrderByCreatedAtAsc(StatusPagamento.PENDENTE, PageRequest.of(0, pageSize))
                .stream()
                .map(PagamentoMapper::toDomain)
                .toList();
    }
}
