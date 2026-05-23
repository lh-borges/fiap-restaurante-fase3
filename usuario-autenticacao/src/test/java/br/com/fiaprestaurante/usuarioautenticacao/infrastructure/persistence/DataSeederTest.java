package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.persistence;

import br.com.fiaprestaurante.usuarioautenticacao.TestFixtures;
import br.com.fiaprestaurante.usuarioautenticacao.application.dto.CadastrarUsuarioCommand;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.CadastrarUsuarioUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void deveCadastrarUsuariosSeedQuandoAindaNaoExistem() {
        DataSeeder seeder = new DataSeeder(cadastrarUsuarioUseCase, usuarioRepository);
        when(usuarioRepository.buscarPorEmail(anyString())).thenReturn(Optional.empty());
        ArgumentCaptor<CadastrarUsuarioCommand> captor = ArgumentCaptor.forClass(CadastrarUsuarioCommand.class);

        seeder.run(null);

        verify(cadastrarUsuarioUseCase, org.mockito.Mockito.times(2)).executar(captor.capture());
        assertThat(captor.getAllValues())
            .extracting(CadastrarUsuarioCommand::email)
            .containsExactly("dono@fiap.com", "usuario@fiap.com");
        assertThat(captor.getAllValues())
            .extracting(CadastrarUsuarioCommand::perfil)
            .containsExactly(PerfilUsuario.DONO_RESTAURANTE, PerfilUsuario.USUARIO);
    }

    @Test
    void deveIgnorarSeedQuandoUsuariosJaExistem() {
        DataSeeder seeder = new DataSeeder(cadastrarUsuarioUseCase, usuarioRepository);
        when(usuarioRepository.buscarPorEmail(anyString())).thenReturn(Optional.of(TestFixtures.usuario()));

        seeder.run(null);

        verifyNoInteractions(cadastrarUsuarioUseCase);
    }
}
