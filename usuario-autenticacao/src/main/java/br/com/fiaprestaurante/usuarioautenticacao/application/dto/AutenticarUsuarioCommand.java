package br.com.fiaprestaurante.usuarioautenticacao.application.dto;

public record AutenticarUsuarioCommand(
    String email,
    String senha
) {}
