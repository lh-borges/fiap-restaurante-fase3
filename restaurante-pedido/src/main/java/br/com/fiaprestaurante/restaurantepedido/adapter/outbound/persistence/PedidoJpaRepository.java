package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório JPA para persistência de pedidos no banco de dados MySQL.
 * Estende JpaRepository fornecendo operações CRUD padrão
 * e define consultas customizadas necessárias para o domínio.
 */
public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, String> {
    List<PedidoEntity> findByClienteId(String clienteId);
}
