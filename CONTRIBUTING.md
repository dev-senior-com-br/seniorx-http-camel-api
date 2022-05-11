# SeniorX HTTP Camel API

## Gerando um snapshot

Para gerar um snapshot para testar as alterações no projeto deve-se seguir os seguintes passos:
- Alterar a versão no pom.xml do projeto para a nova versão que será liberada seguida de `-alpha-0`. Ou incrementar o `-alpha-?`. Exemplo: `1.0.1-alpha-0`, ou `1.0.1-alpha-1`.
- Commitar e fazer push da alteração.
- Criar uma tag no branch de trabalho iniciando com a letra `v` seguida da versão que está no pom.xml. Exemplo: `v1.0.1-alpha-0`.
- Acessar as [releases do projeto](https://github.com/dev-senior-com-br/seniorx-http-camel-api/releases) e clicar em "Draft a new release".
- No campo "Choose a tag", selecione a tag criada nos passos anteriores.
- Escreva uma nota para a release, ou clieuq no botão "Auto-generate release notes".
- Marque a opção "This is a pre-release".
- Clique em "Publish release".

Para acompanhar o progresso da release basta acessar as [actions do projeto](https://github.com/dev-senior-com-br/seniorx-http-camel-api/actions). Terá um workflow executando com o nome da tag da release.

Após o término da execução do workflow, acesse a [pasta do projeto no maven](https://repo1.maven.org/maven2/br/com/senior/seniorx-http-camel-api/) e aguarde a release aparecer lá. Pode levar alguns minutos para a release ser publicada no maven, é necessário atualizar a página (F5) de tempos em tempos até que a pasta da release apareça.

Após a release aparecer no maven ela está pronta para ser utilizada.

## Gerando uma release

Para gerar uma release oficial deve-se seguir os seguintes passos:
- Alterar a versão no pom.xml do projeto para a nova versão que será liberada.
- Commitar e fazer push da alteração.
- Acessar as [releases do projeto](https://github.com/dev-senior-com-br/seniorx-http-camel-api/releases) e clicar em "Draft a new release".
- No campo "Choose a tag", escreva a nova versão que será liberada iniciando com a letra `v`. Exemplo: `v1.0.1`.
- Clique na opção "Create new tag: <tag digitada> on publish" logo abaixo do campo onde a tag foi digitada.
- Escreva uma nota para a release, ou clieuq no botão "Auto-generate release notes".
- Clique em "Publish release".

Para acompanhar o progresso da release basta acessar as [actions do projeto](https://github.com/dev-senior-com-br/seniorx-http-camel-api/actions). Terá um workflow executando com o nome da tag da release.

Após o término da execução do workflow, acesse a [pasta do projeto no maven](https://repo1.maven.org/maven2/br/com/senior/seniorx-http-camel-api/) e aguarde a release aparecer lá. Pode levar alguns minutos para a release ser publicada no maven, é necessário atualizar a página (F5) de tempos em tempos até que a pasta da release apareça.

Após a release aparecer no maven ela está pronta para ser utilizada.
