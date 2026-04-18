package br.com.fiaprestaurante.modules.usuarioautenticacao.adapter.in.graphql.dto;

import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.PerfilUsuario;

public class CadastrarUsuarioInput {

    private String nome;
    private String email;
    private String senha;
    private PerfilUsuario perfil;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }
}
