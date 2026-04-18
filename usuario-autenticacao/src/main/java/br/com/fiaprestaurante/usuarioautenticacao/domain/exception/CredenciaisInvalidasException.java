package br.com.fiaprestaurante.usuarioautenticacao.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;

public class CredenciaisInvalidasException extends BusinessException {

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas.");
    }
}
