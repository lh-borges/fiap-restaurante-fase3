package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoEmPreparoEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.input.IniciarPreparoUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.output.CozinhaEventPublisher;
import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.exception.PedidoCozinhaNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementacao do caso de uso {@link IniciarPreparoUseCase}.
 *
 * <p>Transita RECEBIDO -> EM_PREPARO no agregado e publica o evento
 * {@code pedido.em-preparo} para o restaurante-pedido atualizar o status
 * do Pedido principal.
 *
 * @author Danilo Fernando
 */
@Service
public class IniciarPreparoService implements IniciarPreparoUseCase {

    private final PedidoCozinhaRepository repository;
    private final CozinhaEventPublisher publisher;

    public IniciarPreparoService(PedidoCozinhaRepository repository,
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
        pedido.iniciarPreparo();
        repository.salvar(pedido);
        publisher.publicarEmPreparo(new PedidoEmPreparoEvent(
                pedido.getPedidoId(),
                pedido.getId(),
                pedido.getRestauranteId(),
                Instant.now()
        ));
        return PedidoCozinhaResponse.from(pedido);
    }
}
