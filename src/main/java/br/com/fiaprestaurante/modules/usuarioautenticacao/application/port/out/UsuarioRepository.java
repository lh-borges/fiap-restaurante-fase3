package br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out;

import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorPublicId(UUID publicId);

    Usuario salvar(Usuario usuario);
}
