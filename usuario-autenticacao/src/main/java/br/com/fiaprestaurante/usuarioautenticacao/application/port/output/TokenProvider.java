package br.com.fiaprestaurante.usuarioautenticacao.application.port.output;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;

public interface TokenProvider {

    String gerarToken(Usuario usuario);

    long tempoDeExpiracaoEmSegundos();
}
