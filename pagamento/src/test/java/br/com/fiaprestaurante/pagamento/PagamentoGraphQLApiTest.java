package br.com.fiaprestaurante.pagamento;

import br.com.fiaprestaurante.pagamento.adapter.outbound.persistence.PagamentoJpaEntity;
import br.com.fiaprestaurante.pagamento.adapter.outbound.persistence.PagamentoJpaRepository;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"pedido.criado", "pagamento.aprovado", "pagamento.pendente"})
class PagamentoGraphQLApiTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PagamentoJpaRepository pagamentoJpaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pagamentoJpaRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = {"USUARIO"})
    void deveConsultarPagamentoPorPedidoComUsuarioAutenticado() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        PagamentoJpaEntity pagamento = salvarPagamento(pedidoId, StatusPagamento.APROVADO, null);

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"{pagamentoPorPedido(pedidoId:\\"%s\\"){id pedidoId valor status tentativas motivoFalha createdAt updatedAt}}"}
                                """.formatted(pedidoId).strip()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagamentoPorPedido.id").value(pagamento.getId().toString()))
                .andExpect(jsonPath("$.data.pagamentoPorPedido.pedidoId").value(pedidoId.toString()))
                .andExpect(jsonPath("$.data.pagamentoPorPedido.valor").value(42.9))
                .andExpect(jsonPath("$.data.pagamentoPorPedido.status").value("APROVADO"))
                .andExpect(jsonPath("$.data.pagamentoPorPedido.tentativas").value(1))
                .andExpect(jsonPath("$.data.pagamentoPorPedido.motivoFalha").doesNotExist())
                .andExpect(jsonPath("$.data.pagamentoPorPedido.createdAt").exists())
                .andExpect(jsonPath("$.data.pagamentoPorPedido.updatedAt").exists());
    }

    @Test
    @WithMockUser(authorities = {"DONO_RESTAURANTE"})
    void deveListarPagamentosPendentesComDonoRestaurante() throws Exception {
        UUID pedidoPendenteId = UUID.randomUUID();
        salvarPagamento(pedidoPendenteId, StatusPagamento.PENDENTE, "Gateway indisponivel");
        salvarPagamento(UUID.randomUUID(), StatusPagamento.APROVADO, null);

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{pagamentosPendentes{pedidoId status motivoFalha}}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagamentosPendentes.length()").value(1))
                .andExpect(jsonPath("$.data.pagamentosPendentes[0].pedidoId").value(pedidoPendenteId.toString()))
                .andExpect(jsonPath("$.data.pagamentosPendentes[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$.data.pagamentosPendentes[0].motivoFalha").value("Gateway indisponivel"));
    }

    @Test
    @WithMockUser(authorities = {"USUARIO"})
    void deveNegarListagemDePendentesParaUsuarioSemPermissao() throws Exception {
        String response = mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{pagamentosPendentes{pedidoId status}}\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("errors");
        assertThat(response).doesNotContain("pagamentosPendentes\":[");
    }

    @Test
    void deveNegarConsultaSemAutenticacao() throws Exception {
        String response = mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"{pagamentoPorPedido(pedidoId:\\"%s\\"){id}}"}
                                """.formatted(UUID.randomUUID()).strip()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("errors");
    }

    private PagamentoJpaEntity salvarPagamento(UUID pedidoId, StatusPagamento status, String motivoFalha) {
        Instant criadoEm = Instant.parse("2026-05-21T10:00:00Z").plusSeconds(pagamentoJpaRepository.count());
        PagamentoJpaEntity pagamento = new PagamentoJpaEntity(
                UUID.randomUUID(),
                pedidoId,
                new BigDecimal("42.90"),
                status,
                1,
                motivoFalha,
                criadoEm,
                criadoEm.plusSeconds(60)
        );
        return pagamentoJpaRepository.save(pagamento);
    }
}
