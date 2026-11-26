# CineMel · Segunda menção de Engenharia de Software

**4 de dezembro de 2026**

## 1. Evolução do CineMel

A API organiza filmes, salas, assentos e sessões. Uma sessão liga um filme a uma sala e informa horário, formato, áudio e preço. Nesta etapa, o grupo automatizou verificações dessas funcionalidades e passou a executá-las na integração contínua.

## 2. Cenários e resultados

- Mostrem a [tabela de cenários](cenarios-de-testes.md) e executem a suíte.
- Abram `SessaoApiTest` e exibam os três casos de criação. Com referências válidas, a API retorna 201 e persiste a sessão. Filme ou sala inexistente gera 404 e preserva os registros.
- Expliquem como esses testes detectam mudanças indesejadas nos códigos HTTP, na persistência de sessões e na ordenação dos assentos.

## 3. Demonstração da integração contínua

O [workflow Cinemel](../.github/workflows/ci.yml) roda em pushes e pull requests para `main` e `develop` e pode ser acionado manualmente pelo GitHub Actions.

1. O runner Linux baixa o código, configura o Temurin 17 e habilita cache Maven.
2. O Maven Wrapper executa `./mvnw clean test`. Uma falha nesta etapa impede a construção da imagem.
3. O Docker constrói uma imagem identificada pelo SHA do commit. Nas branches previstas, a etapa seguinte publica a imagem no Docker Hub quando habilitada e com as credenciais configuradas.
4. Abram a [aba Actions](https://github.com/ofxzinho/cinemel-devops/actions) na execução do commit apresentado. Expandam **Executar testes**, mostrem o resumo do Surefire e depois expandam **Construir imagem Docker**. Se acionarem uma execução durante a apresentação, deixem uma execução concluída aberta para mostrar os resultados.

## 4. Observabilidade aplicada à criação de sessões

Observabilidade é a capacidade de entender o que acontece dentro do sistema a partir dos sinais que ele emite. Se `POST /sessoes` começar a ficar lento, métricas ajudam a perceber a mudança, logs dão contexto e traces mostram onde cada requisição gastou tempo.

| Sinal | Exemplo no CineMel | Ferramenta |
|---|---|---|
| Métricas | Volume de requisições, taxa de respostas 5xx e latência p95 de `POST /sessoes` | **Prometheus** coleta as métricas. **Grafana** exibe o histórico e pode alertar sobre desvios sustentados |
| Logs | Rota, status, duração e ID do trace. Um evento de validação pode indicar filme ou sala ausente | **Elastic Stack (ELK)** pode processar, indexar e permitir a consulta dos logs com Logstash, Elasticsearch e Kibana |
| Traces | Um trace acompanha a requisição e mede as etapas de consulta do filme, consulta da sala e gravação da sessão | **OpenTelemetry** instrumenta a aplicação e exporta os sinais para ferramentas compatíveis |

Se o p95 aumentar antes de surgirem erros, o painel sinaliza a degradação. A equipe pode filtrar os logs pelo horário, correlacionar um registro com seu trace e investigar a etapa que ficou mais lenta. Depois da correção, a mesma métrica permite comparar o comportamento antes e depois. Este é um exemplo de aplicação da telemetria ao projeto.

## 5. Entrega

O representante envia por e-mail a [documentação da solução](../README.md), os [cenários de testes](cenarios-de-testes.md), o [repositório](https://github.com/ofxzinho/cinemel-devops) e o material usado na apresentação. O corpo do e-mail deve trazer o nome **CineMel** e os nomes completos de todos os integrantes.
