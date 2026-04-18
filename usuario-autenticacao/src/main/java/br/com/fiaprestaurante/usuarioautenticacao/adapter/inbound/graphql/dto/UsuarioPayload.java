package br.com.fiaprestaurante.usuarioautenticacao.adapter.inbound.graphql.dto;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;

public class UsuarioPayload {

    private final String id;
    private final String nome;
    private final String email;
    private final String perfil;
    private final String criadoEm;

    private UsuarioPayload(String id, String nome, String email, String perfil, String criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.criadoEm = criadoEm;
    }

    public static UsuarioPayload from(Usuario usuario) {
        return new UsuarioPayload(
            usuario.publicId().toString(),
            usuario.nome(),
            usuario.email(),
            usuario.perfil().name(),
            usuario.criadoEm().toString()
        );
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getPerfil() { return perfil; }
    public String getCriadoEm() { return criadoEm; }
}
