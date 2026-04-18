package br.com.fiaprestaurante.usuarioautenticacao.application.dto;

import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;

public record CadastrarUsuarioCommand(
    String nome,
    String email,
    String senha,
    PerfilUsuario perfil
) {}
