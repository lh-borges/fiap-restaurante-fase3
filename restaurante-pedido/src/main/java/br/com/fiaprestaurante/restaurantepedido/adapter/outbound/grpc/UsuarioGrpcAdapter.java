package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.grpc;

import br.com.fiaprestaurante.grpc.BuscarUsuarioRequest;
import br.com.fiaprestaurante.grpc.UsuarioResponse;
import br.com.fiaprestaurante.grpc.UsuarioServiceGrpc;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.UsuarioGrpcPort;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saída responsável pela comunicação com o serviço de usuários via gRPC.
 * Implementa a porta de saída UsuarioGrpcPort da camada de aplicação.
 * Utiliza o stub blocking do gRPC para buscar dados do cliente autenticado
 * no serviço usuario-autenticacao.
 */

@Component
public class UsuarioGrpcAdapter implements UsuarioGrpcPort {

    private final UsuarioServiceGrpc.UsuarioServiceBlockingStub usuarioServiceStub;

    public UsuarioGrpcAdapter(UsuarioServiceGrpc.UsuarioServiceBlockingStub usuarioServiceStub) {
        this.usuarioServiceStub = usuarioServiceStub;
    }

    /**
     * Busca os dados de um usuário pelo seu ID público via gRPC.
     * Monta a requisição gRPC e chama o serviço de autenticação,
     * retornando nome, email e perfil do usuário.
     */
    @Override
    public UsuarioResponse buscarUsuario(String publicId) {
        BuscarUsuarioRequest request = BuscarUsuarioRequest.newBuilder()
                .setPublicId(publicId)
                .build();
        return usuarioServiceStub.buscarUsuario(request);
    }
}
