package br.com.fiaprestaurante.pagamento.application.port.output;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Porta de saída para o gateway de pagamento externo (procpag) — abstrai
 * a comunicação HTTP de modo que o use case não precise conhecer detalhes
 * de protocolo, retry, circuit breaker etc.
 *
 * <p>A implementação padrão é {@code ExternalPaymentClient}, que aplica as
 * políticas de resiliência exigidas pelo requisito 5.4 da fase 3
 * (Circuit Breaker, Retry, Timeout, Fallback).
 *
 * @author Danilo Fernando
 */
public interface PaymentGateway {

    /**
     * Envia uma requisição de cobrança ao gateway externo.
     *
     * @param pedidoId identificador do pedido (também usado como identificador
     *                 lógico da transação externa, para garantir idempotência no procpag)
     * @param valor    valor a ser cobrado
     * @return {@code true} se o gateway autorizou (HTTP 2xx), {@code false} se rejeitou
     * @throws RuntimeException quando há falha de comunicação (timeout, conexão recusada,
     *                          5xx etc.); o caller é responsável por capturar e tratar como
     *                          pendência se necessário
     */
    boolean processar(UUID pedidoId, BigDecimal valor);
}
