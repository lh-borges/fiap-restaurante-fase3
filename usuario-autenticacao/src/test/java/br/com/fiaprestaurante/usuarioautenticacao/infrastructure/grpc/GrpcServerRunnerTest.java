package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.grpc;

import br.com.fiaprestaurante.usuarioautenticacao.application.port.input.BuscarUsuarioAtualUseCase;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class GrpcServerRunnerTest {

    @Test
    void deveControlarCicloDeVidaDoServidorGrpc() {
        UsuarioGrpcService service = new UsuarioGrpcService(mock(BuscarUsuarioAtualUseCase.class));
        GrpcServerRunner runner = new GrpcServerRunner(service, 0);

        assertThat(runner.isRunning()).isFalse();

        try {
            runner.start();

            assertThat(runner.isRunning()).isTrue();
        } finally {
            runner.stop();
        }

        assertThat(runner.isRunning()).isFalse();
    }

    @Test
    void deveIgnorarStopQuandoServidorAindaNaoFoiIniciado() {
        UsuarioGrpcService service = new UsuarioGrpcService(mock(BuscarUsuarioAtualUseCase.class));
        GrpcServerRunner runner = new GrpcServerRunner(service, 0);

        runner.stop();

        assertThat(runner.isRunning()).isFalse();
    }

    @Test
    void deveFalharQuandoPortaGrpcEstiverOcupada() throws Exception {
        UsuarioGrpcService service = new UsuarioGrpcService(mock(BuscarUsuarioAtualUseCase.class));

        try (ServerSocket socket = new ServerSocket(0)) {
            GrpcServerRunner runner = new GrpcServerRunner(service, socket.getLocalPort());

            assertThatThrownBy(runner::start)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao iniciar o servidor gRPC na porta " + socket.getLocalPort());
            assertThat(runner.isRunning()).isFalse();
        }
    }
}
