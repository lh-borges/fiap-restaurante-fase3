package br.com.fiaprestaurante.usuarioautenticacao.application.usecase;

import br.com.fiaprestaurante.usuarioautenticacao.TestFixtures;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.AutenticarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.SessaoUsuario;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.PasswordHasher;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.TokenProvider;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.CredenciaisInvalidasException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioJaExisteException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    void deveCadastrarUsuarioNormalizandoCamposEPerfilPadrao() {
        CadastrarUsuarioService service = new CadastrarUsuarioService(usuarioRepository, passwordHasher);
        when(usuarioRepository.buscarPorEmail(TestFixtures.EMAIL)).thenReturn(Optional.empty());
        when(passwordHasher.hash(TestFixtures.SENHA)).thenReturn(TestFixtures.SENHA_HASH);
        when(usuarioRepository.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = service.executar(new CadastrarUsuarioCommand(
            "  Ana Maria  ",
            "  ANA@FIAP.COM  ",
            TestFixtures.SENHA,
            null
        ));

        assertThat(usuario.id()).isNull();
        assertThat(usuario.publicId()).isNotNull();
        assertThat(usuario.nome()).isEqualTo(TestFixtures.NOME);
        assertThat(usuario.email()).isEqualTo(TestFixtures.EMAIL);
        assertThat(usuario.senhaHash()).isEqualTo(TestFixtures.SENHA_HASH);
        assertThat(usuario.perfil()).isEqualTo(PerfilUsuario.USUARIO);
        assertThat(usuario.criadoEm()).isNotNull();
        verify(usuarioRepository).buscarPorEmail(TestFixtures.EMAIL);
        verify(usuarioRepository).salvar(any(Usuario.class));
    }

    @Test
    void deveCadastrarUsuarioComPerfilInformado() {
        CadastrarUsuarioService service = new CadastrarUsuarioService(usuarioRepository, passwordHasher);
        when(usuarioRepository.buscarPorEmail(TestFixtures.EMAIL)).thenReturn(Optional.empty());
        when(passwordHasher.hash(TestFixtures.SENHA)).thenReturn(TestFixtures.SENHA_HASH);
        when(usuarioRepository.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = service.executar(new CadastrarUsuarioCommand(
            TestFixtures.NOME,
            TestFixtures.EMAIL,
            TestFixtures.SENHA,
            PerfilUsuario.DONO_RESTAURANTE
        ));

        assertThat(usuario.perfil()).isEqualTo(PerfilUsuario.DONO_RESTAURANTE);
    }

    @Test
    void deveFalharAoCadastrarEmailJaExistente() {
        CadastrarUsuarioService service = new CadastrarUsuarioService(usuarioRepository, passwordHasher);
        when(usuarioRepository.buscarPorEmail(TestFixtures.EMAIL)).thenReturn(Optional.of(TestFixtures.usuario()));

        assertThatThrownBy(() -> service.executar(new CadastrarUsuarioCommand(
            TestFixtures.NOME,
            TestFixtures.EMAIL,
            TestFixtures.SENHA,
            PerfilUsuario.USUARIO
        )))
            .isInstanceOf(UsuarioJaExisteException.class)
            .hasMessageContaining(TestFixtures.EMAIL);
        verify(passwordHasher, never()).hash(any());
        verify(usuarioRepository, never()).salvar(any());
    }

    @Test
    void deveAutenticarUsuarioNormalizandoEmailEGerandoSessao() {
        AutenticarUsuarioService service = new AutenticarUsuarioService(usuarioRepository, passwordHasher, tokenProvider);
        when(usuarioRepository.buscarPorEmail(TestFixtures.EMAIL)).thenReturn(Optional.of(TestFixtures.usuario()));
        when(passwordHasher.matches(TestFixtures.SENHA, TestFixtures.SENHA_HASH)).thenReturn(true);
        when(tokenProvider.gerarToken(TestFixtures.usuario())).thenReturn(TestFixtures.TOKEN);
        when(tokenProvider.tempoDeExpiracaoEmSegundos()).thenReturn(3600L);

        SessaoUsuario sessao = service.executar(new AutenticarUsuarioCommand("  ANA@FIAP.COM  ", TestFixtures.SENHA));

        assertThat(sessao.token()).isEqualTo(TestFixtures.TOKEN);
        assertThat(sessao.expiraEmSegundos()).isEqualTo(3600L);
        assertThat(sessao.usuario()).isEqualTo(TestFixtures.usuario());
        verify(usuarioRepository).buscarPorEmail(TestFixtures.EMAIL);
    }

    @Test
    void deveFalharAoAutenticarUsuarioInexistente() {
        AutenticarUsuarioService service = new AutenticarUsuarioService(usuarioRepository, passwordHasher, tokenProvider);
        when(usuarioRepository.buscarPorEmail(TestFixtures.EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(new AutenticarUsuarioCommand(TestFixtures.EMAIL, TestFixtures.SENHA)))
            .isInstanceOf(CredenciaisInvalidasException.class)
            .hasMessage("Credenciais inválidas.");
        verify(passwordHasher, never()).matches(any(), any());
        verify(tokenProvider, never()).gerarToken(any());
    }

    @Test
    void deveFalharAoAutenticarSenhaInvalida() {
        AutenticarUsuarioService service = new AutenticarUsuarioService(usuarioRepository, passwordHasher, tokenProvider);
        when(usuarioRepository.buscarPorEmail(TestFixtures.EMAIL)).thenReturn(Optional.of(TestFixtures.usuario()));
        when(passwordHasher.matches("errada", TestFixtures.SENHA_HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.executar(new AutenticarUsuarioCommand(TestFixtures.EMAIL, "errada")))
            .isInstanceOf(CredenciaisInvalidasException.class);
        verify(tokenProvider, never()).gerarToken(any());
    }

    @Test
    void deveBuscarUsuarioAtualPorPublicId() {
        BuscarUsuarioAtualService service = new BuscarUsuarioAtualService(usuarioRepository);
        when(usuarioRepository.buscarPorPublicId(TestFixtures.PUBLIC_ID)).thenReturn(Optional.of(TestFixtures.usuario()));

        Usuario usuario = service.executar(TestFixtures.PUBLIC_ID);

        assertThat(usuario).isEqualTo(TestFixtures.usuario());
    }

    @Test
    void deveFalharAoBuscarUsuarioAtualInexistente() {
        BuscarUsuarioAtualService service = new BuscarUsuarioAtualService(usuarioRepository);
        UUID publicId = TestFixtures.PUBLIC_ID;
        when(usuarioRepository.buscarPorPublicId(publicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(publicId))
            .isInstanceOf(UsuarioNaoEncontradoException.class)
            .hasMessageContaining(publicId.toString());
    }

    @Test
    void deveEnviarUsuarioCriadoParaRepositorio() {
        CadastrarUsuarioService service = new CadastrarUsuarioService(usuarioRepository, passwordHasher);
        when(usuarioRepository.buscarPorEmail(TestFixtures.EMAIL)).thenReturn(Optional.empty());
        when(passwordHasher.hash(TestFixtures.SENHA)).thenReturn(TestFixtures.SENHA_HASH);
        when(usuarioRepository.salvar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        service.executar(new CadastrarUsuarioCommand(TestFixtures.NOME, TestFixtures.EMAIL, TestFixtures.SENHA, PerfilUsuario.USUARIO));

        verify(usuarioRepository).salvar(captor.capture());
        assertThat(captor.getValue().publicId()).isNotNull();
        assertThat(captor.getValue().senhaHash()).isEqualTo(TestFixtures.SENHA_HASH);
    }
}
