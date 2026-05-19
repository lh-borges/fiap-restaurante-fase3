package br.com.fiaprestaurante.pagamento.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;

public class PagamentoNaoEncontradoException extends BusinessException {

    public PagamentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
