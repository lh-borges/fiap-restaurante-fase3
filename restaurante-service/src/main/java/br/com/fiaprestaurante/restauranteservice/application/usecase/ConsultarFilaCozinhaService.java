package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.port.input.ConsultarFilaCozinhaUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementacao do caso de uso {@link ConsultarFilaCozinhaUseCase}.
 *
 * <p>Apenas leitura — encapsula as consultas em transacoes read-only para
 * permitir otimizacoes do Hibernate.
 *
 * @author Danilo Fernando
 */
@Service
public class ConsultarFilaCozinhaService implements ConsultarFilaCozinhaUseCase {

    private final PedidoCozinhaRepository repository;

    public ConsultarFilaCozinhaService(PedidoCozinhaRepository repository) {
        this.repository = repository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<PedidoCozinhaResponse> listar(StatusCozinha filtroStatus) {
        return repository.listar(filtroStatus).stream()
                .map(PedidoCozinhaResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Optional<PedidoCozinhaResponse> porId(UUID pedidoCozinhaId) {
        return repository.porId(pedidoCozinhaId).map(PedidoCozinhaResponse::from);
    }
}
