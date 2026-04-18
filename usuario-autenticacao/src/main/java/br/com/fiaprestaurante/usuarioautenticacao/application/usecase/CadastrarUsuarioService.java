package br.com.fiaprestaurante.usuarioautenticacao.application.usecase;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.CadastrarUsuarioUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.PasswordHasher;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioJaExisteException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class CadastrarUsuarioService implements CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public CadastrarUsuarioService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Usuario executar(CadastrarUsuarioCommand command) {
        String email = command.email().toLowerCase().strip();

        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            throw new UsuarioJaExisteException(email);
        }

        PerfilUsuario perfil = command.perfil() != null ? command.perfil() : PerfilUsuario.USUARIO;

        Usuario usuario = new Usuario(
            null,
            UUID.randomUUID(),
            command.nome().strip(),
            email,
            passwordHasher.hash(command.senha()),
            perfil,
            OffsetDateTime.now()
        );

        return usuarioRepository.salvar(usuario);
    }
}
