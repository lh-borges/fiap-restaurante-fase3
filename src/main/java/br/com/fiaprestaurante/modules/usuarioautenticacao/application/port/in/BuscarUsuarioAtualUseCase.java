package br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.in;

import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;
import java.util.UUID;

public interface BuscarUsuarioAtualUseCase {

    Usuario executar(UUID publicId);
}
