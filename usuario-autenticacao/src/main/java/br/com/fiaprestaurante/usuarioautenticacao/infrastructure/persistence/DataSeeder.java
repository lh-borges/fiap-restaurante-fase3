package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.persistence;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.CadastrarUsuarioUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final UsuarioRepository usuarioRepository;

    public DataSeeder(CadastrarUsuarioUseCase cadastrarUsuarioUseCase, UsuarioRepository usuarioRepository) {
        this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("Dono do Restaurante", "dono@fiap.com", "dono123", PerfilUsuario.DONO_RESTAURANTE);
        seed("Usuario FIAP", "usuario@fiap.com", "usuario123", PerfilUsuario.USUARIO);
    }

    private void seed(String nome, String email, String senha, PerfilUsuario perfil) {
        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            log.info("[Seed] Usuário já existe: {}", email);
            return;
        }
        cadastrarUsuarioUseCase.executar(new CadastrarUsuarioCommand(nome, email, senha, perfil));
        log.info("[Seed] Usuário criado: {} ({})", email, perfil);
    }
}
