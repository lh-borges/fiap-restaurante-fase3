package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence.entity.ItemPedidoEntity;
import br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence.entity.PedidoEntity;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepositoryPort;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de saída responsável pela persistência de pedidos no banco de dados MySQL.
 * Implementa a porta de saída PedidoRepositoryPort da camada de aplicação,
 * fazendo a conversão entre as entidades de domínio e as entidades JPA.
 */

@Component
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {

    private final PedidoJpaRepository jpaRepository;

    public PedidoRepositoryAdapter(PedidoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }


    /**
     * Salva ou atualiza um pedido no banco de dados.
     * Converte a entidade de domínio para entidade JPA antes de persistir.
     */
    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoEntity entity = toEntity(pedido);
        PedidoEntity salvo = jpaRepository.saveAndFlush(entity);
        return toDomain(salvo);
    }


    /**
     * Busca um pedido pelo seu ID.
     */
    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return jpaRepository.findById(id.toString()).map(this::toDomain);
    }


    /**
     * Busca todos os pedidos de um cliente específico.
     */
    @Override
    public List<Pedido> buscarPorClienteId(String clienteId) {
        return jpaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDomain)
                .toList();
    }


    /**
     * Lista todos os pedidos do sistema.
     */
    @Override
    public List<Pedido> listarTodos() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Converte uma entidade de domínio Pedido para entidade JPA PedidoEntity.
     * Os UUIDs são convertidos para String para compatibilidade com o MySQL.
     */
    private PedidoEntity toEntity(Pedido pedido) {
        PedidoEntity entity = new PedidoEntity();

        entity.setClienteId(pedido.getClienteId());
        entity.setClienteNome(pedido.getClienteNome());
        entity.setClienteEmail(pedido.getClienteEmail());
        entity.setRestauranteId(pedido.getRestauranteId().toString());
        entity.setValorTotal(pedido.getValorTotal());
        entity.setStatus(pedido.getStatus());
        entity.setCriadoEm(pedido.getCriadoEm());

        List<ItemPedidoEntity> itens = pedido.getItens().stream().map(i -> {
            ItemPedidoEntity item = new ItemPedidoEntity();
            item.setPedido(entity);
            item.setProdutoId(i.getProdutoId().toString());
            item.setNome(i.getNome());
            item.setQuantidade(i.getQuantidade());
            item.setPreco(i.getPreco());
            return item;
        }).toList();

        entity.setItens(itens);
        return entity;
    }

    /**
     * Converte uma entidade JPA PedidoEntity para entidade de domínio Pedido.
     * As Strings de UUID são convertidas de volta para UUID.
     */
    private Pedido toDomain(PedidoEntity entity) {
        List<ItemPedido> itens = entity.getItens().stream()
                .map(i -> new ItemPedido(
                        UUID.fromString(i.getProdutoId()),
                        i.getNome(),
                        i.getQuantidade(),
                        i.getPreco()))
                .toList();

        Pedido pedido = new Pedido(
                entity.getClienteId(),
                entity.getClienteNome(),
                entity.getClienteEmail(),
                UUID.fromString(entity.getRestauranteId()),
                itens
        );
        pedido.setId(UUID.fromString(entity.getId()));
        return pedido;
    }


}
