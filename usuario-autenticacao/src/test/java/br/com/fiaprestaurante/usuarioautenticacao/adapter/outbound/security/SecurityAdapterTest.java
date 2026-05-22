package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.security;

import br.com.fiaprestaurante.usuarioautenticacao.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityAdapterTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Test
    void deveGerarHashBcryptEValidarSenha() {
        BcryptPasswordHasher hasher = new BcryptPasswordHasher();

        String hash = hasher.hash(TestFixtures.SENHA);

        assertThat(hash).isNotEqualTo(TestFixtures.SENHA);
        assertThat(hasher.matches(TestFixtures.SENHA, hash)).isTrue();
        assertThat(hasher.matches("errada", hash)).isFalse();
    }

    @Test
    void deveGerarTokenJwtComClaimsDoUsuario() {
        JwtTokenProvider provider = new JwtTokenProvider(jwtEncoder);
        Jwt jwt = Jwt.withTokenValue(TestFixtures.TOKEN)
            .header("alg", "RS256")
            .subject(TestFixtures.PUBLIC_ID.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600L))
            .build();
        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(jwt);
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        String token = provider.gerarToken(TestFixtures.usuario());

        assertThat(token).isEqualTo(TestFixtures.TOKEN);
        assertThat(provider.tempoDeExpiracaoEmSegundos()).isEqualTo(3600L);
        verify(jwtEncoder).encode(captor.capture());
        assertThat(captor.getValue().getJwsHeader().getAlgorithm().getName()).isEqualTo("RS256");
        assertThat((Object) captor.getValue().getClaims().getClaim("iss")).isEqualTo("fiap-restaurante");
        assertThat(captor.getValue().getClaims().getSubject()).isEqualTo(TestFixtures.PUBLIC_ID.toString());
        assertThat((Object) captor.getValue().getClaims().getClaim("email")).isEqualTo(TestFixtures.EMAIL);
        assertThat((Object) captor.getValue().getClaims().getClaim("nome")).isEqualTo(TestFixtures.NOME);
        assertThat((Object) captor.getValue().getClaims().getClaim("groups")).isEqualTo(Set.of("USUARIO"));
        assertThat(captor.getValue().getClaims().getExpiresAt())
            .isAfter(captor.getValue().getClaims().getIssuedAt());
    }
}
