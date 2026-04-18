package br.com.fiaprestaurante.modules.usuarioautenticacao.adapter.in.graphql.dto;

import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;

public class UsuarioPayload {

    private String id;
    private String nome;
    private String email;
    private String perfil;
    private String criadoEm;

    public static UsuarioPayload from(Usuario usuario) {
        UsuarioPayload payload = new UsuarioPayload();
        payload.id = usuario.publicId().toString();
        payload.nome = usuario.nome();
        payload.email = usuario.email();
        payload.perfil = usuario.perfil().name();
        payload.criadoEm = usuario.criadoEm().toString();
        return payload;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getCriadoEm() {
        return criadoEm;
    }
}
