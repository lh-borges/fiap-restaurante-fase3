package br.com.fiaprestaurante.restauranteservice.adapter.outbound.persistence;

import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter de saida que implementa {@link PedidoCozinhaRepository} delegando
 * ao Spring Data JPA via {@link PedidoCozinhaJpaRepository}.
 *
 * <p>Faz a tradutaria entre o dominio puro e o modelo JPA usando o
 * {@link PedidoCozinhaMapper}.
 *
 * @author Danilo Fernando
 */
@Component
public class PedidoCozinhaRepositoryAdapter implements PedidoCozinhaRepository {

    private final PedidoCozinhaJpaRepository jpaRepository;

    public PedidoCozinhaRepositoryAdapter(PedidoCozinhaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /** {@inheritDoc} */
    @Override
    public PedidoCozinha salvar(PedidoCozinha pedidoCozinha) {
        PedidoCozinhaJpaEntity entity = PedidoCozinhaMapper.toEntity(pedidoCozinha);
        jpaRepository.save(entity);
        return pedidoCozinha;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PedidoCozinha> porId(UUID id) {
        return jpaRepository.findById(id).map(PedidoCozinhaMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public List<PedidoCozinha> listar(StatusCozinha filtroStatus) {
        List<PedidoCozinhaJpaEntity> entities = filtroStatus == null
                ? jpaRepository.findAllByOrderByCreatedAtAsc()
                : jpaRepository.findByStatusOrderByCreatedAtAsc(filtroStatus);
        return entities.stream().map(PedidoCozinhaMapper::toDomain).toList();
    }
}
