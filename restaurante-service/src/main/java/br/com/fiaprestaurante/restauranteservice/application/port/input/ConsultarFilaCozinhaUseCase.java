package br.com.fiaprestaurante.restauranteservice.application.port.input;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de entrada — consultas de leitura sobre a fila da cozinha.
 *
 * @author Danilo Fernando
 */
public interface ConsultarFilaCozinhaUseCase {

    /**
     * Lista todos os pedidos da fila — opcionalmente filtrados por status —
     * do mais antigo para o mais recente (ordem FIFO de preparo).
     *
     * @param filtroStatus status para filtrar; {@code null} retorna todos
     * @return lista (possivelmente vazia) de pedidos
     */
    List<PedidoCozinhaResponse> listar(StatusCozinha filtroStatus);

    /**
     * Busca um pedido especifico da cozinha pelo seu ID.
     *
     * @param pedidoCozinhaId identidade interna na cozinha
     * @return o pedido, ou {@code Optional.empty()} se nao encontrado
     */
    Optional<PedidoCozinhaResponse> porId(UUID pedidoCozinhaId);
}
