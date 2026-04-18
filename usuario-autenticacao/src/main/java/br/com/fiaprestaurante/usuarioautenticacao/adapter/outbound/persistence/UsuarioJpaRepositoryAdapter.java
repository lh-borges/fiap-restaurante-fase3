package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.persistence;

import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.UsuarioRepository;
import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioJpaRepositoryAdapter implements UsuarioRepository {

    private final EntityManager entityManager;

    public UsuarioJpaRepositoryAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        try {
            UsuarioJpaEntity entity = entityManager.createQuery(
                    "select u from UsuarioJpaEntity u where lower(u.email) = :email",
                    UsuarioJpaEntity.class
                )
                .setParameter("email", email.toLowerCase())
                .getSingleResult();
            return Optional.of(UsuarioMapper.toDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorPublicId(UUID publicId) {
        try {
            UsuarioJpaEntity entity = entityManager.createQuery(
                    "select u from UsuarioJpaEntity u where u.publicId = :publicId",
                    UsuarioJpaEntity.class
                )
                .setParameter("publicId", publicId)
                .getSingleResult();
            return Optional.of(UsuarioMapper.toDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity entity = UsuarioMapper.toEntity(usuario);
        if (entity.getId() == null) {
            entityManager.persist(entity);
            entityManager.flush();
            return UsuarioMapper.toDomain(entity);
        }
        UsuarioJpaEntity merged = entityManager.merge(entity);
        return UsuarioMapper.toDomain(merged);
    }
}
