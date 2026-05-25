package br.com.fiaprestaurante.pagamento.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Beans utilitários compartilhados pelo módulo.
 *
 * <p>Spring Boot 4 removeu {@code RestTemplateBuilder} do autoconfigure, então
 * configuramos o {@link RestTemplate} manualmente com timeouts explícitos.
 * Esses timeouts atendem o requisito 5.4 da fase 3 (Timeout na chamada ao
 * gateway externo).
 *
 * @author Danilo Fernando
 */
@Configuration
public class BeanConfig {

    /**
     * {@link RestTemplate} usado pelo {@code ExternalPaymentClient} para
     * conversar com o procpag.
     *
     * @param connectTimeoutMs timeout para estabelecer conexão TCP (ms)
     * @param readTimeoutMs    timeout para receber resposta após enviar requisição (ms)
     * @return template configurado
     */
    @Bean
    public RestTemplate restTemplate(@Value("${pagamento.gateway.connect-timeout-ms:3000}") int connectTimeoutMs,
                                     @Value("${pagamento.gateway.read-timeout-ms:5000}") int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return new RestTemplate(factory);
    }
}
