package br.com.fiaprestaurante.usuarioautenticacao.application.port.input;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.AutenticarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.SessaoUsuario;

public interface AutenticarUsuarioUseCase {

    SessaoUsuario executar(AutenticarUsuarioCommand command);
}
