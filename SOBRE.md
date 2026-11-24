# Kanlist

**Kanlist** é um app mobile para quem acompanha anime, mangá e afins e quer um lugar só para organizar tudo: o que está assistindo, o que está lendo, o que quer começar e o que já terminou. Em cada obra você marca os episódios assistidos e os capítulos lidos.

O app é todo em português, não pede cadastro e guarda tudo no próprio aparelho. Os dados das obras (capas, sinopses, episódios, capítulos, personagens) vêm de bases públicas: AniList, MangaDex, Kitsu e MyAnimeList.

---

## Para quem é

- Quem assiste vários animes por temporada e perde a conta de em qual episódio parou.
- Quem lê mangá, manhwa ou manhua e quer saber em qual capítulo está, de preferência com a lista de capítulos na mão.
- Quem quer ter um histórico do que consumiu: quanto tempo gastou, quantos capítulos leu, sua sequência de dias.
- Quem prefere privacidade: sem conta, sem servidor e sem rastrear o que você assiste.

---

## Tipos de obra suportados

| Para assistir (episódios) | Para ler (capítulos) |
|---|---|
| Anime | Mangá (japonês) |
| Filme | Manhwa (coreano / webtoon) |
| OVA / Especial | Manhua (chinês) |
| Donghua (animação chinesa) | Novel / light novel |
| | One-shot |
| | Doujin |

Cada tipo tem cor, ícone e tempo médio próprios. Por exemplo: cerca de 24 min por episódio de anime, 105 min por filme e 6 min por capítulo de mangá. Esses valores são usados para calcular o tempo total que você gastou.

---

## Status de acompanhamento

Cada obra da biblioteca fica em um destes status. O texto se adapta ao tipo da obra (assistir ou ler):

- **Assistindo / Lendo**
- **Quero assistir / Quero ler**
- **Completo**
- **Pausado**
- **Abandonado**
- **Reassistindo / Relendo**

---

## Telas e funcionalidades

### Boas-vindas
Na primeira vez que você abre o app, ele pergunta seu nome ou apelido. Não há login nem e-mail.

### Início
Mostra a sua rotina do dia:
- Saudação pelo horário ("Boa tarde, Mira"), avatar e data por extenso.
- **Faixa da semana**: 7 dias para navegar. Cada dia mostra a atividade registrada e os lançamentos daquela data.
- **Continue de onde parou**: um card grande com a obra mais recente e o botão de continuar.
- **Meta de hoje**: barra de progresso da meta diária (por exemplo, 0/3) e a sua sequência de dias.
- **Rotina de hoje**: as obras que você está assistindo ou lendo, com um botão para marcar o próximo episódio ou capítulo sem sair da tela.
- **Lançamentos do dia**: episódios novos das obras que você acompanha ("Episódio novo saiu!").
- Botão flutuante **+** para buscar ou adicionar uma obra.

### Explorar
Vitrine para descobrir obras, separada em **Para assistir** e **Para ler**:
- **Em alta**: o que todo mundo está assistindo.
- **Temporada atual**: os lançamentos da estação (inverno, primavera, verão ou outono do ano atual).
- **Mangás populares**: os mais lidos no momento.
- **Manhwas** (webtoons coreanos), **Donghuas** (animações chinesas) e **Manhuas** (quadrinhos chineses).
- **Por gênero**: ação, romance, fantasia, terror e outros.
- Cada lista tem **Ver tudo**, que abre uma grade com rolagem infinita.

### Busca
- Pesquisa **ao mesmo tempo** no AniList, MangaDex, Kitsu e MyAnimeList, junta os resultados e remove duplicados.
- Mostra quantos resultados vieram e de quais fontes. Se alguma fonte estiver fora do ar, avisa ("indisponível: Jikan") e segue com as outras.
- Guarda as buscas recentes.
- Um botão de adição rápida coloca a obra na biblioteca direto da lista de resultados.

### Página da obra
É a tela principal do app:
- **Banner e capa** em destaque, título no idioma escolhido (romaji, inglês ou nativo) e título original.
- Selos de **tipo, situação** (em lançamento, finalizado, em breve, em hiato, cancelado), **ano, nota média** e **total de episódios ou capítulos**.
- **Próximo episódio**: quando a obra está em lançamento, mostra quanto falta para sair o próximo.
- **Card de acompanhamento**:
  - anel de progresso com a porcentagem e o número visto ou lido (ex.: 12/28);
  - seletor de status;
  - tempo restante estimado (ex.: "11 h 12 min restantes");
  - botão grande **Marcar ep N / Marcar cap N**;
  - nota pessoal de 1 a 5 estrelas;
  - **favoritar**;
  - **anotações** pessoais ("onde parou, teorias, arcos favoritos…").
- **Lista de episódios ou capítulos**:
  - toque para marcar ou desmarcar;
  - **Marcar até o N** marca tudo até aquele ponto de uma vez;
  - **Marcar só este**, **Inverter ordem** e **Ocultar concluídos**;
  - nos mangás, os capítulos reais vêm do **MangaDex**, com número, volume, título e idioma, filtrados pelos idiomas que você escolheu (ex.: PT-BR e inglês).
- **Sinopse** com "Ler mais". Se o texto estiver em inglês, o app pode **traduzir para português automaticamente**, e há o botão "Ver original (EN)".
- **Informações**: estúdio ou autores, temporada, data de início, duração, volumes e país de origem.
- **Personagens**, **Relacionados** (sequências, prequências, spin-offs), **Se você gostou…** (recomendações) e **Onde encontrar** (links oficiais e de streaming).
- **Menu de opções**: zerar progresso, remover da biblioteca e adicionar a uma coleção.
- **Funciona offline**: obras que já estão na biblioteca abrem mesmo sem internet, com os dados salvos.

### Biblioteca
Tem duas abas: **Minha lista** e **Coleções**.
- **Minha lista**:
  - chips por status com a contagem de cada um ("Em andamento 4", "Planejado 12"…);
  - filtro de texto;
  - filtro por tipo (só manhwas, só filmes etc.);
  - chip de favoritos; ordenação por atividade recente, adicionados recentemente, título (A–Z), progresso ou minha nota;
  - cada obra mostra capa, tipo, status, barra de progresso e um botão **+1** para avançar sem abrir a obra.
- **Coleções**: listas personalizadas com nome e cor ("Para maratonar", "Favoritos de 2026"…). É possível adicionar e remover obras, renomear e excluir.

### Perfil
- **Estatísticas**: episódios assistidos, capítulos lidos, tempo total gasto e melhor sequência de dias.
- **Gráfico de atividade** dos últimos 14 dias, destacando os dias em que você bateu a meta.
- **Por tipo**: quanto de cada tipo de obra você tem.
- **Preferências**:
  - nome;
  - tema (claro, escuro ou do sistema);
  - idioma dos títulos;
  - meta diária;
  - idiomas dos capítulos;
  - tradução automática de sinopses;
  - vibração ao marcar;
  - mostrar ou ocultar conteúdo adulto.
- **Fontes de dados**: ativar ou desativar cada base (AniList, MangaDex, Kitsu, MyAnimeList).
- **Backup**: exportar a biblioteca inteira em JSON (copiada para a área de transferência) e importar de volta, por exemplo ao trocar de celular.
- **Manutenção**: limpar o cache e apagar todos os dados, com confirmação.

---

## Visual

- Estilo aconchegante e arredondado, inspirado em apps de rotina: fundo creme, cards brancos com sombra suave, cantos bem arredondados e ícones sobre "azulejos" em tons pastel (pêssego, sálvia, lavanda, céu, rosa).
- Cor de destaque marrom-chocolate e laranja-caramelo para progresso e ações.
- **Tema claro e tema escuro** (tons quentes amarronzados), que podem seguir o tema do sistema.
- Tipografia **Urbanist**, com bastante espaço entre os elementos.
- Esqueletos de carregamento, telas vazias ilustradas com um mascote, vibração ao marcar episódios e animações suaves.

---

## De onde vêm os dados

| Fonte | Uso no app |
|---|---|
| **AniList** | Fonte principal: busca, em alta, temporada, detalhes, personagens, relacionados, recomendações e próximo episódio |
| **MangaDex** | Lista real de capítulos de mangás, manhwas e manhuas, com idioma e volume, além de capas |
| **Kitsu** | Lista de episódios com título e cruzamento de IDs com o MyAnimeList |
| **MyAnimeList (Jikan)** | Fonte complementar na busca e nos detalhes |
| **MyMemory** | Tradução automática das sinopses para português |

Nenhuma dessas fontes exige chave nem conta. O app consulta várias delas e, quando uma falha ou demora, **usa as outras**, para que a tela não fique vazia. As respostas ficam em cache para abrir mais rápido e economizar dados.

---

## Privacidade e armazenamento

- **Sem conta, sem login e sem nuvem.** Biblioteca, progresso, notas, anotações, coleções e histórico ficam só no seu aparelho.
- O banco local é versionado e tem **backup automático** antes de qualquer atualização de formato.
- A única forma de levar os dados para outro aparelho é o **exportar/importar** em JSON, e isso fica sob seu controle.

---

## Tecnologia

- **Expo SDK 58** com **React Native 0.88**, **React 19.3** e **TypeScript 6**.
- **expo-router**: navegação por arquivos, com abas e telas empilhadas.
- **TanStack Query**: cache e carregamento dos dados das APIs.
- **Zustand** + **AsyncStorage**: estado e persistência local da biblioteca.
- Código organizado em camadas (`domain`, `data/sources`, `data/repositories`, `state`, `queries`, `ui`, `features`). As telas não falam direto com as APIs.
- Um servidor pequeno (Hono, em `packages/web`) serve só de proxy para o MangaDex na versão de pré-visualização web, que é bloqueada por CORS no navegador. No celular o app acessa as APIs diretamente.

---

## Em uma frase

> **Kanlist é o seu diário de animes e mangás: você encontra a obra, marca o episódio ou capítulo com um toque e acompanha seu progresso e sua sequência de dias, tudo em português, sem conta e com os dados guardados só no seu celular.**
