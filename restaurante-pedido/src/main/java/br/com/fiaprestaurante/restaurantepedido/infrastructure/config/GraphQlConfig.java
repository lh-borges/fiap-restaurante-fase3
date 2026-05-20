package br.com.fiaprestaurante.restaurantepedido.infrastructure.config;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Configuração de runtime do GraphQL — registra scalars customizados.
 *
 * <p>Spring GraphQL não traz {@code BigDecimal} nativamente; declaramos
 * aqui para que o schema possa usar valores monetários sem perda de precisão
 * (cruciais em pagamento).
 *
 * @author Danilo Fernando
 */
@Configuration
public class GraphQlConfig {

    /**
     * @return wiring que adiciona o scalar {@code BigDecimal} ao schema
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(bigDecimalScalar());
    }

    /**
     * Define o scalar {@code BigDecimal} e o {@link Coercing} associado.
     *
     * <p>Serializa para string (preserva precisão); aceita strings, números
     * e {@code BigDecimal} na entrada.
     *
     * @return scalar GraphQL configurado
     */
    private static GraphQLScalarType bigDecimalScalar() {
        return GraphQLScalarType.newScalar()
                .name("BigDecimal")
                .description("Valor monetário com precisão arbitrária (BigDecimal)")
                .coercing(new Coercing<BigDecimal, String>() {

                    @Override
                    public String serialize(Object dataFetcherResult, GraphQLContext graphQLContext, Locale locale)
                            throws CoercingSerializeException {
                        if (dataFetcherResult instanceof BigDecimal bd) {
                            return bd.toPlainString();
                        }
                        if (dataFetcherResult instanceof Number n) {
                            return new BigDecimal(n.toString()).toPlainString();
                        }
                        if (dataFetcherResult instanceof String s) {
                            return new BigDecimal(s).toPlainString();
                        }
                        throw new CoercingSerializeException("Não é possível serializar como BigDecimal: " + dataFetcherResult);
                    }

                    @Override
                    public BigDecimal parseValue(Object input, GraphQLContext graphQLContext, Locale locale)
                            throws CoercingParseValueException {
                        try {
                            if (input instanceof BigDecimal bd) {
                                return bd;
                            }
                            if (input instanceof Number n) {
                                return new BigDecimal(n.toString());
                            }
                            return new BigDecimal(input.toString());
                        } catch (Exception e) {
                            throw new CoercingParseValueException("Valor BigDecimal inválido: " + input, e);
                        }
                    }

                    @Override
                    public BigDecimal parseLiteral(Value<?> input,
                                                   CoercedVariables variables,
                                                   GraphQLContext graphQLContext,
                                                   Locale locale) throws CoercingParseLiteralException {
                        try {
                            if (input instanceof StringValue sv) {
                                return new BigDecimal(sv.getValue());
                            }
                            return new BigDecimal(input.toString());
                        } catch (Exception e) {
                            throw new CoercingParseLiteralException("Literal BigDecimal inválido: " + input, e);
                        }
                    }
                })
                .build();
    }
}
