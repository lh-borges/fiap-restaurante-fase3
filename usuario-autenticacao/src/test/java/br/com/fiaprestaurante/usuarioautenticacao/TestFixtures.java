package br.com.fiaprestaurante.usuarioautenticacao;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.SessaoUsuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class TestFixtures {

    public static final Long USUARIO_ID = 10L;
    public static final UUID PUBLIC_ID = UUID.fromString("8c35942d-a54f-449f-9dbb-965bc2373180");
    public static final String NOME = "Ana Maria";
    public static final String EMAIL = "ana@fiap.com";
    public static final String SENHA = "123456";
    public static final String SENHA_HASH = "$2a$10$hash";
    public static final String TOKEN = "jwt-token";
    public static final OffsetDateTime CRIADO_EM = OffsetDateTime.parse("2026-05-21T18:30:00-03:00");

    private TestFixtures() {}

    public static Usuario usuario() {
        return new Usuario(
            USUARIO_ID,
            PUBLIC_ID,
            NOME,
            EMAIL,
            SENHA_HASH,
            PerfilUsuario.USUARIO,
            CRIADO_EM
        );
    }

    public static Usuario donoRestaurante() {
        return new Usuario(
            USUARIO_ID,
            PUBLIC_ID,
            NOME,
            EMAIL,
            SENHA_HASH,
            PerfilUsuario.DONO_RESTAURANTE,
            CRIADO_EM
        );
    }

    public static SessaoUsuario sessaoUsuario() {
        return new SessaoUsuario(TOKEN, 3600L, usuario());
    }
}
