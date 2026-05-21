package br.com.fiaprestaurante.pagamento.adapter.outbound.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalPaymentClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ExternalPaymentClient client;

    @BeforeEach
    void setUp() {
        client = new ExternalPaymentClient(restTemplate, "http://procpag:8089", "/requisicao");
    }

    @Test
    void deveEnviarPostParaGatewayERetornarTrueEmStatus2xx() {
        UUID pedidoId = UUID.randomUUID();
        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.captor();
        when(restTemplate.postForEntity(eq("http://procpag:8089/requisicao"), entityCaptor.capture(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", "ok")));

        boolean aprovado = client.processar(pedidoId, new BigDecimal("42.90"));

        assertThat(aprovado).isTrue();
        HttpEntity<Map<String, Object>> request = entityCaptor.getValue();
        assertThat(request.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getBody())
                .containsEntry("pagamento_id", pedidoId.toString())
                .containsEntry("valor", 42)
                .containsEntry("cliente_id", pedidoId.toString());
    }

    @Test
    void deveRetornarFalseEmStatusNao2xx() {
        UUID pedidoId = UUID.randomUUID();
        when(restTemplate.postForEntity(eq("http://procpag:8089/requisicao"),
                org.mockito.ArgumentMatchers.any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of()));

        boolean aprovado = client.processar(pedidoId, new BigDecimal("42.90"));

        assertThat(aprovado).isFalse();
    }
}
