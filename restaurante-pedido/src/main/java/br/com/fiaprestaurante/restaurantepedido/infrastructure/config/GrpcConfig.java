package br.com.fiaprestaurante.restaurantepedido.infrastructure.config;

import br.com.fiaprestaurante.grpc.UsuarioServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuração do cliente gRPC para comunicação com o serviço de usuários.
 * Cria e configura o canal gRPC e o stub bloqueante utilizados
 * pelo UsuarioGrpcAdapter para buscar dados do cliente autenticado.
 * O host e porta são configuráveis via application.properties.
 */
@Configuration
public class GrpcConfig {

    @Value("${grpc.usuario.host:localhost}")
    private String host;

    @Value("${grpc.usuario.port:9090}")
    private int port;

    @Bean
    public ManagedChannel usuarioChannel() {
        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public UsuarioServiceGrpc.UsuarioServiceBlockingStub usuarioServiceBlockingStub(ManagedChannel usuarioChannel) {
        return UsuarioServiceGrpc.newBlockingStub(usuarioChannel);
    }
}
