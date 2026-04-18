package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.grpc;

import br.com.fiaprestaurante.grpc.BuscarUsuarioRequest;
import br.com.fiaprestaurante.grpc.UsuarioResponse;
import br.com.fiaprestaurante.grpc.UsuarioServiceGrpc;
import br.com.fiaprestaurante.grpc.VerificarPerfilRequest;
import br.com.fiaprestaurante.grpc.VerificarPerfilResponse;
import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.BuscarUsuarioAtualUseCase;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UsuarioGrpcService extends UsuarioServiceGrpc.UsuarioServiceImplBase {

    private final BuscarUsuarioAtualUseCase buscarUsuarioAtualUseCase;

    public UsuarioGrpcService(BuscarUsuarioAtualUseCase buscarUsuarioAtualUseCase) {
        this.buscarUsuarioAtualUseCase = buscarUsuarioAtualUseCase;
    }

    @Override
    public void buscarUsuario(BuscarUsuarioRequest request, StreamObserver<UsuarioResponse> responseObserver) {
        try {
            UUID publicId = UUID.fromString(request.getPublicId());
            Usuario usuario = buscarUsuarioAtualUseCase.executar(publicId);
            UsuarioResponse response = UsuarioResponse.newBuilder()
                .setPublicId(usuario.publicId().toString())
                .setNome(usuario.nome())
                .setEmail(usuario.email())
                .setPerfil(usuario.perfil().name())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UsuarioNaoEncontradoException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("publicId invalido: " + request.getPublicId())
                .asRuntimeException());
        }
    }

    @Override
    public void verificarPerfil(VerificarPerfilRequest request, StreamObserver<VerificarPerfilResponse> responseObserver) {
        try {
            UUID publicId = UUID.fromString(request.getPublicId());
            try {
                Usuario usuario = buscarUsuarioAtualUseCase.executar(publicId);
                boolean autorizado = usuario.perfil().name().equals(request.getPerfilRequerido());
                responseObserver.onNext(VerificarPerfilResponse.newBuilder().setAutorizado(autorizado).build());
            } catch (UsuarioNaoEncontradoException e) {
                responseObserver.onNext(VerificarPerfilResponse.newBuilder().setAutorizado(false).build());
            }
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("publicId invalido: " + request.getPublicId())
                .asRuntimeException());
        }
    }
}
