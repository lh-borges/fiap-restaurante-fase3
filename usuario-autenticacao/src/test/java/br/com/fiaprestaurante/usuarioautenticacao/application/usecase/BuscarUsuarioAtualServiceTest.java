package br.com.fiaprestaurante.usuarioautenticacao.application.usecase;

import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link BuscarUsuarioAtualService} - busca por
 * publicId e tratamento de usuario nao encontrado.
 *
 * @author Danilo Fernando
 */
class BuscarUsuarioAtualServiceTest {

    private static final UUID PUBLIC_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    private UsuarioRepository repository;
    private BuscarUsuarioAtualService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        service = new BuscarUsuarioAtualService(repository);
    }

    @Test
    void deveRetornarUsuarioQuandoExistir() {
        Usuario u = new Usuario(1L, PUBLIC_ID, "Ana", "ana@fiap.com",
                "hash", PerfilUsuario.USUARIO, OffsetDateTime.now());
        when(repository.buscarPorPublicId(PUBLIC_ID)).thenReturn(Optional.of(u));

        Usuario resultado = service.executar(PUBLIC_ID);

        assertThat(resultado).isEqualTo(u);
    }

    @Test
    void deveLancarSeNaoEncontrado() {
        when(repository.buscarPorPublicId(PUBLIC_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(PUBLIC_ID))
                .isInstanceOf(UsuarioNaoEncontradoException.class)
                .hasMessageContaining(PUBLIC_ID.toString());
    }
}
