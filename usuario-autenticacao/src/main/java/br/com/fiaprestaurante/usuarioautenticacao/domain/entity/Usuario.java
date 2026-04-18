package br.com.fiaprestaurante.usuarioautenticacao.domain.entity;

import br.com.fiaprestaurante.usuarioautenticacao.domain.valueobject.PerfilUsuario;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Usuario(
    Long id,
    UUID publicId,
    String nome,
    String email,
    String senhaHash,
    PerfilUsuario perfil,
    OffsetDateTime criadoEm
) {}
