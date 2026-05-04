package br.com.fiaprestaurante.restaurantepedido.application.port.output;

import br.com.fiaprestaurante.grpc.UsuarioResponse;

/**
 * Porta de saída para comunicação com o serviço de usuários via gRPC.
 * Utilizada para buscar dados do cliente autenticado.
 */

public interface UsuarioGrpcPort {

    UsuarioResponse buscarUsuario(String publicId);
}
