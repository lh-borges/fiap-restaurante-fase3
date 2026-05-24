package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.persistence;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios do {@link UsuarioMapper} - cobre conversao em ambos
 * os sentidos e o round-trip dominio↔JPA.
 *
 * @author Danilo Fernando
 */
class UsuarioMapperTest {

    @Test
    void toDomainDeveCopiarTodosOsCampos() {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        e.setId(42L);
        e.setPublicId(UUID.fromString("11111111-1111-4111-8111-111111111111"));
        e.setNome("Ana");
        e.setEmail("ana@fiap.com");
        e.setSenhaHash("$2a$10$hash");
        e.setPerfil(PerfilUsuario.DONO_RESTAURANTE);
        e.setCriadoEm(OffsetDateTime.parse("2026-01-01T10:00:00Z"));

        Usuario u = UsuarioMapper.toDomain(e);

        assertThat(u.id()).isEqualTo(42L);
        assertThat(u.publicId()).isEqualTo(e.getPublicId());
        assertThat(u.nome()).isEqualTo("Ana");
        assertThat(u.email()).isEqualTo("ana@fiap.com");
        assertThat(u.senhaHash()).isEqualTo("$2a$10$hash");
        assertThat(u.perfil()).isEqualTo(PerfilUsuario.DONO_RESTAURANTE);
        assertThat(u.criadoEm()).isEqualTo(e.getCriadoEm());
    }

    @Test
    void toEntityDeveCopiarTodosOsCampos() {
        Usuario u = new Usuario(7L, UUID.randomUUID(), "Joao", "joao@fiap.com",
                "hash", PerfilUsuario.USUARIO, OffsetDateTime.now());

        UsuarioJpaEntity e = UsuarioMapper.toEntity(u);

        assertThat(e.getId()).isEqualTo(u.id());
        assertThat(e.getPublicId()).isEqualTo(u.publicId());
        assertThat(e.getNome()).isEqualTo(u.nome());
        assertThat(e.getEmail()).isEqualTo(u.email());
        assertThat(e.getSenhaHash()).isEqualTo(u.senhaHash());
        assertThat(e.getPerfil()).isEqualTo(u.perfil());
        assertThat(e.getCriadoEm()).isEqualTo(u.criadoEm());
    }

    @Test
    void roundTripDevePreservarTodosOsCampos() {
        Usuario original = new Usuario(1L, UUID.randomUUID(), "Ana", "ana@fiap.com",
                "hash", PerfilUsuario.USUARIO, OffsetDateTime.now());

        Usuario reconvertido = UsuarioMapper.toDomain(UsuarioMapper.toEntity(original));

        assertThat(reconvertido).isEqualTo(original);
    }
}
