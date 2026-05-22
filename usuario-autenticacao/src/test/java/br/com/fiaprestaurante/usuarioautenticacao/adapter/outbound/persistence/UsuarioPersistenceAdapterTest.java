package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.persistence;

import br.com.fiaprestaurante.usuarioautenticacao.TestFixtures;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioPersistenceAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<UsuarioJpaEntity> typedQuery;

    @Test
    void deveMapearUsuarioEntreDominioEJpa() {
        Usuario usuario = TestFixtures.usuario();

        UsuarioJpaEntity entity = UsuarioMapper.toEntity(usuario);
        Usuario domain = UsuarioMapper.toDomain(entity);

        assertThat(entity.getId()).isEqualTo(TestFixtures.USUARIO_ID);
        assertThat(entity.getPublicId()).isEqualTo(TestFixtures.PUBLIC_ID);
        assertThat(entity.getNome()).isEqualTo(TestFixtures.NOME);
        assertThat(entity.getEmail()).isEqualTo(TestFixtures.EMAIL);
        assertThat(entity.getSenhaHash()).isEqualTo(TestFixtures.SENHA_HASH);
        assertThat(entity.getPerfil()).isEqualTo(PerfilUsuario.USUARIO);
        assertThat(entity.getCriadoEm()).isEqualTo(TestFixtures.CRIADO_EM);
        assertThat(domain).isEqualTo(usuario);
    }

    @Test
    void deveExporGettersESettersDaEntidadeJpa() {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setId(TestFixtures.USUARIO_ID);
        entity.setPublicId(TestFixtures.PUBLIC_ID);
        entity.setNome(TestFixtures.NOME);
        entity.setEmail(TestFixtures.EMAIL);
        entity.setSenhaHash(TestFixtures.SENHA_HASH);
        entity.setPerfil(PerfilUsuario.DONO_RESTAURANTE);
        entity.setCriadoEm(TestFixtures.CRIADO_EM);

        assertThat(entity.getId()).isEqualTo(TestFixtures.USUARIO_ID);
        assertThat(entity.getPublicId()).isEqualTo(TestFixtures.PUBLIC_ID);
        assertThat(entity.getNome()).isEqualTo(TestFixtures.NOME);
        assertThat(entity.getEmail()).isEqualTo(TestFixtures.EMAIL);
        assertThat(entity.getSenhaHash()).isEqualTo(TestFixtures.SENHA_HASH);
        assertThat(entity.getPerfil()).isEqualTo(PerfilUsuario.DONO_RESTAURANTE);
        assertThat(entity.getCriadoEm()).isEqualTo(TestFixtures.CRIADO_EM);
    }

    @Test
    void deveBuscarUsuarioPorEmailViaEntityManager() {
        UsuarioJpaRepositoryAdapter adapter = new UsuarioJpaRepositoryAdapter(entityManager);
        UsuarioJpaEntity entity = UsuarioMapper.toEntity(TestFixtures.usuario());
        when(entityManager.createQuery(anyString(), eq(UsuarioJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("email", TestFixtures.EMAIL)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(entity);

        Optional<Usuario> result = adapter.buscarPorEmail("ANA@FIAP.COM");

        assertThat(result).get()
            .extracting(Usuario::email)
            .isEqualTo(TestFixtures.EMAIL);
        verify(typedQuery).setParameter("email", TestFixtures.EMAIL);
    }

    @Test
    void deveRetornarVazioAoBuscarEmailInexistente() {
        UsuarioJpaRepositoryAdapter adapter = new UsuarioJpaRepositoryAdapter(entityManager);
        when(entityManager.createQuery(anyString(), eq(UsuarioJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("email", TestFixtures.EMAIL)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenThrow(new NoResultException());

        Optional<Usuario> result = adapter.buscarPorEmail(TestFixtures.EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    void deveBuscarUsuarioPorPublicIdViaEntityManager() {
        UsuarioJpaRepositoryAdapter adapter = new UsuarioJpaRepositoryAdapter(entityManager);
        UsuarioJpaEntity entity = UsuarioMapper.toEntity(TestFixtures.usuario());
        when(entityManager.createQuery(anyString(), eq(UsuarioJpaEntity.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("publicId", TestFixtures.PUBLIC_ID)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(entity);

        Optional<Usuario> result = adapter.buscarPorPublicId(TestFixtures.PUBLIC_ID);

        assertThat(result).get()
            .extracting(Usuario::publicId)
            .isEqualTo(TestFixtures.PUBLIC_ID);
    }

    @Test
    void deveSalvarUsuarioNovoComPersistEFlush() {
        UsuarioJpaRepositoryAdapter adapter = new UsuarioJpaRepositoryAdapter(entityManager);
        Usuario novo = new Usuario(
            null,
            TestFixtures.PUBLIC_ID,
            TestFixtures.NOME,
            TestFixtures.EMAIL,
            TestFixtures.SENHA_HASH,
            PerfilUsuario.USUARIO,
            TestFixtures.CRIADO_EM
        );

        Usuario salvo = adapter.salvar(novo);

        assertThat(salvo.id()).isNull();
        verify(entityManager).persist(org.mockito.ArgumentMatchers.any(UsuarioJpaEntity.class));
        verify(entityManager).flush();
    }

    @Test
    void deveAtualizarUsuarioExistenteComMerge() {
        UsuarioJpaRepositoryAdapter adapter = new UsuarioJpaRepositoryAdapter(entityManager);
        Usuario usuario = TestFixtures.usuario();
        when(entityManager.merge(org.mockito.ArgumentMatchers.any(UsuarioJpaEntity.class)))
            .thenReturn(UsuarioMapper.toEntity(usuario));

        Usuario salvo = adapter.salvar(usuario);

        assertThat(salvo).isEqualTo(usuario);
        verify(entityManager).merge(org.mockito.ArgumentMatchers.any(UsuarioJpaEntity.class));
    }
}
