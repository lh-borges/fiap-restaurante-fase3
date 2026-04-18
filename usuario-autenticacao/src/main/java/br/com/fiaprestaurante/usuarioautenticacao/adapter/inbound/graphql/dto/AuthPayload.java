package br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.SessaoUsuario;

public class AuthPayload {

    private final String token;
    private final String tipoToken = "Bearer";
    private final long expiraEmSegundos;
    private final UsuarioPayload usuario;

    private AuthPayload(String token, long expiraEmSegundos, UsuarioPayload usuario) {
        this.token = token;
        this.expiraEmSegundos = expiraEmSegundos;
        this.usuario = usuario;
    }

    public static AuthPayload from(SessaoUsuario sessao) {
        return new AuthPayload(
            sessao.token(),
            sessao.expiraEmSegundos(),
            UsuarioPayload.from(sessao.usuario())
        );
    }

    public String getToken() { return token; }
    public String getTipoToken() { return tipoToken; }
    public long getExpiraEmSegundos() { return expiraEmSegundos; }
    public UsuarioPayload getUsuario() { return usuario; }
}
