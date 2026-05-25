package br.com.fiaprestaurante.restauranteservice.application.port.output;

import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida — persistencia do agregado {@link PedidoCozinha}.
 *
 * <p>O adapter {@code PedidoCozinhaRepositoryAdapter} implementa esta interface
 * delegando ao Spring Data JPA, mantendo o dominio livre de qualquer
 * dependencia de framework.
 *
 * @author Danilo Fernando
 */
public interface PedidoCozinhaRepository {

    /**
     * Persiste ou atualiza um agregado.
     *
     * @param pedidoCozinha agregado a salvar
     * @return o mesmo agregado (para encadeamento)
     */
    PedidoCozinha salvar(PedidoCozinha pedidoCozinha);

    /**
     * @param id identidade interna do pedido na cozinha
     * @return o agregado, ou {@code Optional.empty()} se nao encontrado
     */
    Optional<PedidoCozinha> porId(UUID id);

    /**
     * Lista todos os pedidos — opcionalmente filtrados por status — ordenados
     * pelo {@code createdAt} ascendente (FIFO).
     *
     * @param filtroStatus status para filtrar; {@code null} retorna todos
     * @return lista (possivelmente vazia) de pedidos
     */
    List<PedidoCozinha> listar(StatusCozinha filtroStatus);
}
