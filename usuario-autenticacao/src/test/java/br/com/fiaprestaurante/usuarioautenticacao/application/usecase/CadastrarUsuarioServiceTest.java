package br.com.fiaprestaurante.usuarioautenticacao.application.usecase;

import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.PasswordHasher;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioJaExisteException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
 * Testes unitarios do {@link CadastrarUsuarioService} - cobre sucesso,
 * duplicata, normalizacao de email e perfil default.
 *
 * @author Danilo Fernando
 */
class CadastrarUsuarioServiceTest {

    private UsuarioRepository repository;
    private PasswordHasher hasher;
    private CadastrarUsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        hasher = mock(PasswordHasher.class);
        service = new CadastrarUsuarioService(repository, hasher);
    }

    @Test
    void deveCadastrarUsuarioNormalizarEmailEHashearSenha() {
        CadastrarUsuarioCommand cmd = new CadastrarUsuarioCommand(
                "  Ana Lima  ", "  Ana@FIAP.com  ", "senha123", PerfilUsuario.USUARIO);
        when(repository.buscarPorEmail("ana@fiap.com")).thenReturn(Optional.empty());
        when(hasher.hash("senha123")).thenReturn("$2a$10$hashedsenha");
        when(repository.salvar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario salvo = service.executar(cmd);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).salvar(captor.capture());
        Usuario persistido = captor.getValue();
        assertThat(persistido.id()).isNull();
        assertThat(persistido.publicId()).isNotNull();
        assertThat(persistido.nome()).isEqualTo("Ana Lima");
        assertThat(persistido.email()).isEqualTo("ana@fiap.com");
        assertThat(persistido.senhaHash()).isEqualTo("$2a$10$hashedsenha");
        assertThat(persistido.perfil()).isEqualTo(PerfilUsuario.USUARIO);
        assertThat(persistido.criadoEm()).isNotNull();
        assertThat(salvo).isEqualTo(persistido);
    }

    @Test
    void deveAplicarPerfilUsuarioPadraoQuandoNaoInformado() {
        CadastrarUsuarioCommand cmd = new CadastrarUsuarioCommand(
                "Joao", "joao@fiap.com", "senha", null);
        when(repository.buscarPorEmail("joao@fiap.com")).thenReturn(Optional.empty());
        when(hasher.hash(any())).thenReturn("hash");
        when(repository.salvar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.executar(cmd);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).salvar(captor.capture());
        assertThat(captor.getValue().perfil()).isEqualTo(PerfilUsuario.USUARIO);
    }

    @Test
    void deveLancarSeEmailJaExistir() {
        CadastrarUsuarioCommand cmd = new CadastrarUsuarioCommand(
                "Ana", "ana@fiap.com", "senha", PerfilUsuario.USUARIO);
        Usuario existente = new Usuario(1L, UUID.randomUUID(), "Ana",
                "ana@fiap.com", "hash", PerfilUsuario.USUARIO, OffsetDateTime.now());
        when(repository.buscarPorEmail("ana@fiap.com")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.executar(cmd))
                .isInstanceOf(UsuarioJaExisteException.class)
                .hasMessageContaining("ana@fiap.com");

        verify(repository, never()).salvar(any());
        verify(hasher, never()).hash(any());
    }
}
