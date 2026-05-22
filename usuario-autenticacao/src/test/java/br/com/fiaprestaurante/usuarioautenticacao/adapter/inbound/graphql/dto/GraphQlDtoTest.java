package br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto;

import br.com.fiaprestaurante.usuarioautenticacao.TestFixtures;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlDtoTest {

    @Test
    void deveExporCadastrarUsuarioInput() {
        CadastrarUsuarioInput input = new CadastrarUsuarioInput();
        input.setNome(TestFixtures.NOME);
        input.setEmail(TestFixtures.EMAIL);
        input.setSenha(TestFixtures.SENHA);
        input.setPerfil(PerfilUsuario.USUARIO);

        assertThat(input.getNome()).isEqualTo(TestFixtures.NOME);
        assertThat(input.getEmail()).isEqualTo(TestFixtures.EMAIL);
        assertThat(input.getSenha()).isEqualTo(TestFixtures.SENHA);
        assertThat(input.getPerfil()).isEqualTo(PerfilUsuario.USUARIO);
    }

    @Test
    void deveExporLoginInput() {
        LoginInput input = new LoginInput();
        input.setEmail(TestFixtures.EMAIL);
        input.setSenha(TestFixtures.SENHA);

        assertThat(input.getEmail()).isEqualTo(TestFixtures.EMAIL);
        assertThat(input.getSenha()).isEqualTo(TestFixtures.SENHA);
    }

    @Test
    void deveCriarUsuarioPayloadAPartirDoDominio() {
        UsuarioPayload payload = UsuarioPayload.from(TestFixtures.usuario());

        assertThat(payload.getId()).isEqualTo(TestFixtures.PUBLIC_ID.toString());
        assertThat(payload.getNome()).isEqualTo(TestFixtures.NOME);
        assertThat(payload.getEmail()).isEqualTo(TestFixtures.EMAIL);
        assertThat(payload.getPerfil()).isEqualTo("USUARIO");
        assertThat(payload.getCriadoEm()).isEqualTo(TestFixtures.CRIADO_EM.toString());
    }

    @Test
    void deveCriarAuthPayloadAPartirDaSessao() {
        AuthPayload payload = AuthPayload.from(TestFixtures.sessaoUsuario());

        assertThat(payload.getToken()).isEqualTo(TestFixtures.TOKEN);
        assertThat(payload.getTipoToken()).isEqualTo("Bearer");
        assertThat(payload.getExpiraEmSegundos()).isEqualTo(3600L);
        assertThat(payload.getUsuario().getId()).isEqualTo(TestFixtures.PUBLIC_ID.toString());
    }
}
