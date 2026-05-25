package br.com.fiaprestaurante.pagamento.application.dto;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representação imutável (DTO) de um {@link Pagamento} exposta para fora da
 * camada de aplicação — usada por controllers GraphQL, retornos de use cases
 * e consultas.
 *
 * <p>Não é serializada em Kafka; para isso existem os tipos específicos de
 * evento ({@link PagamentoAprovadoEvent}, {@link PagamentoPendenteEvent}).
 *
 * @param id          identidade única do pagamento
 * @param pedidoId    identificador do pedido associado
 * @param valor       valor monetário processado
 * @param status      estado atual ({@code PENDENTE}, {@code APROVADO} ou {@code RECUSADO})
 *                    serializado como string para facilitar consumo por clientes externos
 * @param tentativas  número de chamadas já feitas ao gateway externo
 * @param motivoFalha descrição da última falha, ou {@code null} se aprovado
 * @param createdAt   instante de criação
 * @param updatedAt   instante da última atualização
 *
 * @author Danilo Fernando
 */
public record PagamentoResponse(
        UUID id,
        UUID pedidoId,
        BigDecimal valor,
        String status,
        int tentativas,
        String motivoFalha,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Constrói um {@code PagamentoResponse} a partir de uma entidade de domínio.
     *
     * @param pagamento entidade de domínio fonte; não pode ser {@code null}
     * @return DTO contendo os mesmos dados em forma imutável
     */
    public static PagamentoResponse from(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getValor(),
                pagamento.getStatus().name(),
                pagamento.getTentativas(),
                pagamento.getMotivoFalha(),
                pagamento.getCreatedAt(),
                pagamento.getUpdatedAt()
        );
    }
}
