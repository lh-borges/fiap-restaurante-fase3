package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusCozinhaUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementacao de {@link AtualizarStatusCozinhaUseCase} — reage aos eventos
 * Kafka {@code pedido.em-preparo} e {@code pedido.pronto} publicados pelo
 * restaurante-service.
 *
 * <p>Toda transicao eh delegada a entidade de dominio {@link Pedido}, que
 * garante as regras (idempotencia, transicoes validas).
 *
 * @author Danilo Fernando
 */
@Service
public class AtualizarStatusCozinhaService implements AtualizarStatusCozinhaUseCase {

    private static final Logger log = LoggerFactory.getLogger(AtualizarStatusCozinhaService.class);

    private final PedidoRepository pedidoRepository;

    public AtualizarStatusCozinhaService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void marcarComoEmPreparo(UUID pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido nao encontrado: " + pedidoId));
        pedido.marcarComoEmPreparo();
        pedidoRepository.salvar(pedido);
        log.info("Pedido marcado como EM_PREPARO: pedidoId={}", pedidoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void marcarComoPronto(UUID pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido nao encontrado: " + pedidoId));
        pedido.marcarComoPronto();
        pedidoRepository.salvar(pedido);
        log.info("Pedido marcado como PRONTO: pedidoId={}", pedidoId);
    }
}
