package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter de saída que implementa {@link PedidoRepository} delegando para o
 * Spring Data JPA via {@link PedidoJpaRepository} e convertendo entre domínio
 * e entidade JPA com {@link PedidoMapper}.
 *
 * <p>É a única classe da camada de adapter que conhece tanto o domínio
 * quanto o ORM — exatamente o ponto de tradução previsto pelo hexagonal.
 *
 * @author Danilo Fernando
 */
@Component
public class PedidoRepositoryAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpaRepository;

    /**
     * @param jpaRepository repositório Spring Data subjacente
     */
    public PedidoRepositoryAdapter(PedidoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity entity = PedidoMapper.toEntity(pedido);
        PedidoJpaEntity salvo = jpaRepository.save(entity);
        return PedidoMapper.toDomain(salvo);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Pedido> buscarPorId(UUID pedidoId) {
        return jpaRepository.findById(pedidoId).map(PedidoMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public List<Pedido> listarPorCliente(UUID clienteId) {
        return jpaRepository.findByClienteIdOrderByCreatedAtDesc(clienteId)
                .stream()
                .map(PedidoMapper::toDomain)
                .toList();
    }
}
