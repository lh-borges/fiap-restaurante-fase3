package br.com.fiaprestaurante.usuarioautenticacao.application.usecase;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.AutenticarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.SessaoUsuario;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.AutenticarUsuarioUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.PasswordHasher;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.TokenProvider;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.CredenciaisInvalidasException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

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
        String email = command.email().toLowerCase().strip();

        Usuario usuario = usuarioRepository.buscarPorEmail(email)
            .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordHasher.matches(command.senha(), usuario.senhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        String token = tokenProvider.gerarToken(usuario);
        return new SessaoUsuario(token, tokenProvider.tempoDeExpiracaoEmSegundos(), usuario);
    }
}
