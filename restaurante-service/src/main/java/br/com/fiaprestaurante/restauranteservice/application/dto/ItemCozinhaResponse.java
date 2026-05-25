package br.com.fiaprestaurante.restauranteservice.application.dto;

import java.util.UUID;

/**
 * Resposta de leitura para um item da cozinha. Espelha o value object
 * {@link br.com.fiaprestaurante.restauranteservice.domain.entity.ItemCozinha}.
 *
 * @author Danilo Fernando
 */
public record ItemCozinhaResponse(UUID produtoId, String nome, int quantidade) {
}
