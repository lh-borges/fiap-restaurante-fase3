package br.com.fiaprestaurante.modules.usuarioautenticacao.adapter.out.security;

import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.TokenProvider;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

@ApplicationScoped
public class JwtTokenProvider implements TokenProvider {

    private static final long TEMPO_EXPIRACAO_EM_SEGUNDOS = 3600L;

    @Override
    public String gerarToken(Usuario usuario) {
        return Jwt.subject(usuario.publicId().toString())
            .upn(usuario.email())
            .groups(Set.of(usuario.perfil().name()))
            .claim("email", usuario.email())
            .claim("nome", usuario.nome())
            .sign();
    }

    @Override
    public long tempoDeExpiracaoEmSegundos() {
        return TEMPO_EXPIRACAO_EM_SEGUNDOS;
    }
}
