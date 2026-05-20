package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusPagamentoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementação de {@link AtualizarStatusPagamentoUseCase} — reage aos eventos
 * Kafka {@code pagamento.aprovado} e {@code pagamento.pendente} para refletir
 * o estado do pagamento no pedido (requisitos 4.5, 4.6 e 4.7).
 *
 * <p>Toda transição de estado é delegada à entidade de domínio
 * {@link Pedido}, que garante as regras de transição válidas (idempotência,
 * estados terminais, etc.).
 *
 * @author Danilo Fernando
 */
@Service
public class AtualizarStatusPagamentoService implements AtualizarStatusPagamentoUseCase {

    private static final Logger log = LoggerFactory.getLogger(AtualizarStatusPagamentoService.class);

    private final PedidoRepository pedidoRepository;

    /**
     * @param pedidoRepository porta de persistência
     */
    public AtualizarStatusPagamentoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void marcarComoPago(UUID pedidoId, UUID pagamentoId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + pedidoId));
        pedido.marcarComoPago(pagamentoId);
        pedidoRepository.salvar(pedido);
        log.info("Pedido marcado como PAGO: pedidoId={} pagamentoId={}", pedidoId, pagamentoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void marcarComoPendente(UUID pedidoId, String motivo) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + pedidoId));
        pedido.marcarComoPendentePagamento(motivo);
        pedidoRepository.salvar(pedido);
        log.warn("Pedido marcado como PENDENTE_PAGAMENTO: pedidoId={} motivo={}", pedidoId, motivo);
    }
}
