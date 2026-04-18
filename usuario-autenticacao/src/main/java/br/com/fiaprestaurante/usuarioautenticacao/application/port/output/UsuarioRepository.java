package br.com.fiaprestaurante.usuarioautenticacao.application.port.output;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorPublicId(UUID publicId);

    Usuario salvar(Usuario usuario);
}
