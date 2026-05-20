package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository sobre {@link PedidoJpaEntity}.
 *
 * <p>Fornece operações CRUD herdadas de {@link JpaRepository} e um derived
 * query usado pelo {@link PedidoRepositoryAdapter} para listar pedidos do
 * cliente em ordem decrescente de criação.
 *
 * @author Danilo Fernando
 */
public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, UUID> {

    /**
     * Lista todos os pedidos de um cliente, do mais recente para o mais antigo.
     *
     * @param clienteId identificador do cliente
     * @return lista (possivelmente vazia) de pedidos
     */
    List<PedidoJpaEntity> findByClienteIdOrderByCreatedAtDesc(UUID clienteId);
}
