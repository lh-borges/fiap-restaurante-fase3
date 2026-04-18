package br.com.fiaprestaurante.modules.usuarioautenticacao.application.service;

import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.in.BuscarUsuarioAtualUseCase;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.UsuarioRepository;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class BuscarUsuarioAtualService implements BuscarUsuarioAtualUseCase {

    private final UsuarioRepository usuarioRepository;

    @Inject
    public BuscarUsuarioAtualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario executar(UUID publicId) {
        return usuarioRepository.buscarPorPublicId(publicId)
            .orElseThrow(UsuarioNaoEncontradoException::new);
    }
}
