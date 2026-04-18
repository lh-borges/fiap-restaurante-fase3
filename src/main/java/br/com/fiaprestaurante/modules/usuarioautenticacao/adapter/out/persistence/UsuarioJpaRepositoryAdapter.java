package br.com.fiaprestaurante.modules.usuarioautenticacao.adapter.out.persistence;

import br.com.fiaprestaurante.modules.usuarioautenticacao.application.port.out.UsuarioRepository;
import br.com.fiaprestaurante.modules.usuarioautenticacao.domain.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UsuarioJpaRepositoryAdapter implements UsuarioRepository {

    private final EntityManager entityManager;

    public UsuarioJpaRepositoryAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        try {
            UsuarioJpaEntity entity = entityManager.createQuery(
                    "select u from UsuarioJpaEntity u where lower(u.email) = :email",
                    UsuarioJpaEntity.class
                )
                .setParameter("email", email.toLowerCase())
                .getSingleResult();
            return Optional.of(UsuarioMapper.toDomain(entity));
        } catch (NoResultException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Usuario> buscarPorPublicId(UUID publicId) {
        try {
            UsuarioJpaEntity entity = entityManager.createQuery(
                    "select u from UsuarioJpaEntity u where u.publicId = :publicId",
                    UsuarioJpaEntity.class
                )
                .setParameter("publicId", publicId)
                .getSingleResult();
            return Optional.of(UsuarioMapper.toDomain(entity));
        } catch (NoResultException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity entity = UsuarioMapper.toEntity(usuario);
        if (entity.getId() == null) {
            entityManager.persist(entity);
            entityManager.flush();
            return UsuarioMapper.toDomain(entity);
        }
        UsuarioJpaEntity mergedEntity = entityManager.merge(entity);
        return UsuarioMapper.toDomain(mergedEntity);
    }
}
