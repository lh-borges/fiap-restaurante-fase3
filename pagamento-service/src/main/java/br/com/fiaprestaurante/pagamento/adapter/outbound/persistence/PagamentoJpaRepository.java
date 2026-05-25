package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository sobre {@link PagamentoJpaEntity}.
 *
 * <p>Fornece operações CRUD herdadas de {@link JpaRepository} e dois
 * derived queries usados pelo {@link PagamentoRepositoryAdapter}.
 *
 * @author Danilo Fernando
 */
public interface PagamentoJpaRepository extends JpaRepository<PagamentoJpaEntity, UUID> {

    /**
     * Busca o pagamento associado a um pedido.
     *
     * <p>Como existe índice único em {@code pedido_id}, retorna no máximo um
     * resultado.
     *
     * @param pedidoId identificador do pedido
     * @return o pagamento, ou vazio se não houver
     */
    Optional<PagamentoJpaEntity> findByPedidoId(UUID pedidoId);

    /**
     * Lista pagamentos com o status informado, ordenados pela data de criação
     * (mais antigos primeiro). Suporta paginação para limitar o tamanho do lote.
     *
     * @param status   status alvo (geralmente {@link StatusPagamento#PENDENTE})
     * @param pageable paginação (usar {@code PageRequest.of(0, batchSize)})
     * @return lista (possivelmente vazia)
     */
    List<PagamentoJpaEntity> findByStatusOrderByCreatedAtAsc(StatusPagamento status, Pageable pageable);
}
