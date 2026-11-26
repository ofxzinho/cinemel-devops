# CineMel

API REST para gerenciar o catálogo de filmes e a programação de um cinema. É uma aplicação Java com Spring Boot, Spring MVC, Spring Data JPA, PostgreSQL e Swagger.

| Recurso | Rota | O que representa |
|---|---|---|
| Filmes | `/filmes` | Título, classificação, duração e situação no catálogo |
| Salas | `/salas` | Sala e dimensões de sua grade de assentos |
| Assentos | `/assentos` | Fileira, número e tipo de assento em uma sala |
| Sessões | `/sessoes` | Exibição de um filme em uma sala, com horário, formato, áudio e preço |

As rotas permitem cadastrar, listar, consultar por ID, atualizar e excluir. Uma sessão só é cadastrada quando o filme e a sala informados existem.

## Executar a API

Use Java 17 e tenha um banco PostgreSQL chamado `cinemel` disponível. Configure a conexão com `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` de acordo com seu ambiente.

No Windows, execute `.\mvnw.cmd spring-boot:run`. No Linux ou macOS, execute `./mvnw spring-boot:run`. Abra o [Swagger](http://localhost:8080/docs) para consultar os esquemas e experimentar as operações.

## Testes e apresentação

- [Cenários de testes e comandos para a demonstração](docs/cenarios-de-testes.md)
- [Material da apresentação de 4 de dezembro de 2026](docs/apresentacao.md)
- [Workflow de integração contínua](.github/workflows/ci.yml)
