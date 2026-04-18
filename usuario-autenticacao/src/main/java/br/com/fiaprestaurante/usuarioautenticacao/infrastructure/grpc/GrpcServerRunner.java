package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServerRunner implements SmartLifecycle {

    private final UsuarioGrpcService usuarioGrpcService;
    private final int port;
    private Server server;
    private volatile boolean running = false;

    public GrpcServerRunner(
        UsuarioGrpcService usuarioGrpcService,
        @Value("${grpc.server.port:9000}") int port
    ) {
        this.usuarioGrpcService = usuarioGrpcService;
        this.port = port;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(port)
                .addService(usuarioGrpcService)
                .build()
                .start();
            running = true;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao iniciar o servidor gRPC na porta " + port, e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
