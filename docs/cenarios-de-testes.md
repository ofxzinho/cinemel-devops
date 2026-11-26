# Cenários de testes do CineMel

| Tipo | Cenário e entrada | Resultado esperado |
|---|---|---|
| Inicialização | Carregar o Spring Boot com o perfil `test` | O contexto inicializa com o banco de testes |
| API de filmes | Enviar dados válidos em `POST /filmes` | HTTP 201, cabeçalho `Location`, ID gerado e título retornado |
| API de filmes | Consultar um ID inexistente em `GET /filmes/{id}` | HTTP 404 |
| Unidade | Consultar um filme que o repositório informa como ausente | O controlador retorna HTTP 404 |
| Unidade | Excluir um filme que o repositório informa como existente | O controlador chama a exclusão e retorna HTTP 204 |
| Repositório | Gravar B2, A2 e A1 na primeira sala e A1 em outra sala | A consulta da primeira sala retorna somente A1, A2 e B2, nessa ordem |
| API de assentos | Cadastrar A1, repetir o cadastro na mesma sala e cadastrar A1 em outra sala | A primeira e a terceira requisição retornam HTTP 201. A segunda retorna HTTP 409 com mensagem de conflito e a primeira sala continua com um único A1 |
| API de sessões | Enviar `POST /sessoes` com filme e sala cadastrados, formato `DOIS_D`, áudio `LEGENDADO` e preço 32,00 | HTTP 201, `Location`, ID e dados da sessão. A sessão é persistida com os vínculos e o preço esperados |
| API de sessões | Enviar `POST /sessoes` com filme inexistente e sala cadastrada | HTTP 404, ausência de `Location` e quantidade de sessões preservada |
| API de sessões | Enviar `POST /sessoes` com filme cadastrado e sala inexistente | HTTP 404, ausência de `Location` e quantidade de sessões preservada |

Os testes de API acionam as rotas com MockMvc. Os testes com persistência usam H2 em memória. Os cenários de sessão são executados em transações revertidas após cada teste.

## Como reproduzir

| Execução | Windows PowerShell | Linux ou macOS |
|---|---|---|
| Suíte completa | `.\mvnw.cmd clean test` | `./mvnw clean test` |
| Apenas sessões | `.\mvnw.cmd '-Dtest=SessaoApiTest' test` | `./mvnw -Dtest=SessaoApiTest test` |

A suíte contém **10 testes**. O resumo da execução aparece no console, e o Maven Surefire grava os relatórios em `target/surefire-reports/`.

Os testes protegem o contrato HTTP, verificam que as sessões respeitam os vínculos com filme e sala e evitam que uma alteração na consulta de assentos misture salas ou mude a ordem esperada.
