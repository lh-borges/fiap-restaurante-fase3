package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConfirmarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoEventPublisher;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementação de {@link ConfirmarPedidoUseCase} — transita o pedido para
 * {@code CONFIRMADO} e publica o evento {@code pedido.criado} no Kafka,
 * iniciando o fluxo assíncrono de pagamento (requisitos 4.2 e 5.3).
 *
 * <p>Valida que o {@code clienteId} do JWT corresponde ao dono do pedido,
 * evitando que um usuário confirme pedido alheio.
 *
 * @author Danilo Fernando
 */
@Service
public class ConfirmarPedidoService implements ConfirmarPedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarPedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;

    /**
     * @param pedidoRepository porta de persistência
     * @param eventPublisher   porta de publicação de eventos
     */
    public ConfirmarPedidoService(PedidoRepository pedidoRepository, PedidoEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public PedidoResponse executar(UUID pedidoId, UUID clienteId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + pedidoId));

        if (!pedido.getClienteId().equals(clienteId)) {
            throw new BusinessException("pedido não pertence ao cliente autenticado");
        }

        pedido.confirmar();
        Pedido salvo = pedidoRepository.salvar(pedido);

        eventPublisher.publicarPedidoCriado(new PedidoCriadoEvent(
                salvo.getId(),
                salvo.getClienteId(),
                salvo.getValorTotal(),
                Instant.now()
        ));

        log.info("Pedido confirmado: pedidoId={} clienteId={} valorTotal={}",
                salvo.getId(), salvo.getClienteId(), salvo.getValorTotal());

        return PedidoResponse.from(salvo);
    }
}
