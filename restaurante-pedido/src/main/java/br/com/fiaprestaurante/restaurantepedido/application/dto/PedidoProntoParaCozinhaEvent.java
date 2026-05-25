package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evento publicado no topico Kafka {@code pedido.pronto-para-cozinha} apos
 * a confirmacao do pagamento. Consumido pelo {@code restaurante-service}
 * (cozinha) para iniciar o preparo.
 *
 * <p>Carrega snapshot dos itens do pedido (sem preco — irrelevante para a
 * cozinha) para que o restaurante-service nao precise consultar o
 * restaurante-pedido sincronamente.
 *
 * @param pedidoId       identidade do pedido aprovado
 * @param restauranteId  identificador do restaurante que vai preparar
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
     * Snapshot de um item enviado para a cozinha.
     *
     * @param produtoId  identificador do produto
     * @param nome       nome do produto
     * @param quantidade quantidade a preparar
     */
    public record Item(UUID produtoId, String nome, int quantidade) {
    }
}
