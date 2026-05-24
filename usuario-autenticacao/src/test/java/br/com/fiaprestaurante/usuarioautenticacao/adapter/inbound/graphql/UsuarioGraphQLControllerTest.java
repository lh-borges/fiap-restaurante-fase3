package br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql;

import br.com.fiaprestaurante.usuarioautenticacao.TestFixtures;
import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.AuthPayload;
import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.CadastrarUsuarioInput;
import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.LoginInput;
import br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto.UsuarioPayload;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.AutenticarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.AutenticarUsuarioUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.BuscarUsuarioAtualUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.CadastrarUsuarioUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioGraphQLControllerTest {

    @Mock
    private CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    @Mock
    private AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    @Mock
    private BuscarUsuarioAtualUseCase buscarUsuarioAtualUseCase;

    @Test
    void deveCadastrarUsuarioDelegandoParaUseCase() {
        UsuarioGraphQLController controller = new UsuarioGraphQLController(
            cadastrarUsuarioUseCase,
            autenticarUsuarioUseCase,
            buscarUsuarioAtualUseCase
        );
        CadastrarUsuarioInput input = new CadastrarUsuarioInput();
        input.setNome(TestFixtures.NOME);
        input.setEmail(TestFixtures.EMAIL);
        input.setSenha(TestFixtures.SENHA);
        input.setPerfil(PerfilUsuario.DONO_RESTAURANTE);
        when(cadastrarUsuarioUseCase.executar(org.mockito.ArgumentMatchers.any(CadastrarUsuarioCommand.class)))
            .thenReturn(TestFixtures.donoRestaurante());
        ArgumentCaptor<CadastrarUsuarioCommand> captor = ArgumentCaptor.forClass(CadastrarUsuarioCommand.class);

        UsuarioPayload payload = controller.cadastrarUsuario(input);

        assertThat(payload.getId()).isEqualTo(TestFixtures.PUBLIC_ID.toString());
        assertThat(payload.getPerfil()).isEqualTo("DONO_RESTAURANTE");
        verify(cadastrarUsuarioUseCase).executar(captor.capture());
        assertThat(captor.getValue().nome()).isEqualTo(TestFixtures.NOME);
        assertThat(captor.getValue().email()).isEqualTo(TestFixtures.EMAIL);
        assertThat(captor.getValue().senha()).isEqualTo(TestFixtures.SENHA);
        assertThat(captor.getValue().perfil()).isEqualTo(PerfilUsuario.DONO_RESTAURANTE);
    }

    @Test
    void deveAutenticarUsuarioDelegandoParaUseCase() {
        UsuarioGraphQLController controller = new UsuarioGraphQLController(
            cadastrarUsuarioUseCase,
            autenticarUsuarioUseCase,
            buscarUsuarioAtualUseCase
        );
        LoginInput input = new LoginInput();
        input.setEmail(TestFixtures.EMAIL);
        input.setSenha(TestFixtures.SENHA);
        when(autenticarUsuarioUseCase.executar(org.mockito.ArgumentMatchers.any(AutenticarUsuarioCommand.class)))
            .thenReturn(TestFixtures.sessaoUsuario());
        ArgumentCaptor<AutenticarUsuarioCommand> captor = ArgumentCaptor.forClass(AutenticarUsuarioCommand.class);

        AuthPayload payload = controller.login(input);

        assertThat(payload.getToken()).isEqualTo(TestFixtures.TOKEN);
        assertThat(payload.getTipoToken()).isEqualTo("Bearer");
        assertThat(payload.getUsuario().getEmail()).isEqualTo(TestFixtures.EMAIL);
        verify(autenticarUsuarioUseCase).executar(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo(TestFixtures.EMAIL);
        assertThat(captor.getValue().senha()).isEqualTo(TestFixtures.SENHA);
    }

    @Test
    void deveConsultarUsuarioAtualAPartirDoSubjectDoJwt() {
        UsuarioGraphQLController controller = new UsuarioGraphQLController(
            cadastrarUsuarioUseCase,
            autenticarUsuarioUseCase,
            buscarUsuarioAtualUseCase
        );
        Jwt jwt = Jwt.withTokenValue(TestFixtures.TOKEN)
            .header("alg", "RS256")
            .subject(TestFixtures.PUBLIC_ID.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600L))
            .build();
        when(buscarUsuarioAtualUseCase.executar(TestFixtures.PUBLIC_ID)).thenReturn(TestFixtures.usuario());

        UsuarioPayload payload = controller.me(jwt);

        assertThat(payload.getId()).isEqualTo(TestFixtures.PUBLIC_ID.toString());
        assertThat(payload.getNome()).isEqualTo(TestFixtures.NOME);
        verify(buscarUsuarioAtualUseCase).executar(TestFixtures.PUBLIC_ID);
    }
}
