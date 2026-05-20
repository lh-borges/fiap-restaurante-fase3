package br.com.fiaprestaurante.pagamento.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;

/**
 * Lançada quando uma operação tenta acessar um pagamento que não existe no
 * repositório (por exemplo, ao consultar por um {@code pedidoId} desconhecido).
 *
 * <p>Por estender {@link BusinessException}, é tratada como erro de regra de
 * negócio (4xx) e não como falha de infraestrutura (5xx).
 *
 * @author Danilo Fernando
 */
public class PagamentoNaoEncontradoException extends BusinessException {

    /**
     * Cria a exceção com uma mensagem descritiva do contexto.
     *
     * @param mensagem descrição amigável do recurso que não foi encontrado
     */
    public PagamentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
