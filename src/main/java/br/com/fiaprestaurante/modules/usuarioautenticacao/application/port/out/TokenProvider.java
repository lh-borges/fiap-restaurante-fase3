package br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out;

import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;

public interface TokenProvider {

    String gerarToken(Usuario usuario);

    long tempoDeExpiracaoEmSegundos();
}
