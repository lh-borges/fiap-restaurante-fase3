package br.com.fiaprestaurante.usuarioautenticacao.adapter.outbound.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios do {@link BcryptPasswordHasher} - cobre o ciclo
 * completo de hashing e verificacao.
 *
 * @author Danilo Fernando
 */
class BcryptPasswordHasherTest {

    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();

    @Test
    void hashDeveGerarStringComPrefixoBcrypt() {
        String hash = hasher.hash("minhasenha");

        assertThat(hash).isNotNull();
        assertThat(hash).startsWith("$2");
    }

    @Test
    void hashesDaMesmaSenhaDevemSerDiferentes() {
        String hash1 = hasher.hash("senha");
        String hash2 = hasher.hash("senha");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void matchesDeveRetornarTrueParaSenhaCorreta() {
        String hash = hasher.hash("usuario123");

        assertThat(hasher.matches("usuario123", hash)).isTrue();
    }

    @Test
    void matchesDeveRetornarFalseParaSenhaErrada() {
        String hash = hasher.hash("usuario123");

        assertThat(hasher.matches("senhaerrada", hash)).isFalse();
    }
}
