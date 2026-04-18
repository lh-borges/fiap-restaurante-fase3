package br.com.fiaprestaurante.usuarioautenticacao.application.port.output;

public interface PasswordHasher {

    String hash(String plainTextPassword);

    boolean matches(String plainTextPassword, String hashedPassword);
}
