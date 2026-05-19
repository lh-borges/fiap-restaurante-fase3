package br.com.fiaprestaurante.pagamento.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comando de entrada do caso de uso {@code ProcessarPagamentoUseCase}.
 *
 * <p>Encapsula os dados mínimos necessários para iniciar o processamento de
 * pagamento de um pedido: a identidade do pedido e o valor total a ser cobrado.
 *
 * @param pedidoId   identificador do pedido que originou a cobrança
 * @param valorTotal valor monetário a ser processado (deve ser positivo)
 *
 * @author Danilo Fernando
 */
public record ProcessarPagamentoCommand(UUID pedidoId, BigDecimal valorTotal) {
}
