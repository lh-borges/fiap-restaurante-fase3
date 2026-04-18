package br.com.fiaprestaurante.usuarioautenticacao.application.port.input;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;

import java.util.UUID;

public interface BuscarUsuarioAtualUseCase {

    Usuario executar(UUID publicId);
}
