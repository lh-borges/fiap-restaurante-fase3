package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.security;

import br.com.fiaprestaurante.usuarioautenticacao.application.port.output.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
