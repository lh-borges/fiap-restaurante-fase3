package br.com.fiaprestaurante.pagamento.application.port.output;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {

    boolean processar(UUID pedidoId, BigDecimal valor);
}
