package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.input.MarcarComoProntoUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.output.CozinhaEventPublisher;
import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.exception.PedidoCozinhaNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementacao do caso de uso {@link MarcarComoProntoUseCase}.
 *
 * <p>Transita EM_PREPARO -> PRONTO no agregado e publica o evento
 * {@code pedido.pronto} para o restaurante-pedido atualizar o status
 * do Pedido principal.
 *
 * @author Danilo Fernando
 */
@Service
public class MarcarComoProntoService implements MarcarComoProntoUseCase {

    private final PedidoCozinhaRepository repository;
    private final CozinhaEventPublisher publisher;

    public MarcarComoProntoService(PedidoCozinhaRepository repository,
                                   CozinhaEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public PedidoCozinhaResponse executar(UUID pedidoCozinhaId) {
        PedidoCozinha pedido = repository.porId(pedidoCozinhaId)
                .orElseThrow(() -> new PedidoCozinhaNaoEncontradoException(pedidoCozinhaId));
        pedido.marcarComoPronto();
        repository.salvar(pedido);
        publisher.publicarPronto(new PedidoProntoEvent(
                pedido.getPedidoId(),
                pedido.getId(),
                pedido.getRestauranteId(),
                Instant.now()
        ));
        return PedidoCozinhaResponse.from(pedido);
    }
}
