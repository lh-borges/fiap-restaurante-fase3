package br.com.fiaprestaurante.pagamento.domain.valueobject;

/**
 * Estados possíveis de um pagamento ao longo do seu ciclo de vida.
 *
 * <p>Um pagamento nasce como {@link #PENDENTE} (aguardando confirmação do
 * gateway externo). Pode então:
 * <ul>
 *   <li>Transitar para {@link #APROVADO} quando o gateway autoriza a cobrança;</li>
 *   <li>Permanecer em {@link #PENDENTE} quando há falha temporária do gateway,
 *       sendo reprocessado pelo worker {@code ReprocessamentoPagamentoWorker};</li>
 *   <li>Transitar para {@link #RECUSADO} quando o gateway nega definitivamente
 *       a cobrança (estado terminal, sem reprocessamento).</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
public enum StatusPagamento {

    /** Pagamento ainda não confirmado pelo gateway externo; será reprocessado. */
    PENDENTE,

    /** Pagamento confirmado pelo gateway externo; estado terminal de sucesso. */
    APROVADO,

    /** Pagamento negado definitivamente pelo gateway; estado terminal de falha. */
    RECUSADO
}
