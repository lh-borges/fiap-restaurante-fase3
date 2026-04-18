package br.com.fiaprestaurante.usuarioautenticacao.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;

public class UsuarioJaExisteException extends BusinessException {

    public UsuarioJaExisteException(String email) {
        super("Já existe um usuário cadastrado com o e-mail: " + email);
    }
}
