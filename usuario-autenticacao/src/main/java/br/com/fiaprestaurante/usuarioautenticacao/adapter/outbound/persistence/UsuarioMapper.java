package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.persistence;

import br.com.fiaprestaurante.usuarioautenticacao.domain.entity.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {}

    public static Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(
            entity.getId(),
            entity.getPublicId(),
            entity.getNome(),
            entity.getEmail(),
            entity.getSenhaHash(),
            entity.getPerfil(),
            entity.getCriadoEm()
        );
    }

    public static UsuarioJpaEntity toEntity(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setId(usuario.id());
        entity.setPublicId(usuario.publicId());
        entity.setNome(usuario.nome());
        entity.setEmail(usuario.email());
        entity.setSenhaHash(usuario.senhaHash());
        entity.setPerfil(usuario.perfil());
        entity.setCriadoEm(usuario.criadoEm());
        return entity;
    }
}
