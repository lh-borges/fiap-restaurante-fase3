package br.com.fiaprestaurante.pagamento.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void deveConverterClaimGroupsParaAuthoritiesSemPrefixo() {
        var converter = new SecurityConfig().jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "usuario")
                .claim("groups", List.of("USUARIO", "DONO_RESTAURANTE"))
                .issuedAt(Instant.parse("2026-05-21T10:00:00Z"))
                .expiresAt(Instant.parse("2026-05-21T11:00:00Z"))
                .build();

        var authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("USUARIO", "DONO_RESTAURANTE");
    }
}
