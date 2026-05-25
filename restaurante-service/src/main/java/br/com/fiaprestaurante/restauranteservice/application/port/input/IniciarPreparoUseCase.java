package br.com.fiaprestaurante.restauranteservice.application.port.input;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;

import java.util.UUID;

/**
 * Porta de entrada — transita o pedido de RECEBIDO para EM_PREPARO e publica
 * o evento {@code pedido.em-preparo}. Disparada via mutation GraphQL.
 *
 * @author Danilo Fernando
 */
public interface IniciarPreparoUseCase {

    /**
     * @param pedidoCozinhaId identidade do pedido na fila da cozinha
     * @return o pedido atualizado
     */
    PedidoCozinhaResponse executar(UUID pedidoCozinhaId);
}
