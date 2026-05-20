package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.CriarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementação de {@link CriarPedidoUseCase} — cria o pedido no status
 * {@code CRIADO} e devolve com o ID gerado e o valor total calculado
 * (requisito 4.2).
 *
 * <p>A publicação no Kafka acontece somente na confirmação
 * ({@link ConfirmarPedidoService}), preservando a semântica do requisito:
 * "o serviço calcula e devolve, pedindo confirmação".
 *
 * @author Danilo Fernando
 */
@Service
public class CriarPedidoService implements CriarPedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CriarPedidoService.class);

    private final PedidoRepository pedidoRepository;

    /**
     * @param pedidoRepository porta de persistência
     */
    public CriarPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public PedidoResponse executar(CriarPedidoCommand command) {
        List<ItemPedido> itens = command.itens().stream()
                .map(CriarPedidoService::toDomain)
                .toList();

        Pedido pedido = new Pedido(command.clienteId(), command.restauranteId(), itens);
        Pedido salvo = pedidoRepository.salvar(pedido);

        log.info("Pedido criado: pedidoId={} clienteId={} valorTotal={}",
                salvo.getId(), salvo.getClienteId(), salvo.getValorTotal());

        return PedidoResponse.from(salvo);
    }

    /**
     * Converte um {@link ItemPedidoCommand} (DTO) em {@link ItemPedido}
     * (entidade de domínio), disparando as validações de invariantes.
     *
     * @param command item recebido na requisição
     * @return entidade de domínio validada
     */
    private static ItemPedido toDomain(ItemPedidoCommand command) {
        return new ItemPedido(command.produtoId(), command.nome(), command.quantidade(), command.preco());
    }
}
