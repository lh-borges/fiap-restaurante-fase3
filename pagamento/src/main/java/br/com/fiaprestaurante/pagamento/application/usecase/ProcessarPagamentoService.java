package br.com.fiaprestaurante.pagamento.application.usecase;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoAprovadoEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoPendenteEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;
import br.com.fiaprestaurante.pagamento.application.port.input.ProcessarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import br.com.fiaprestaurante.pagamento.application.port.output.PaymentEventPublisher;
import br.com.fiaprestaurante.pagamento.application.port.output.PaymentGateway;
import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Implementação de {@link ProcessarPagamentoUseCase} — orquestra o fluxo
 * principal do microsserviço.
 *
 * <p>Algoritmo (transação única):
 * <ol>
 *   <li>Consulta se já existe pagamento para o {@code pedidoId}; se já estiver
 *       APROVADO, retorna o existente (idempotência);</li>
 *   <li>Cria ou reaproveita a entidade {@code Pagamento} e incrementa o
 *       contador de tentativas;</li>
 *   <li>Invoca o {@link PaymentGateway} (procpag) — qualquer exceção é
 *       capturada em {@link #tentarGateway(ProcessarPagamentoCommand)} e
 *       considerada como falha do gateway (não propaga);</li>
 *   <li>Em sucesso: marca APROVADO, persiste e publica {@code pagamento.aprovado};</li>
 *   <li>Em falha: marca PENDENTE com motivo, persiste e publica {@code pagamento.pendente}.</li>
 * </ol>
 *
 * <p>Toda a operação roda dentro de uma {@link Transactional}, garantindo
 * que persistência e publicação Kafka sejam consistentes do ponto de vista
 * do banco (se a persistência falhar, nada é publicado).
 *
 * @author Danilo Fernando
 */
@Service
public class ProcessarPagamentoService implements ProcessarPagamentoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessarPagamentoService.class);

    private final PagamentoRepository pagamentoRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Construtor com injeção das três portas de saída necessárias.
     *
     * @param pagamentoRepository porta de persistência
     * @param paymentGateway      porta para o gateway externo
     * @param eventPublisher      porta para publicação de eventos
     */
    public ProcessarPagamentoService(PagamentoRepository pagamentoRepository,
                                     PaymentGateway paymentGateway,
                                     PaymentEventPublisher eventPublisher) {
        this.pagamentoRepository = pagamentoRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public PagamentoResponse executar(ProcessarPagamentoCommand command) {
        Optional<Pagamento> existente = pagamentoRepository.buscarPorPedidoId(command.pedidoId());
        if (existente.isPresent() && existente.get().estaAprovado()) {
            log.info("Pagamento já aprovado para pedidoId={} — idempotente, retornando existente", command.pedidoId());
            return PagamentoResponse.from(existente.get());
        }

        Pagamento pagamento = existente.orElseGet(() -> new Pagamento(command.pedidoId(), command.valorTotal()));
        pagamento.incrementarTentativas();

        boolean aprovado = tentarGateway(command);

        if (aprovado) {
            pagamento.aprovar();
            Pagamento salvo = pagamentoRepository.salvar(pagamento);
            eventPublisher.publicarPagamentoAprovado(
                    new PagamentoAprovadoEvent(salvo.getPedidoId(), salvo.getId(), Instant.now())
            );
            log.info("Pagamento APROVADO para pedidoId={}, pagamentoId={}", salvo.getPedidoId(), salvo.getId());
            return PagamentoResponse.from(salvo);
        }

        pagamento.marcarComoPendente("Falha ao processar pagamento no serviço externo");
        Pagamento salvo = pagamentoRepository.salvar(pagamento);
        eventPublisher.publicarPagamentoPendente(
                new PagamentoPendenteEvent(salvo.getPedidoId(), salvo.getId(), salvo.getMotivoFalha(), Instant.now())
        );
        log.warn("Pagamento PENDENTE para pedidoId={}, pagamentoId={}, motivo={}",
                salvo.getPedidoId(), salvo.getId(), salvo.getMotivoFalha());
        return PagamentoResponse.from(salvo);
    }

    /**
     * Invoca o gateway externo capturando qualquer exceção.
     *
     * <p>Esta é a "fronteira de fallback" do use case: as anotações
     * {@code @CircuitBreaker} e {@code @Retry} no {@link PaymentGateway}
     * podem rethrow, mas aqui isolamos isso retornando simplesmente
     * {@code false} — o que aciona a transição para {@code PENDENTE} no
     * caller.
     *
     * @param command comando contendo {@code pedidoId} e valor
     * @return {@code true} se o gateway autorizou; {@code false} se houve
     *         qualquer falha (rede, CB aberto, retorno HTTP de erro)
     */
    private boolean tentarGateway(ProcessarPagamentoCommand command) {
        try {
            return paymentGateway.processar(command.pedidoId(), command.valorTotal());
        } catch (Exception e) {
            log.warn("Gateway de pagamento falhou para pedidoId={}: {}", command.pedidoId(), e.getMessage());
            return false;
        }
    }
}
