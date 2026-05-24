package br.com.fiaprestaurante.usuarioautenticacao.application.usecase;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.AutenticarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.SessaoUsuario;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.PasswordHasher;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.TokenProvider;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.CredenciaisInvalidasException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link AutenticarUsuarioService} - cobre login
 * com sucesso, usuario inexistente e senha errada.
 *
 * @author Danilo Fernando
 */
class AutenticarUsuarioServiceTest {

    private UsuarioRepository repository;
    private PasswordHasher hasher;
    private TokenProvider tokenProvider;
    private AutenticarUsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        hasher = mock(PasswordHasher.class);
        tokenProvider = mock(TokenProvider.class);
        service = new AutenticarUsuarioService(repository, hasher, tokenProvider);
    }

    private Usuario usuario() {
        return new Usuario(1L, UUID.randomUUID(), "Ana", "ana@fiap.com",
                "hash", PerfilUsuario.USUARIO, OffsetDateTime.now());
    }

    @Test
    void deveAutenticarERetornarSessaoComToken() {
        Usuario u = usuario();
        when(repository.buscarPorEmail("ana@fiap.com")).thenReturn(Optional.of(u));
        when(hasher.matches("senha", "hash")).thenReturn(true);
        when(tokenProvider.gerarToken(u)).thenReturn("jwt.fake");
        when(tokenProvider.tempoDeExpiracaoEmSegundos()).thenReturn(3600L);

        SessaoUsuario sessao = service.executar(new AutenticarUsuarioCommand("Ana@FIAP.com", "senha"));

        assertThat(sessao.token()).isEqualTo("jwt.fake");
        assertThat(sessao.expiraEmSegundos()).isEqualTo(3600L);
        assertThat(sessao.usuario()).isEqualTo(u);
    }

    @Test
    void deveLancarCredenciaisInvalidasQuandoEmailInexistente() {
        when(repository.buscarPorEmail("ana@fiap.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(new AutenticarUsuarioCommand("ana@fiap.com", "senha")))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(tokenProvider, never()).gerarToken(any());
    }

    @Test
    void deveLancarCredenciaisInvalidasQuandoSenhaErrada() {
        when(repository.buscarPorEmail("ana@fiap.com")).thenReturn(Optional.of(usuario()));
        when(hasher.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.executar(new AutenticarUsuarioCommand("ana@fiap.com", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(tokenProvider, never()).gerarToken(any());
    }
}
