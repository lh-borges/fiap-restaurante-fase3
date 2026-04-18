package br.com.fiaprestaurante.usuarioautenticacao.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;

public class UsuarioNaoEncontradoException extends BusinessException {

    public UsuarioNaoEncontradoException(String detalhe) {
        super("Usuário não encontrado: " + detalhe);
    }
}
