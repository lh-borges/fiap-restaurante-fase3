package br.com.fiaprestaurante.restauranteservice.domain.exception;

import java.util.UUID;

/**
 * Lancada quando um {@code PedidoCozinha} nao eh encontrado no repositorio.
 *
 * @author Danilo Fernando
 */
public class PedidoCozinhaNaoEncontradoException extends RuntimeException {

    public PedidoCozinhaNaoEncontradoException(UUID id) {
        super("Pedido na cozinha nao encontrado: " + id);
    }
}
