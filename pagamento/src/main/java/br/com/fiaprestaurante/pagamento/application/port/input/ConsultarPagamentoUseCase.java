package br.com.fiaprestaurante.pagamento.application.port.input;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de entrada (use case) para consultas em modo somente-leitura sobre
 * pagamentos.
 *
 * <p>Usada pelo {@code PagamentoGraphQLController} para expor as queries
 * {@code pagamentoPorPedido} e {@code pagamentosPendentes}.
 *
 * @author Danilo Fernando
 */
public interface ConsultarPagamentoUseCase {

    /**
     * Busca o pagamento associado a um determinado pedido.
     *
     * @param pedidoId identificador do pedido
     * @return {@link Optional} contendo o pagamento, ou vazio se nenhum existir
     */
    Optional<PagamentoResponse> porPedidoId(UUID pedidoId);

    /**
     * Lista todos os pagamentos atualmente em {@code PENDENTE}.
     *
     * <p>Útil para diagnóstico operacional e dashboards. A ordenação segue a
     * data de criação (mais antigos primeiro).
     *
     * @return lista (possivelmente vazia) de pagamentos pendentes
     */
    List<PagamentoResponse> pendentes();
}
