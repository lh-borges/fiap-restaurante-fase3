package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.security;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link JwtTokenProvider} - mocka o {@link JwtEncoder}
 * e valida o conteudo das claims geradas (subject, email, nome, groups e
 * expiracao).
 *
 * @author Danilo Fernando
 */
class JwtTokenProviderTest {

    private static final UUID PUBLIC_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final String TOKEN_FAKE = "eyJ.fake.token";

    private JwtEncoder jwtEncoder;
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        jwtEncoder = mock(JwtEncoder.class);
        provider = new JwtTokenProvider(jwtEncoder);
    }

    private Usuario usuario(PerfilUsuario perfil) {
        return new Usuario(1L, PUBLIC_ID, "Ana Lima", "ana@fiap.com",
                "hash", perfil, OffsetDateTime.now());
    }

    @Test
    void deveGerarTokenComClaimsCorretas() {
        Jwt jwtFake = Jwt.withTokenValue(TOKEN_FAKE)
                .header("alg", "RS256")
                .claim("sub", PUBLIC_ID.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class)))
                .thenReturn(jwtFake);

        String token = provider.gerarToken(usuario(PerfilUsuario.USUARIO));

        assertThat(token).isEqualTo(TOKEN_FAKE);

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        Map<String, Object> claims = captor.getValue().getClaims().getClaims();
        assertThat(claims.get("sub")).isEqualTo(PUBLIC_ID.toString());
        assertThat(claims.get("email")).isEqualTo("ana@fiap.com");
        assertThat(claims.get("nome")).isEqualTo("Ana Lima");
        assertThat(claims.get("iss")).isEqualTo("fiap-restaurante");
        assertThat(claims.get("groups")).isInstanceOf(Set.class);
        Set<?> groups = (Set<?>) claims.get("groups");
        assertThat(groups).hasSize(1);
        assertThat(groups.iterator().next()).isEqualTo("USUARIO");
    }

    @Test
    void devePropagarPerfilDonoNoGroupsClaim() {
        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue(TOKEN_FAKE).header("alg", "RS256").claim("sub", "x").build());

        provider.gerarToken(usuario(PerfilUsuario.DONO_RESTAURANTE));

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        Set<?> groups = (Set<?>) captor.getValue().getClaims().getClaims().get("groups");
        assertThat(groups).hasSize(1);
        assertThat(groups.iterator().next()).isEqualTo("DONO_RESTAURANTE");
    }

    @Test
    void tempoDeExpiracaoDeveSer3600Segundos() {
        assertThat(provider.tempoDeExpiracaoEmSegundos()).isEqualTo(3600L);
    }
}
