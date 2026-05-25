package br.com.fiaprestaurante.restauranteservice.application.port.input;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;

import java.util.UUID;

/**
 * Porta de entrada — transita o pedido de EM_PREPARO para PRONTO e publica
 * o evento {@code pedido.pronto}. Disparada via mutation GraphQL.
 *
 * @author Danilo Fernando
 */
public interface MarcarComoProntoUseCase {

    /**
     * @param pedidoCozinhaId identidade do pedido na fila da cozinha
     * @return o pedido atualizado
     */
    PedidoCozinhaResponse executar(UUID pedidoCozinhaId);
}
