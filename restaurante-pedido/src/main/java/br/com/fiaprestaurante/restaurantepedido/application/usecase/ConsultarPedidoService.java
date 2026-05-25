package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação de {@link ConsultarPedidoUseCase} — operações somente-leitura
 * sobre pedidos, expostas via GraphQL (requisito 4.3).
 *
 * <p>Roda em {@code @Transactional(readOnly = true)} para permitir
 * otimizações do Hibernate (sem dirty-checking).
 *
 * @author Danilo Fernando
 */
@Service
public class ConsultarPedidoService implements ConsultarPedidoUseCase {

    private final PedidoRepository pedidoRepository;

    /**
     * @param pedidoRepository porta de persistência
     */
    public ConsultarPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Optional<PedidoResponse> porId(UUID pedidoId, UUID clienteId) {
        return pedidoRepository.buscarPorId(pedidoId)
                .filter(p -> p.getClienteId().equals(clienteId))
                .map(PedidoResponse::from);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> porCliente(UUID clienteId) {
        return pedidoRepository.listarPorCliente(clienteId)
                .stream()
                .map(PedidoResponse::from)
                .toList();
    }
}
