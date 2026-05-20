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

/**
 * Adapter de saída para o gateway de pagamento externo (procpag), conforme
 * requisitos 4.4 e 5.4 da fase 3.
 *
 * <p>Aplica três das quatro políticas de resiliência exigidas via
 * Resilience4j:
 * <ul>
 *   <li><strong>Retry</strong> ({@code @Retry}): tenta {@code max-attempts=3}
 *       vezes com {@code wait-duration=2s} entre tentativas; ignora
 *       {@code CallNotPermittedException} para fail-fast quando o CB já está OPEN;</li>
 *   <li><strong>Circuit Breaker</strong> ({@code @CircuitBreaker}): abre após
 *       50% de falhas em uma janela deslizante de 5 chamadas; transita
 *       automaticamente de OPEN para HALF_OPEN após 30s;</li>
 *   <li><strong>Timeout</strong>: configurado no {@link RestTemplate} via
 *       {@code BeanConfig} (connect 3s / read 5s) — Resilience4j
 *       {@code @TimeLimiter} exige métodos assíncronos, então usamos o
 *       timeout síncrono do cliente HTTP.</li>
 * </ul>
 *
 * <p>A quarta política — <strong>Fallback</strong> — é tratada no caller
 * ({@code ProcessarPagamentoService.tentarGateway}): qualquer exceção que
 * escapa daqui é capturada e considerada como falha do gateway, gerando
 * pagamento PENDENTE.
 *
 * @author Danilo Fernando
 */
@Component
public class ExternalPaymentClient implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(ExternalPaymentClient.class);

    private static final String CB_NAME = "paymentService";

    private final RestTemplate restTemplate;
    private final String endpoint;

    /**
     * Constrói o client com a URL completa do endpoint procpag pré-resolvida.
     *
     * @param restTemplate cliente HTTP com timeouts já configurados
     * @param baseUrl      URL base do procpag (por exemplo {@code http://procpag:8089})
     * @param path         path do endpoint (por exemplo {@code /requisicao})
     */
    public ExternalPaymentClient(RestTemplate restTemplate,
                                 @Value("${pagamento.gateway.url}") String baseUrl,
                                 @Value("${pagamento.gateway.path}") String path) {
        this.restTemplate = restTemplate;
        this.endpoint = baseUrl + path;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Monta o body conforme o contrato do procpag
     * ({@code pagamento_id}, {@code valor}, {@code cliente_id}) e envia POST.
     * Considera autorizado se o status HTTP for {@code 2xx}.
     *
     * @throws RuntimeException se o gateway estiver inalcançável e todas as
     *                          tentativas do {@code @Retry} falharem, ou se o
     *                          circuit breaker recusar a chamada com
     *                          {@code CallNotPermittedException}
     */
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
