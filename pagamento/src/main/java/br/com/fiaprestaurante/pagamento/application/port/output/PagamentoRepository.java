package br.com.fiaprestaurante.pagamento.application.port.output;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para persistência de pagamentos.
 *
 * <p>Mantém a camada de aplicação independente do mecanismo de persistência
 * concreto. A implementação default é
 * {@code PagamentoRepositoryAdapter} (JPA/MySQL), mas qualquer outro backend
 * pode ser plugado sem alterar os use cases.
 *
 * @author Danilo Fernando
 */
public interface PagamentoRepository {

    /**
     * Persiste um novo pagamento ou atualiza um existente.
     *
     * @param pagamento entidade a ser persistida (não pode ser {@code null})
     * @return a entidade reidratada após a persistência (mesmas referências de id e timestamps)
     */
    Pagamento salvar(Pagamento pagamento);

    /**
     * Busca um pagamento pelo identificador do pedido associado.
     *
     * <p>Como a relação pedido↔pagamento é 1-para-1 (garantido por índice
     * único no banco), retorna no máximo um resultado.
     *
     * @param pedidoId identificador do pedido
     * @return {@link Optional} com o pagamento, ou vazio se não houver
     */
    Optional<Pagamento> buscarPorPedidoId(UUID pedidoId);

    /**
     * Lista pagamentos atualmente em status {@code PENDENTE}, ordenados pela
     * data de criação (mais antigos primeiro).
     *
     * @param limite número máximo de registros a retornar (clamping aplicado pela implementação)
     * @return lista (possivelmente vazia) de pagamentos pendentes
     */
    List<Pagamento> listarPendentes(int limite);
}
