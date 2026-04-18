package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.security;

import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.TokenProvider;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
public class JwtTokenProvider implements TokenProvider {

    private static final long TEMPO_EXPIRACAO_EM_SEGUNDOS = 3600L;

    private final JwtEncoder jwtEncoder;

    public JwtTokenProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("fiap-restaurante")
            .issuedAt(agora)
            .expiresAt(agora.plusSeconds(TEMPO_EXPIRACAO_EM_SEGUNDOS))
            .subject(usuario.publicId().toString())
            .claim("email", usuario.email())
            .claim("nome", usuario.nome())
            .claim("groups", Set.of(usuario.perfil().name()))
            .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public long tempoDeExpiracaoEmSegundos() {
        return TEMPO_EXPIRACAO_EM_SEGUNDOS;
    }
}
