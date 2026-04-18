package br.com.fiaprestaurante.usuarioautenticacao.application.usecase;

import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.BuscarUsuarioAtualUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BuscarUsuarioAtualService implements BuscarUsuarioAtualUseCase {

    private final UsuarioRepository usuarioRepository;

    public BuscarUsuarioAtualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario executar(UUID publicId) {
        return usuarioRepository.buscarPorPublicId(publicId)
            .orElseThrow(() -> new UsuarioNaoEncontradoException(publicId.toString()));
    }
}
