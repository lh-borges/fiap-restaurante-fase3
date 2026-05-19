package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PagamentoRepositoryAdapter implements PagamentoRepository {

    private final PagamentoJpaRepository jpaRepository;

    public PagamentoRepositoryAdapter(PagamentoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pagamento salvar(Pagamento pagamento) {
        PagamentoJpaEntity entity = PagamentoMapper.toEntity(pagamento);
        PagamentoJpaEntity salvo = jpaRepository.save(entity);
        return PagamentoMapper.toDomain(salvo);
    }

    @Override
    public Optional<Pagamento> buscarPorPedidoId(UUID pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId).map(PagamentoMapper::toDomain);
    }

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
