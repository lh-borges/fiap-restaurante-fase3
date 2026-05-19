package br.com.fiaprestaurante.pagamento.adapter.outbound.http;

import br.com.fiaprestaurante.pagamento.application.port.output.PaymentGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class ExternalPaymentClient implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(ExternalPaymentClient.class);

    private static final String CB_NAME = "paymentService";

    private final RestTemplate restTemplate;
    private final String endpoint;

    public ExternalPaymentClient(RestTemplate restTemplate,
                                 @Value("${pagamento.gateway.url}") String baseUrl,
                                 @Value("${pagamento.gateway.path}") String path) {
        this.restTemplate = restTemplate;
        this.endpoint = baseUrl + path;
    }

    @Override
    @Retry(name = CB_NAME)
    @CircuitBreaker(name = CB_NAME)
    public boolean processar(UUID pedidoId, BigDecimal valor) {
        log.info("Chamando gateway externo: endpoint={} pedidoId={} valor={}", endpoint, pedidoId, valor);

        Map<String, Object> body = Map.of(
                "pagamento_id", pedidoId.toString(),
                "valor", valor.intValue(),
                "cliente_id", pedidoId.toString()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(body, headers), Map.class);
        log.info("Gateway externo respondeu: pedidoId={} status={} body={}",
                pedidoId, response.getStatusCode(), response.getBody());
        return response.getStatusCode().is2xxSuccessful();
    }
}
