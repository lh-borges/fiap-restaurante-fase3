package br.com.fiaprestaurante.pagamento.adapter.outbound.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link ExternalPaymentClient} - mocka o
 * {@link RestTemplate} e valida o mapeamento HTTP → boolean.
 *
 * <p>As anotacoes {@code @Retry} e {@code @CircuitBreaker} sao proxies
 * AOP do Resilience4j; em testes unitarios chamamos o metodo direto
 * (sem proxy), entao validamos so a logica HTTP em si.
 *
 * @author Danilo Fernando
 */
class ExternalPaymentClientTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final String BASE_URL = "http://procpag:8089";
    private static final String PATH = "/requisicao";

    private RestTemplate restTemplate;
    private ExternalPaymentClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new ExternalPaymentClient(restTemplate, BASE_URL, PATH);
    }

    @Test
    void deveRetornarTrueQuandoGatewayResponde201() {
        when(restTemplate.postForEntity(eq(BASE_URL + PATH), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("status", "accepted"), HttpStatus.CREATED));

        boolean autorizado = client.processar(PEDIDO_ID, new BigDecimal("59.30"));

        assertThat(autorizado).isTrue();
    }

    @Test
    void deveRetornarTrueQuandoGatewayResponde200() {
        when(restTemplate.postForEntity(eq(BASE_URL + PATH), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of(), HttpStatus.OK));

        boolean autorizado = client.processar(PEDIDO_ID, BigDecimal.TEN);

        assertThat(autorizado).isTrue();
    }

    @Test
    void devePropagarExcecaoQuandoRestTemplateLanca() {
        when(restTemplate.postForEntity(eq(BASE_URL + PATH), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("connection refused"));

        assertThatThrownBy(() -> client.processar(PEDIDO_ID, BigDecimal.TEN))
                .isInstanceOf(RestClientException.class);
    }
}
