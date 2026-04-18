package br.com.fiaprestaurante.usuarioautenticacao.application.port.input;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;

public interface CadastrarUsuarioUseCase {

    Usuario executar(CadastrarUsuarioCommand command);
}
