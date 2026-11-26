# CineMel

API REST para gerenciar o catálogo de filmes e a programação de um cinema. É uma aplicação Java com Spring Boot, Spring MVC, Spring Data JPA, PostgreSQL e Swagger.

A [interface web do CineMel](https://cinemel-devops.onrender.com/) permite consultar, cadastrar, editar e excluir registros no celular ou no computador. A barra inferior dá acesso às quatro áreas. As operações de gravação e exclusão pedem confirmação.

| Recurso | Rota | O que representa |
|---|---|---|
| Filmes | `/filmes` | Título, classificação, duração e situação no catálogo |
| Salas | `/salas` | Sala e dimensões de sua grade de assentos |
| Assentos | `/assentos` | Fileira, número e tipo de assento em uma sala |
| Sessões | `/sessoes` | Exibição de um filme em uma sala, com horário, formato, áudio e preço |

As rotas permitem cadastrar, listar, consultar por ID, atualizar e excluir. Uma sessão só é cadastrada quando o filme e a sala informados existem.

## Requisitos funcionais

| ID | Requisito implementado |
|---|---|
| RF01 | Cadastrar, listar, consultar por ID, atualizar e excluir filmes. |
| RF02 | Cadastrar, listar, consultar por ID, atualizar e excluir salas. |
| RF03 | Cadastrar, listar, consultar por ID, atualizar e excluir assentos vinculados a salas. |
| RF04 | Cadastrar, listar, consultar por ID, atualizar e excluir sessões vinculadas a filmes e salas. |

## Requisitos não funcionais

| ID | Requisito implementado |
|---|---|
| RNF01 | A API de demonstração está disponível por HTTPS. |
| RNF02 | As operações HTTP e seus esquemas podem ser consultados e executados pela interface Swagger UI. |
| RNF03 | Os dados do ambiente hospedado são persistidos em PostgreSQL. |
| RNF04 | O GitHub Actions executa a suíte automatizada antes de construir a imagem Docker. O deploy automático da `main` depende da aprovação da CI. |
| RNF05 | A interface web adapta a navegação e os formulários a telas pequenas. |

## Regras de negócio

| ID | Regra implementada |
|---|---|
| RN01 | Para criar ou atualizar uma sessão, o filme e a sala informados devem existir. Uma referência ausente retorna HTTP 404. |
| RN02 | Para criar ou atualizar um assento, a sala informada deve existir. Uma sala ausente retorna HTTP 404. |
| RN03 | Uma sala só pode ter um assento para cada combinação de fileira e número. A API retorna HTTP 409 se houver conflito. Outras salas podem usar a mesma combinação. |
| RN04 | O estado do filme pode ser `EM_CARTAZ` ou `EM_BREVE`. |
| RN05 | O formato da sessão pode ser `DOIS_D`, `TRES_D` ou `IMAX`. O áudio pode ser `DUBLADO` ou `LEGENDADO`. |
| RN06 | O tipo do assento pode ser `NORMAL`, `PCD`, `OBESO` ou `ACOMPANHANTE`. |

### Reenvio de requisições

Repetir um `GET` apenas consulta os dados. Repetir um `POST` válido para filmes, salas ou sessões pode gerar outro registro com novo ID. Para assentos, a primeira gravação retorna HTTP 201 e a tentativa de cadastrar novamente a mesma combinação de sala, fileira e número retorna HTTP 409 com uma mensagem de conflito.

## Requisitos de dados

| ID | Entidade | Dados obrigatórios na persistência | Dados opcionais e restrições |
|---|---|---|---|
| RD01 | Filme | Título (até 150 caracteres) e situação | Sinopse (até 2.000 caracteres), duração em minutos, classificação (até 3 caracteres), data de estreia e URL do pôster |
| RD02 | Sala | Nome (até 50 caracteres) | Total de fileiras e quantidade de assentos por fileira, ambos inteiros |
| RD03 | Assento | Sala existente, fileira (até 2 caracteres), número inteiro e tipo | A combinação de sala, fileira e número é única |
| RD04 | Sessão | Filme e sala existentes, data e hora, formato, áudio e preço base | O preço admite até 10 dígitos, sendo 2 decimais |

Cada registro tem um ID numérico gerado pelo banco. Uma sala pode ter vários assentos e sessões, e um filme pode aparecer em várias sessões.

## Executar a API

Use Java 17 e tenha um banco PostgreSQL chamado `cinemel` disponível. Configure a conexão com `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` de acordo com seu ambiente.

No Windows, execute `.\mvnw.cmd spring-boot:run`. No Linux ou macOS, execute `./mvnw spring-boot:run`. Abra a [interface web](http://localhost:8080/) para usar o sistema ou o [Swagger](http://localhost:8080/docs) para consultar o contrato da API.

## Testes e integração contínua

- [Cenários de testes e comandos para a demonstração](docs/cenarios-de-testes.md)
- [Workflow de integração contínua](.github/workflows/ci.yml)

## API hospedada

O Render acompanha a branch `main` e publica os commits após a CI passar. O serviço gratuito pode demorar para responder à primeira requisição após um período ocioso. [Interface web](https://cinemel-devops.onrender.com/) · [Swagger](https://cinemel-devops.onrender.com/docs)
