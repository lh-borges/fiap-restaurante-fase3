package br.com.fiaprestaurante.restauranteservice.adapter.outbound.persistence;

import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data para {@link PedidoCozinhaJpaEntity}.
 *
 * <p>Usado pelo {@code PedidoCozinhaRepositoryAdapter}, que isola o dominio
 * da dependencia direta com Spring Data.
 *
 * @author Danilo Fernando
 */
@Repository
public interface PedidoCozinhaJpaRepository extends JpaRepository<PedidoCozinhaJpaEntity, UUID> {

    List<PedidoCozinhaJpaEntity> findByStatusOrderByCreatedAtAsc(StatusCozinha status);

    List<PedidoCozinhaJpaEntity> findAllByOrderByCreatedAtAsc();
}
