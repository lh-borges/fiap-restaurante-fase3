package br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.in;

import br.com.fiaprestaurante.modules.usuarioautenticacao.application.command.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;

public interface CadastrarUsuarioUseCase {

    Usuario executar(CadastrarUsuarioCommand command);
}
