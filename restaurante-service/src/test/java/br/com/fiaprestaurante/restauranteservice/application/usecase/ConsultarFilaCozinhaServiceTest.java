package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.entity.ItemCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link ConsultarFilaCozinhaService}.
 *
 * @author Danilo Fernando
 */
class ConsultarFilaCozinhaServiceTest {

    private PedidoCozinhaRepository repository;
    private ConsultarFilaCozinhaService service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoCozinhaRepository.class);
        service = new ConsultarFilaCozinhaService(repository);
    }

    private PedidoCozinha pedido() {
        return new PedidoCozinha(UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ItemCozinha(UUID.randomUUID(), "X", 1)));
    }

    @Test
    void listarSemFiltroDeveDelegarSemStatus() {
        when(repository.listar(null)).thenReturn(List.of(pedido(), pedido()));
        List<PedidoCozinhaResponse> result = service.listar(null);
        assertThat(result).hasSize(2);
        verify(repository).listar(null);
    }

    @Test
    void listarComFiltroDevePassarStatus() {
        when(repository.listar(StatusCozinha.EM_PREPARO)).thenReturn(List.of(pedido()));
        List<PedidoCozinhaResponse> result = service.listar(StatusCozinha.EM_PREPARO);
        assertThat(result).hasSize(1);
        verify(repository).listar(StatusCozinha.EM_PREPARO);
    }

    @Test
    void porIdEncontradoDeveRetornarResponse() {
        PedidoCozinha p = pedido();
        when(repository.porId(p.getId())).thenReturn(Optional.of(p));
        Optional<PedidoCozinhaResponse> result = service.porId(p.getId());
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(p.getId());
    }

    @Test
    void porIdAusenteDeveRetornarEmpty() {
        UUID id = UUID.randomUUID();
        when(repository.porId(id)).thenReturn(Optional.empty());
        assertThat(service.porId(id)).isEmpty();
    }
}
