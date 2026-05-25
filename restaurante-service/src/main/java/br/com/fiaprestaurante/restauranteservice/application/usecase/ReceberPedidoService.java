package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoParaCozinhaEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.input.ReceberPedidoUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.entity.ItemCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacao do caso de uso {@link ReceberPedidoUseCase}.
 *
 * <p>Cria o agregado {@link PedidoCozinha} no status RECEBIDO a partir do
 * evento Kafka {@code pedido.pronto-para-cozinha}.
 *
 * @author Danilo Fernando
 */
@Service
public class ReceberPedidoService implements ReceberPedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReceberPedidoService.class);

    private final PedidoCozinhaRepository repository;

    public ReceberPedidoService(PedidoCozinhaRepository repository) {
        this.repository = repository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void executar(PedidoProntoParaCozinhaEvent event) {
        List<ItemCozinha> itens = event.itens().stream()
                .map(i -> new ItemCozinha(i.produtoId(), i.nome(), i.quantidade()))
                .toList();
        PedidoCozinha pedido = new PedidoCozinha(event.pedidoId(), event.restauranteId(), itens);
        repository.salvar(pedido);
        log.info("PedidoCozinha criado: id={}, pedidoId={}, itens={}",
                pedido.getId(), pedido.getPedidoId(), itens.size());
    }
}
