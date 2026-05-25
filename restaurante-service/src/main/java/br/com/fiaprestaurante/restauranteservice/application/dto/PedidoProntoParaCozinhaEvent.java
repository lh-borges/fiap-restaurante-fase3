package br.com.fiaprestaurante.restauranteservice.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evento consumido do topico Kafka {@code pedido.pronto-para-cozinha},
 * publicado pelo {@code restaurante-pedido} apos a confirmacao do pagamento.
 *
 * <p>Carrega todos os dados de que a cozinha precisa para iniciar o preparo:
 * identidade do pedido, restaurante e itens (sem preco — irrelevante aqui).
 *
 * @param pedidoId       identidade do pedido original
 * @param restauranteId  identificador do restaurante
 * @param itens          lista de itens (snapshot)
 * @param timestamp      instante em que o pedido entrou na fila da cozinha
 *
 * @author Danilo Fernando
 */
public record PedidoProntoParaCozinhaEvent(
        UUID pedidoId,
        UUID restauranteId,
        List<Item> itens,
        Instant timestamp
) {

    /**
     * Snapshot do item enviado pelo restaurante-pedido para a cozinha.
     *
     * @param produtoId  identificador do produto
     * @param nome       nome do produto
     * @param quantidade quantidade a preparar
     */
    public record Item(UUID produtoId, String nome, int quantidade) {
    }
}
