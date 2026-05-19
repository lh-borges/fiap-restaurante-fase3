package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, UUID> {

    Optional<PagamentoJpaEntity> findByPedidoId(UUID pedidoId);

    List<PagamentoJpaEntity> findByStatusOrderByCreatedAtAsc(StatusPagamento status, Pageable pageable);
}
