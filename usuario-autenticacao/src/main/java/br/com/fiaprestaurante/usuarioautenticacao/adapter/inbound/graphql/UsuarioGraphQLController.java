package br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql;

import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.AuthPayload;
import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.CadastrarUsuarioInput;
import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.LoginInput;
import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.UsuarioPayload;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.AutenticarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.AutenticarUsuarioUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.BuscarUsuarioAtualUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.CadastrarUsuarioUseCase;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class UsuarioGraphQLController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final BuscarUsuarioAtualUseCase buscarUsuarioAtualUseCase;

    public UsuarioGraphQLController(
        CadastrarUsuarioUseCase cadastrarUsuarioUseCase,
        AutenticarUsuarioUseCase autenticarUsuarioUseCase,
        BuscarUsuarioAtualUseCase buscarUsuarioAtualUseCase
    ) {
        this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.buscarUsuarioAtualUseCase = buscarUsuarioAtualUseCase;
    }

    @MutationMapping
    @PreAuthorize("permitAll()")
    public UsuarioPayload cadastrarUsuario(@Argument CadastrarUsuarioInput input) {
        return UsuarioPayload.from(
            cadastrarUsuarioUseCase.executar(new CadastrarUsuarioCommand(
                input.getNome(),
                input.getEmail(),
                input.getSenha(),
                input.getPerfil()
            ))
        );
    }

    @MutationMapping
    @PreAuthorize("permitAll()")
    public AuthPayload login(@Argument LoginInput input) {
        return AuthPayload.from(
            autenticarUsuarioUseCase.executar(new AutenticarUsuarioCommand(
                input.getEmail(),
                input.getSenha()
            ))
        );
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public UsuarioPayload me(@AuthenticationPrincipal Jwt jwt) {
        UUID publicId = UUID.fromString(jwt.getSubject());
        return UsuarioPayload.from(buscarUsuarioAtualUseCase.executar(publicId));
    }
}
