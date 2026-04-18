package br.com.fiaprestaurante.modules.usuarioautenticacao.application.command;

import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.PerfilUsuario;

public record CadastrarUsuarioCommand(
    String nome,
    String email,
    String senha,
    PerfilUsuario perfil
) {
}
