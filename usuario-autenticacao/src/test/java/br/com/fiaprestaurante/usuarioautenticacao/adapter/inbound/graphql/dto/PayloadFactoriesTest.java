package br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.SessaoUsuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios das factories {@link UsuarioPayload#from(Usuario)} e
 * {@link AuthPayload#from(SessaoUsuario)}.
 *
 * @author Danilo Fernando
 */
class PayloadFactoriesTest {

    private static final UUID PUBLIC_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    private Usuario usuario() {
        return new Usuario(1L, PUBLIC_ID, "Ana", "ana@fiap.com",
                "hash", PerfilUsuario.USUARIO,
                OffsetDateTime.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void usuarioPayloadFromDeveConverterPublicIdEPerfilParaString() {
        UsuarioPayload p = UsuarioPayload.from(usuario());

        assertThat(p.getId()).isEqualTo(PUBLIC_ID.toString());
        assertThat(p.getNome()).isEqualTo("Ana");
        assertThat(p.getEmail()).isEqualTo("ana@fiap.com");
        assertThat(p.getPerfil()).isEqualTo("USUARIO");
        assertThat(p.getCriadoEm()).isEqualTo("2026-01-01T10:00Z");
    }

    @Test
    void authPayloadFromDeveCopiarTokenETempoEUsuarioAninhado() {
        SessaoUsuario sessao = new SessaoUsuario("jwt.fake.token", 3600L, usuario());

        AuthPayload p = AuthPayload.from(sessao);

        assertThat(p.getToken()).isEqualTo("jwt.fake.token");
        assertThat(p.getTipoToken()).isEqualTo("Bearer");
        assertThat(p.getExpiraEmSegundos()).isEqualTo(3600L);
        assertThat(p.getUsuario().getEmail()).isEqualTo("ana@fiap.com");
    }
}
