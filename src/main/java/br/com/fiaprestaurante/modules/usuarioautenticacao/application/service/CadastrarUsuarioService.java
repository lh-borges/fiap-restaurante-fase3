package br.com.fiaprestaurante.modules.usuarioautenticacao.application.service;

import br.com.fiaprestaurante.modules.usuarioautenticacao.application.command.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.in.CadastrarUsuarioUseCase;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.PasswordHasher;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.UsuarioRepository;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.exception.UsuarioJaExisteException;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.PerfilUsuario;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class CadastrarUsuarioService implements CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    @Inject
    public CadastrarUsuarioService(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public Usuario executar(CadastrarUsuarioCommand command) {
        validar(command);

        String emailNormalizado = normalizarEmail(command.email());
        usuarioRepository.buscarPorEmail(emailNormalizado)
            .ifPresent(usuario -> {
                throw new UsuarioJaExisteException(emailNormalizado);
            });

        PerfilUsuario perfil = command.perfil() == null ? PerfilUsuario.CLIENTE : command.perfil();

        Usuario usuario = new Usuario(
            null,
            UUID.randomUUID(),
            command.nome().trim(),
            emailNormalizado,
            passwordHasher.hash(command.senha()),
            perfil,
            OffsetDateTime.now(ZoneOffset.UTC)
        );

        return usuarioRepository.salvar(usuario);
    }

    private void validar(CadastrarUsuarioCommand command) {
        Objects.requireNonNull(command, "O comando de cadastro e obrigatorio.");
        if (command.nome() == null || command.nome().isBlank()) {
            throw new IllegalArgumentException("O nome do usuario e obrigatorio.");
        }
        if (command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException("O email do usuario e obrigatorio.");
        }
        if (command.senha() == null || command.senha().length() < 6) {
            throw new IllegalArgumentException("A senha deve possuir ao menos 6 caracteres.");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
