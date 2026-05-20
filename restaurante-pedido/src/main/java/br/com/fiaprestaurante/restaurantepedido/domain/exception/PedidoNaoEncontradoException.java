package br.com.fiaprestaurante.restaurantepedido.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;

/**
 * Lançada quando uma operação tenta acessar um pedido inexistente.
 *
 * <p>Por estender {@link BusinessException}, é tratada como erro de regra de
 * negócio (4xx) e não como falha de infraestrutura (5xx).
 *
 * @author Danilo Fernando
 */
public class PedidoNaoEncontradoException extends BusinessException {

    /**
     * Cria a exceção com uma mensagem descritiva do contexto.
     *
     * @param mensagem descrição amigável do recurso que não foi encontrado
     */
    public PedidoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
