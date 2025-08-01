# API de Votação

API para criação e gerenciamento de pautas, sessões de votação e votos.

Desenvolvida com foco na simplicidade, clareza na separação dos domínios.  
A imagem da API foi construída utilizando compilação nativa para garantir desempenho otimizado e tempos de inicialização reduzidos.

## Tecnologias

* **Java 23**
* **Spring Boot 3**
* **Spring Data JPA**
* **Spring Actuator**
* **PostgreSQL**
* **JUnit**
* **Mockito**
* **Gradle**
* **Docker**
* **k6**

## Como executar

### Com Docker

A maneira mais simples de executar a aplicação é com o Docker.

1.  **Iniciar os containers:**

    ```bash
    docker compose up
    ```

A API estará disponível em `http://localhost:8080`.

### Com Gradle

É possível executar a aplicação diretamente com o Gradle.

1.  **Iniciar o banco de dados:**

    ```
    Criar o banco votacao-db no postgres, após isso executar o script presente em sql/install.ddl
    ```

2.  **Executar a aplicação:**

    ```bash
    ./gradlew bootRun
    ```

A API estará disponível em `http://localhost:8080`.

## Testes de Carga

Para executar o teste de carga com k6:

1.  **Iniciar a aplicação e o banco de dados com Docker:**

    ```bash
    docker compose up
    ```

2.  **Executar o teste de carga:**

    ```bash
    docker compose -f docker-compose.k6.yml up
    ``` 

## Documentação da API (Swagger)

A documentação interativa da API está disponível via Swagger UI, acessível em:

`http://localhost:8080/swagger-ui.html`

## Considerações

A solução desenvolvida tem a simplicidade como foco principal, sem serviços extras e várias dependências.  
Em uma solução mais completa os votos poderiam ser cadastrados de forma assíncrona em batch, melhorando consideravelmente a performance e latência da aplicação.