package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.grpc;

import br.com.fiaprestaurante.grpc.BuscarUsuarioRequest;
import br.com.fiaprestaurante.grpc.UsuarioResponse;
import br.com.fiaprestaurante.grpc.VerificarPerfilRequest;
import br.com.fiaprestaurante.grpc.VerificarPerfilResponse;
import br.com.fiaprestaurante.usuarioautenticacao.TestFixtures;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.BuscarUsuarioAtualUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioGrpcServiceTest {

    @Mock
    private BuscarUsuarioAtualUseCase buscarUsuarioAtualUseCase;

    @Mock
    private StreamObserver<UsuarioResponse> usuarioObserver;

    @Mock
    private StreamObserver<VerificarPerfilResponse> perfilObserver;

    @Test
    void deveBuscarUsuarioPorPublicId() {
        UsuarioGrpcService service = new UsuarioGrpcService(buscarUsuarioAtualUseCase);
        when(buscarUsuarioAtualUseCase.executar(TestFixtures.PUBLIC_ID)).thenReturn(TestFixtures.usuario());
        ArgumentCaptor<UsuarioResponse> responseCaptor = ArgumentCaptor.forClass(UsuarioResponse.class);

        service.buscarUsuario(BuscarUsuarioRequest.newBuilder()
            .setPublicId(TestFixtures.PUBLIC_ID.toString())
            .build(), usuarioObserver);

        verify(usuarioObserver).onNext(responseCaptor.capture());
        verify(usuarioObserver).onCompleted();
        verify(usuarioObserver, never()).onError(org.mockito.ArgumentMatchers.any());
        assertThat(responseCaptor.getValue().getPublicId()).isEqualTo(TestFixtures.PUBLIC_ID.toString());
        assertThat(responseCaptor.getValue().getNome()).isEqualTo(TestFixtures.NOME);
        assertThat(responseCaptor.getValue().getEmail()).isEqualTo(TestFixtures.EMAIL);
        assertThat(responseCaptor.getValue().getPerfil()).isEqualTo("USUARIO");
    }

    @Test
    void deveRetornarNotFoundAoBuscarUsuarioInexistente() {
        UsuarioGrpcService service = new UsuarioGrpcService(buscarUsuarioAtualUseCase);
        when(buscarUsuarioAtualUseCase.executar(TestFixtures.PUBLIC_ID))
            .thenThrow(new UsuarioNaoEncontradoException(TestFixtures.PUBLIC_ID.toString()));
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);

        service.buscarUsuario(BuscarUsuarioRequest.newBuilder()
            .setPublicId(TestFixtures.PUBLIC_ID.toString())
            .build(), usuarioObserver);

        verify(usuarioObserver).onError(errorCaptor.capture());
        Status status = Status.fromThrowable(errorCaptor.getValue());
        assertThat(status.getCode()).isEqualTo(Status.Code.NOT_FOUND);
        assertThat(status.getDescription()).contains(TestFixtures.PUBLIC_ID.toString());
    }

    @Test
    void deveRetornarInvalidArgumentAoBuscarUsuarioComPublicIdInvalido() {
        UsuarioGrpcService service = new UsuarioGrpcService(buscarUsuarioAtualUseCase);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);

        service.buscarUsuario(BuscarUsuarioRequest.newBuilder()
            .setPublicId("id-invalido")
            .build(), usuarioObserver);

        verify(buscarUsuarioAtualUseCase, never()).executar(org.mockito.ArgumentMatchers.any());
        verify(usuarioObserver).onError(errorCaptor.capture());
        Status status = Status.fromThrowable(errorCaptor.getValue());
        assertThat(status.getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(status.getDescription()).isEqualTo("publicId invalido: id-invalido");
    }

    @Test
    void deveAutorizarPerfilQuandoUsuarioTemPerfilRequerido() {
        UsuarioGrpcService service = new UsuarioGrpcService(buscarUsuarioAtualUseCase);
        when(buscarUsuarioAtualUseCase.executar(TestFixtures.PUBLIC_ID)).thenReturn(TestFixtures.donoRestaurante());
        ArgumentCaptor<VerificarPerfilResponse> responseCaptor = ArgumentCaptor.forClass(VerificarPerfilResponse.class);

        service.verificarPerfil(VerificarPerfilRequest.newBuilder()
            .setPublicId(TestFixtures.PUBLIC_ID.toString())
            .setPerfilRequerido("DONO_RESTAURANTE")
            .build(), perfilObserver);

        verify(perfilObserver).onNext(responseCaptor.capture());
        verify(perfilObserver).onCompleted();
        assertThat(responseCaptor.getValue().getAutorizado()).isTrue();
    }

    @Test
    void deveNegarPerfilQuandoUsuarioNaoTemPerfilRequerido() {
        UsuarioGrpcService service = new UsuarioGrpcService(buscarUsuarioAtualUseCase);
        when(buscarUsuarioAtualUseCase.executar(TestFixtures.PUBLIC_ID)).thenReturn(TestFixtures.usuario());
        ArgumentCaptor<VerificarPerfilResponse> responseCaptor = ArgumentCaptor.forClass(VerificarPerfilResponse.class);

        service.verificarPerfil(VerificarPerfilRequest.newBuilder()
            .setPublicId(TestFixtures.PUBLIC_ID.toString())
            .setPerfilRequerido("DONO_RESTAURANTE")
            .build(), perfilObserver);

        verify(perfilObserver).onNext(responseCaptor.capture());
        verify(perfilObserver).onCompleted();
        assertThat(responseCaptor.getValue().getAutorizado()).isFalse();
    }

    @Test
    void deveNegarPerfilQuandoUsuarioNaoExiste() {
        UsuarioGrpcService service = new UsuarioGrpcService(buscarUsuarioAtualUseCase);
        when(buscarUsuarioAtualUseCase.executar(TestFixtures.PUBLIC_ID))
            .thenThrow(new UsuarioNaoEncontradoException(TestFixtures.PUBLIC_ID.toString()));
        ArgumentCaptor<VerificarPerfilResponse> responseCaptor = ArgumentCaptor.forClass(VerificarPerfilResponse.class);

        service.verificarPerfil(VerificarPerfilRequest.newBuilder()
            .setPublicId(TestFixtures.PUBLIC_ID.toString())
            .setPerfilRequerido("USUARIO")
            .build(), perfilObserver);

        verify(perfilObserver).onNext(responseCaptor.capture());
        verify(perfilObserver).onCompleted();
        assertThat(responseCaptor.getValue().getAutorizado()).isFalse();
    }

    @Test
    void deveRetornarInvalidArgumentAoVerificarPerfilComPublicIdInvalido() {
        UsuarioGrpcService service = new UsuarioGrpcService(buscarUsuarioAtualUseCase);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);

        service.verificarPerfil(VerificarPerfilRequest.newBuilder()
            .setPublicId("id-invalido")
            .setPerfilRequerido("USUARIO")
            .build(), perfilObserver);

        verify(buscarUsuarioAtualUseCase, never()).executar(org.mockito.ArgumentMatchers.any());
        verify(perfilObserver).onError(errorCaptor.capture());
        Status status = Status.fromThrowable(errorCaptor.getValue());
        assertThat(status.getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
        assertThat(status.getDescription()).isEqualTo("publicId invalido: id-invalido");
    }
}
