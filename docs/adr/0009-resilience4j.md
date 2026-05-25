# ADR 0009: Resilience4j para resiliência na integração externa

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

O requisito 5.4 exige resiliência na chamada ao **gateway externo de
pagamento** (`procpag`), que é descrito pelos professores como um
serviço *eventualmente disponível* — ora autoriza pagamentos, ora
responde com falha ou timeout. A spec lista explicitamente:

- **Circuit Breaker** — abrir em sequência de falhas
- **Retry** — tentar novamente
- **Timeout** — limitar tempo de chamada
- **Fallback** — caminho alternativo quando tudo falha

Adicionalmente, o requisito 4.5 exige que o **fallback marque o pedido
como `PENDENTE_PAGAMENTO`** e publique o evento `pagamento.pendente`
no Kafka.

## Decisão

Adotar **Resilience4j 2.3.0** (`resilience4j-spring-boot3` +
`resilience4j-reactor`), aplicando todas as 4 técnicas como
**anotações** no método de integração do `pagamento-service`
(`ExternalPaymentClient`):

```properties
# pagamento-service/src/main/resources/application.properties (resumo)
resilience4j.circuitbreaker.instances.paymentService.sliding-window-size=5
resilience4j.circuitbreaker.instances.paymentService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.paymentService.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.paymentService.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.paymentService.automatic-transition-from-open-to-half-open-enabled=true

resilience4j.retry.instances.paymentService.max-attempts=3
resilience4j.retry.instances.paymentService.wait-duration=2s
resilience4j.retry.instances.paymentService.enable-exponential-backoff=true
resilience4j.retry.instances.paymentService.exponential-backoff-multiplier=2
resilience4j.retry.instances.paymentService.ignore-exceptions=io.github.resilience4j.circuitbreaker.CallNotPermittedException

resilience4j.timelimiter.instances.paymentService.timeout-duration=5s
resilience4j.timelimiter.instances.paymentService.cancel-running-future=true
```

O fallback publica `pagamento.pendente` no Kafka e marca o pagamento
como `PENDENTE`. Um worker `@Scheduled` no próprio módulo `pagamento-service`
reprocessa pendentes a cada 30 segundos.

## Consequências

### Positivas

- **Declarativo:** anotações `@CircuitBreaker`, `@Retry`,
  `@TimeLimiter`, `@Fallback` separam a regra de resiliência da
  lógica de negócio. Ler o método principal fica fácil.
- **Métricas via Actuator:** `/actuator/circuitbreakers` expõe estado
  (CLOSED/OPEN/HALF_OPEN), contagem de falhas, etc.
- **Configurável em runtime:** properties podem ser ajustadas sem
  recompilar (ex.: aumentar timeout sob alta carga).
- **Padrão moderno:** Resilience4j é o sucessor do Hystrix (este em
  maintenance mode desde 2018).

### Negativas

- **Mais uma dependência:** ~1 MB no jar final.
- **Curva de aprendizado:** combinar CB + Retry + TimeLimiter exige
  cuidado para evitar interações inesperadas (ex.: retry dentro do
  timeout pode estourar tempo total). A linha
  `retry.ignore-exceptions=CallNotPermittedException` é resultado
  desse aprendizado — não adianta retentar quando o CB já está aberto.

## Alternativas consideradas

- **Hystrix (Netflix):** em maintenance mode desde 2018, não
  recomendado para projetos novos.
- **Retry manual com `try/catch + Thread.sleep`:** funciona mas
  acopla regra de resiliência ao código de negócio. Resilience4j
  faz isso melhor, é a escolha óbvia.
- **Spring Retry (`@Retryable`):** cobre retry mas não Circuit
  Breaker. Solução parcial.
- **Service mesh (Istio, Linkerd) com retry/CB no proxy:** força a
  ter Kubernetes ou similar — overkill para o escopo do projeto.
