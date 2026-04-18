package br.com.fiaprestaurante.usuarioautenticacao.application.dto;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;

public record SessaoUsuario(
    String token,
    long expiraEmSegundos,
    Usuario usuario
) {}
