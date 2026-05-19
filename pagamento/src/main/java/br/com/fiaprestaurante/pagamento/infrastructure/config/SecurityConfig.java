package br.com.fiaprestaurante.pagamento.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

/**
 * Configuração de segurança — OAuth2 Resource Server validando JWTs
 * assinados em RS256 pelo {@code usuario-autenticacao}.
 *
 * <p>Atende o requisito 5.2 da fase 3:
 * <ul>
 *   <li>Sessão stateless (todos os requests carregam o JWT);</li>
 *   <li>Decoder configurado com a chave pública RSA distribuída em
 *       {@code classpath:keys/publicKey.pem};</li>
 *   <li>Authorities extraídas da claim {@code groups} do token, sem prefixo
 *       (compatível com {@code @PreAuthorize("hasAuthority('USUARIO')")}).</li>
 * </ul>
 *
 * <p>Importante: o filtro autoriza todas as URLs ({@code permitAll}); a
 * proteção é feita a nível de método nos controllers via {@link EnableMethodSecurity}.
 *
 * @author Danilo Fernando
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private Resource publicKeyResource;

    /**
     * Define a {@link SecurityFilterChain} principal.
     *
     * @param http builder do Spring Security
     * @return cadeia de filtros configurada
     * @throws Exception se a leitura da chave pública falhar
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtDecoder jwtDecoder = jwtDecoder();
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .build();
    }

    /**
     * Cria o {@link JwtDecoder} a partir da chave pública RSA no classpath.
     *
     * @return decoder Nimbus configurado
     * @throws IOException se a leitura do arquivo de chave falhar
     */
    private JwtDecoder jwtDecoder() throws IOException {
        RSAPublicKey publicKey = RsaKeyConverters.x509().convert(publicKeyResource.getInputStream());
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Conversor que mapeia a claim {@code groups} do JWT para
     * authorities do Spring Security, sem prefixo {@code SCOPE_} ou {@code ROLE_}.
     *
     * @return conversor pronto para uso pelo Resource Server
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("groups");
        converter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}
