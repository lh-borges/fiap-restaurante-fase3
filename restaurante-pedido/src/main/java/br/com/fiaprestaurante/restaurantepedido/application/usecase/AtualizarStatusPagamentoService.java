package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoProntoParaCozinhaEvent;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusPagamentoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoEventPublisher;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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
    private final PedidoEventPublisher eventPublisher;

    /**
     * @param pedidoRepository porta de persistência
     * @param eventPublisher   porta de publicacao de eventos (Kafka)
     */
    public AtualizarStatusPagamentoService(PedidoRepository pedidoRepository,
                                           PedidoEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
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
        publicarParaCozinha(pedido);
    }

    /**
     * Notifica o {@code restaurante-service} (cozinha) que o pedido esta pronto
     * para iniciar o preparo. Disparado apos o status virar PAGO.
     */
    private void publicarParaCozinha(Pedido pedido) {
        List<PedidoProntoParaCozinhaEvent.Item> itens = pedido.getItens().stream()
                .map(this::toEventItem)
                .toList();
        eventPublisher.publicarProntoParaCozinha(new PedidoProntoParaCozinhaEvent(
                pedido.getId(),
                pedido.getRestauranteId(),
                itens,
                Instant.now()
        ));
    }

    private PedidoProntoParaCozinhaEvent.Item toEventItem(ItemPedido item) {
        return new PedidoProntoParaCozinhaEvent.Item(item.getProdutoId(), item.getNome(), item.getQuantidade());
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
