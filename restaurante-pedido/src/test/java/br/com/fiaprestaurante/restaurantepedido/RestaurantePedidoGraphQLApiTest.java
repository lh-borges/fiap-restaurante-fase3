package br.com.fiaprestaurante.restaurantepedido;

import br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence.ItemPedidoJpaEntity;
import br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence.PedidoJpaEntity;
import br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence.PedidoJpaRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(properties = {
        "spring.kafka.consumer.group-id=restaurante-pedido-test",
        "pedido.topics.pedido-criado=pedido.criado",
        "pedido.topics.pagamento-aprovado=pagamento.aprovado",
        "pedido.topics.pagamento-pendente=pagamento.pendente",
        "pedido.topics.pedido-pronto-para-cozinha=pedido.pronto-para-cozinha",
        "pedido.topics.pedido-em-preparo=pedido.em-preparo",
        "pedido.topics.pedido-pronto=pedido.pronto"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "pedido.criado", "pagamento.aprovado", "pagamento.pendente",
                "pedido.pronto-para-cozinha", "pedido.em-preparo", "pedido.pronto"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class RestaurantePedidoGraphQLApiTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PedidoJpaRepository pedidoJpaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pedidoJpaRepository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    @WithMockUser(authorities = {"USUARIO"})
    void deveRetornarStatusComUsuarioAutenticado() throws Exception {
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"{statusModuloRestaurantePedido{nome implementado descricao}}\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.statusModuloRestaurantePedido.nome").value("restaurante-pedido"))
            .andExpect(jsonPath("$.data.statusModuloRestaurantePedido.implementado").value(true));
    }

    @Test
    void deveConsultarPedidoPorIdComUsuarioAutenticado() throws Exception {
        UUID clienteId = UUID.randomUUID();
        PedidoJpaEntity pedido = salvarPedido(clienteId, StatusPedido.PAGO, UUID.randomUUID(), null);

        mockMvc.perform(post("/graphql")
                        .with(jwtUsuario(clienteId, "USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"{pedidoPorId(pedidoId:\\"%s\\"){id clienteId restauranteId valorTotal status pagamentoId motivoPendencia createdAt updatedAt itens{produtoId nome quantidade preco subtotal}}}"}
                                """.formatted(pedido.getId()).strip()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pedidoPorId.id").value(pedido.getId().toString()))
                .andExpect(jsonPath("$.data.pedidoPorId.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.data.pedidoPorId.restauranteId").value(pedido.getRestauranteId().toString()))
                .andExpect(jsonPath("$.data.pedidoPorId.valorTotal").value(42.9))
                .andExpect(jsonPath("$.data.pedidoPorId.status").value("PAGO"))
                .andExpect(jsonPath("$.data.pedidoPorId.pagamentoId").value(pedido.getPagamentoId().toString()))
                .andExpect(jsonPath("$.data.pedidoPorId.motivoPendencia").doesNotExist())
                .andExpect(jsonPath("$.data.pedidoPorId.createdAt").exists())
                .andExpect(jsonPath("$.data.pedidoPorId.updatedAt").exists())
                .andExpect(jsonPath("$.data.pedidoPorId.itens.length()").value(1))
                .andExpect(jsonPath("$.data.pedidoPorId.itens[0].nome").value("Pizza"))
                .andExpect(jsonPath("$.data.pedidoPorId.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.data.pedidoPorId.itens[0].preco").value(21.45))
                .andExpect(jsonPath("$.data.pedidoPorId.itens[0].subtotal").value(42.9));
    }

    @Test
    void deveListarMeusPedidosComUsuarioAutenticado() throws Exception {
        UUID clienteId = UUID.randomUUID();
        PedidoJpaEntity pedidoDoCliente = salvarPedido(clienteId, StatusPedido.PENDENTE_PAGAMENTO, null, "Gateway indisponivel");
        salvarPedido(UUID.randomUUID(), StatusPedido.CRIADO, null, null);

        mockMvc.perform(post("/graphql")
                        .with(jwtUsuario(clienteId, "USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{meusPedidos{id clienteId status motivoPendencia}}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meusPedidos.length()").value(1))
                .andExpect(jsonPath("$.data.meusPedidos[0].id").value(pedidoDoCliente.getId().toString()))
                .andExpect(jsonPath("$.data.meusPedidos[0].clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.data.meusPedidos[0].status").value("PENDENTE_PAGAMENTO"))
                .andExpect(jsonPath("$.data.meusPedidos[0].motivoPendencia").value("Gateway indisponivel"));
    }

    @Test
    void deveCriarPedidoComClienteExtraidoDoJwt() throws Exception {
        UUID clienteId = UUID.randomUUID();
        UUID restauranteId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();

        mockMvc.perform(post("/graphql")
                        .with(jwtUsuario(clienteId, "USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"mutation CriarPedido($input: CriarPedidoInput!) { criarPedido(input: $input){id clienteId restauranteId valorTotal status itens{produtoId nome quantidade preco subtotal}} }","variables":{"input":{"restauranteId":"%s","itens":[{"produtoId":"%s","nome":"Pizza","quantidade":2,"preco":"21.45"}]}}}
                                """.formatted(restauranteId, produtoId).strip()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.criarPedido.id").exists())
                .andExpect(jsonPath("$.data.criarPedido.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.data.criarPedido.restauranteId").value(restauranteId.toString()))
                .andExpect(jsonPath("$.data.criarPedido.valorTotal").value(42.9))
                .andExpect(jsonPath("$.data.criarPedido.status").value("CRIADO"))
                .andExpect(jsonPath("$.data.criarPedido.itens[0].produtoId").value(produtoId.toString()))
                .andExpect(jsonPath("$.data.criarPedido.itens[0].nome").value("Pizza"))
                .andExpect(jsonPath("$.data.criarPedido.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.data.criarPedido.itens[0].subtotal").value(42.9));

        assertThat(pedidoJpaRepository.findAll()).singleElement()
                .satisfies(pedido -> {
                    assertThat(pedido.getClienteId()).isEqualTo(clienteId);
                    assertThat(pedido.getRestauranteId()).isEqualTo(restauranteId);
                    assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
                    assertThat(pedido.getValorTotal()).isEqualByComparingTo("42.90");
                });
    }

    @Test
    void deveConfirmarPedidoComUsuarioAutenticado() throws Exception {
        UUID clienteId = UUID.randomUUID();
        PedidoJpaEntity pedido = salvarPedido(clienteId, StatusPedido.CRIADO, null, null);

        mockMvc.perform(post("/graphql")
                        .with(jwtUsuario(clienteId, "USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"mutation { confirmarPedido(pedidoId:\\"%s\\"){id clienteId status valorTotal} }"}
                                """.formatted(pedido.getId()).strip()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.confirmarPedido.id").value(pedido.getId().toString()))
                .andExpect(jsonPath("$.data.confirmarPedido.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.data.confirmarPedido.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.data.confirmarPedido.valorTotal").value(42.9));

        assertThat(pedidoJpaRepository.findById(pedido.getId())).get()
                .extracting(PedidoJpaEntity::getStatus)
                .isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    void deveNegarConfirmacaoDePedidoDeOutroCliente() throws Exception {
        PedidoJpaEntity pedido = salvarPedido(UUID.randomUUID(), StatusPedido.CRIADO, null, null);

        String response = mockMvc.perform(post("/graphql")
                        .with(jwtUsuario(UUID.randomUUID(), "USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"mutation { confirmarPedido(pedidoId:\\"%s\\"){id status} }"}
                                """.formatted(pedido.getId()).strip()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).contains("errors");
        assertThat(response).contains("pedido não pertence ao cliente autenticado");
        assertThat(pedidoJpaRepository.findById(pedido.getId())).get()
                .extracting(PedidoJpaEntity::getStatus)
                .isEqualTo(StatusPedido.CRIADO);
    }

    @Test
    void deveNegarAcessoSemAutenticacao() throws Exception {
        String result = mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"{statusModuloRestaurantePedido{nome}}\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errors[0].extensions.classification").value("FORBIDDEN"))
            .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("errors");
    }

    @Test
    @WithMockUser(authorities = {"USUARIO"})
    void deveTratarPedidoIdInvalidoComoBadRequestGraphQL() throws Exception {
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"{pedidoPorId(pedidoId:\\"id-invalido\\"){id}}"}
                                """.strip()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.pedidoPorId").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").value("Argumento invalido: Invalid UUID string: id-invalido"))
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("BAD_REQUEST"));
    }

    @Test
    void deveTratarPedidoNaoEncontradoComoNotFoundHttp() throws Exception {
        mockMvc.perform(post("/graphql")
                        .with(jwtUsuario(UUID.randomUUID(), "USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"{pedidoPorId(pedidoId:\\"%s\\"){id}}"}
                                """.formatted(UUID.randomUUID()).strip()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("NOT_FOUND"));
    }

    @Test
    void devePublicarContratoOpenApiDoEndpointGraphqlProtegido() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("FIAP Restaurante - Pedidos"))
                .andExpect(jsonPath("$.paths['/graphql'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/graphql'].post.requestBody.content['application/json'].examples.CriarPedido").exists())
                .andExpect(jsonPath("$.paths['/graphql'].post.responses['200'].content['application/json'].examples.Sucesso").exists())
                .andExpect(jsonPath("$.paths['/graphql'].post.responses['400'].content['application/json'].examples.ErroBadRequest").exists())
                .andExpect(jsonPath("$.paths['/graphql'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/graphql'].post.responses['403'].content['application/json'].examples.ErroForbidden").exists())
                .andExpect(jsonPath("$.paths['/graphql'].post.responses['404'].content['application/json'].examples.ErroNotFound").exists())
                .andExpect(jsonPath("$.paths['/graphql'].post.responses['500'].content['application/json'].examples.ErroInterno").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
    }

    private PedidoJpaEntity salvarPedido(UUID clienteId,
                                         StatusPedido status,
                                         UUID pagamentoId,
                                         String motivoPendencia) {
        Instant criadoEm = Instant.parse("2026-05-21T10:00:00Z").plusSeconds(pedidoJpaRepository.count());
        PedidoJpaEntity pedido = new PedidoJpaEntity(
                UUID.randomUUID(),
                clienteId,
                UUID.randomUUID(),
                new BigDecimal("42.90"),
                status,
                pagamentoId,
                motivoPendencia,
                criadoEm,
                criadoEm.plusSeconds(60),
                List.of(new ItemPedidoJpaEntity(UUID.randomUUID(), "Pizza", 2, new BigDecimal("21.45")))
        );
        return pedidoJpaRepository.save(pedido);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtUsuario(UUID clienteId,
                                                                                                  String authority) {
        return jwt()
                .jwt(token -> token.subject(clienteId.toString()))
                .authorities(new SimpleGrantedAuthority(authority));
    }
}
