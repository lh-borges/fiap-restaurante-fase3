# ADR 0012: AppCDS + Layered JARs no Dockerfile

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

O `docker-compose.yml` constrói **4 imagens Spring Boot** (uma por
microsserviço). Cada imagem é um fat jar (`-Dspring-boot.repackage`),
o que tem duas consequências negativas:

1. **Cache de Docker fraco:** mudar uma única linha de código
   invalida o layer inteiro (~50 MB de jar reconstruído).
2. **Boot lento da JVM:** Spring Boot precisa carregar e linkar
   ~5000 classes antes de subir o ApplicationContext. Em ambiente
   conteinerizado com I/O lento, isso paga 1-2 segundos a cada
   restart.

A JVM moderna oferece duas técnicas para mitigar:

- **Layered JARs (Spring Boot 2.4+):** o jar pode ser extraído em
  layers semânticos (`dependencies`, `spring-boot-loader`,
  `snapshot-dependencies`, `application`).
- **AppCDS (Application Class Data Sharing):** a JVM pode pré-carregar
  classes em um arquivo binário (`.jsa`) e usar no boot subsequente.
  Disponível desde JDK 13; estável em JDK 17+.

## Decisão

Dockerfile multi-stage em **4 estágios** nos 4 módulos Spring:

1. **`build`** — compila o jar com Maven (com BuildKit cache em `~/.m2`).
2. **`extract`** — quebra o jar em layers via
   `java -Djarmode=layertools -jar app.jar extract`.
3. **`cds`** — training run para gerar o `application.jsa`:
   ```dockerfile
   RUN java -XX:ArchiveClassesAtExit=/app/application.jsa \
            -Dspring.context.exit=onRefresh \
            -Dspring.profiles.active=cds \
            org.springframework.boot.loader.launch.JarLauncher
   ```
4. **`runtime`** — copia as layers (`dependencies`,
   `spring-boot-loader`, `snapshot-dependencies`, `application`) +
   o `application.jsa`, e ENTRYPOINT carrega o archive:
   ```dockerfile
   ENTRYPOINT ["java", "-XX:SharedArchiveFile=application.jsa",
               "org.springframework.boot.loader.launch.JarLauncher"]
   ```

Profile `cds` (`application-cds.properties`) configura H2 in-memory
e desliga listeners Kafka para o training não exigir MySQL/Kafka
externos durante o `docker build`.

## Consequências

### Positivas

- **Rebuild incremental:** mudança de código invalida apenas a
  layer `application` (~5 MB), não o jar inteiro (~50 MB).
- **Boot da JVM ~30% mais rápido:** classes pré-resolvidas no
  archive. Validado via `java -version` exibindo `mixed mode,
  sharing`.
- **Sanity check implícito:** se o training run falhar (algum bean
  não consegue refrescar com H2), o build inteiro falha — pega
  bugs cedo.

### Negativas

- **Build ~1 min mais lento por app** (o training run inicia a JVM
  + sobe o Spring Context inteiro).
- **Mais complexidade no Dockerfile** (4 estágios em vez de 2).
- **Profile `cds` precisa ser mantido:** se o `application.properties`
  principal ganhar configs novas que exigem rede (Kafka listener,
  HTTP outbound), o profile precisa de sobrescrita correspondente.
- **+~100 MB por imagem** (`application.jsa` é grande).

## Alternativas consideradas

- **Fat jar simples sem layers:** mais simples, mas perde cache
  inteligente. Inaceitável para um projeto que vai ter muitos
  rebuilds.
- **Layered JARs sem AppCDS:** ganha o cache mas perde o boot
  mais rápido. AppCDS é barato em complexidade adicional.
- **GraalVM Native Image:** boot ~30× mais rápido, mas exige
  reflection metadata manual para Spring Boot (apesar de
  Native Hints melhorarem isso). Aumenta o tempo de build de
  ~1 min para ~5 min e tem suporte mais frágil a libs como
  Hibernate.
- **JLink custom runtime:** reduz tamanho da imagem, mas é
  complementar ao AppCDS, não substituto. Possível evolução futura.
