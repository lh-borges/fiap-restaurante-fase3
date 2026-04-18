package br.com.fiaprestaurante.modules.usuarioautenticacao.application.model;

import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;

public record SessaoUsuario(
    String token,
    long expiraEmSegundos,
    Usuario usuario
) {
}
