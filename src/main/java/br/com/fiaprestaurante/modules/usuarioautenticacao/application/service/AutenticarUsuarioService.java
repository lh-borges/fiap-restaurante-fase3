package br.com.fiaprestaurante.modules.usuarioautenticacao.application.service;

import br.com.fiaprestaurante.modules.usuarioautenticacao.application.command.AutenticarUsuarioCommand;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.model.SessaoUsuario;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.in.AutenticarUsuarioUseCase;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.PasswordHasher;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.TokenProvider;
import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.UsuarioRepository;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.exception.CredenciaisInvalidasException;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    @Inject
    public AutenticarUsuarioService(
        UsuarioRepository usuarioRepository,
        PasswordHasher passwordHasher,
        TokenProvider tokenProvider
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public SessaoUsuario executar(AutenticarUsuarioCommand command) {
        Objects.requireNonNull(command, "O comando de autenticacao e obrigatorio.");
        if (command.email() == null || command.email().isBlank() || command.senha() == null || command.senha().isBlank()) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(command.email().trim().toLowerCase())
            .filter(candidato -> passwordHasher.matches(command.senha(), candidato.senhaHash()))
            .orElseThrow(CredenciaisInvalidasException::new);

        return new SessaoUsuario(
            tokenProvider.gerarToken(usuario),
            tokenProvider.tempoDeExpiracaoEmSegundos(),
            usuario
        );
    }
}
