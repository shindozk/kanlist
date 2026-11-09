# Kanlist - Kotlin Version

Aplicativo mobile para organizar animes, mangás e afins. Versão nativa Android em Kotlin com Material You.

## 🎯 Funcionalidades

- **Biblioteca pessoal**: Organize o que está assistindo/lendo, planejado, completado, pausado, abandonado
- **Progresso detalhado**: Marque episódios e capítulos com um toque, marque até determinado número
- **Múltiplas fontes**: Busca simultânea no AniList, MangaDex, Kitsu e MyAnimeList (Jikan)
- **Capítulos reais**: Lista de capítulos do MangaDex com idioma, volume, grupo de scanlation
- **Offline-first**: Dados salvos localmente com Room, funcionam sem internet
- **Material You**: Design system adaptativo com cores dinâmicas, tema claro/escuro/sistema
- **Privacidade**: Sem conta, sem login, sem nuvem - tudo no seu dispositivo
- **Backup/Restore**: Exportar/importar biblioteca em JSON
- **Estatísticas**: Tempo gasto, sequências de dias, gráficos de atividade

## 🏗️ Arquitetura

```
app/
├── data/
│   ├── local/           # Room Database, DAOs, DataStore
│   ├── repository/      # Repository implementations
│   ├── source/
│   │   ├── api/         # Retrofit services (AniList, MangaDex, Kitsu, Jikan)
│   │   ├── local/       # Preferences DataSource
│   │   └── mapper/      # API → Domain mappers
├── domain/
│   ├── model/           # Entities (Media, UserMedia, Chapter, etc.)
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic (Use Cases)
├── presentation/
│   ├── ui/              # Compose screens
│   │   ├── home/
│   │   ├── explore/
│   │   ├── search/
│   │   ├── library/
│   │   ├── profile/
│   │   ├── workdetail/
│   │   └── onboarding/
│   ├── viewmodel/       # Hilt ViewModels
│   ├── navigation/      # Navigation graph
│   └── theme/           # Material You theme
├── di/                  # Hilt modules
└── util/                # Utilities
```

## 🛠️ Tecnologias

- **Kotlin** 1.9.23
- **Jetpack Compose** + **Material 3** (Material You)
- **Hilt** para Dependency Injection
- **Room** para banco de dados local
- **DataStore** para preferências
- **Retrofit** + **Moshi** para APIs
- **Coil** para carregamento de imagens
- **Coroutines** + **Flow** para assincronismo
- **Navigation Compose** para navegação
- **Paging 3** para listas infinitas
- **WorkManager** para tarefas em background
- **ktlint** + **detekt** para qualidade de código

## 📱 Telas

1. **Onboarding** - Pergunta nome na primeira execução
2. **Início** - Saudação, semana, continue assistindo, meta diária, rotina, lançamentos
3. **Explorar** - Abas "Assistir" e "Ler" com trending, sazonal, por tipo
4. **Busca** - Busca multi-fonte com filtros, adição rápida à biblioteca
5. **Biblioteca** - Abas "Minha Lista" (filtros, ordenação, +1) e "Coleções"
6. **Obra** - Banner, capa, progresso, episódios/capítulos, sinopse, info, relacionados
7. **Perfil** - Stats, gráfico de atividade, preferências, backup, manutenção

## 🎨 Design System

Baseado no design original do Kanlist:
- **Cores**: Creme (claro) / Marrom quente (escuro), acento laranja-caramelo e marrom-chocolate
- **Tiles pastel** por tipo de mídia (pêssego, sálvia, lavanda, céu, rosa)
- **Tipografia**: Urbanist (geométrica arredondada)
- **Formas**: Cantos arredondados generosos (22dp cards, 26dp hero, 14dp tiles)
- **Sombras**: Suaves, elevadas
- **Animações**: Entrada escalonada, feedback háptico ao marcar

## 📦 Build Local

### Requisitos
- **x86_64 Linux/macOS/Windows** (as build tools do Android SDK são apenas x86_64)
- JDK 21
- Android SDK (API 34, Build Tools 34.0.0)

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease

# Testes
./gradlew test
./gradlew connectedAndroidTest

# Qualidade de código
./gradlew ktlintCheck detekt
```

### ⚠️ ARM64 (AndroPI, Raspberry Pi, Apple Silicon Linux)
As build tools do Android SDK (`aapt2`, `aidl`, `zipalign`, etc.) são **apenas x86_64**. Não há builds oficiais ARM64 do Google.

**Opções para ARM64:**
1. **GitHub Actions** (recomendado) - usa runners x86_64 na nuvem
2. [hamza72x/android-sdk-linux-arm64](https://github.com/hamza72x/android-sdk-linux-arm64) - builds ARM64 do AOSP (requer compilar do fonte)

## 🚀 GitHub Actions (Build na Nuvem)

O projeto inclui workflows prontos:

### Build sob demanda
```bash
# Via GitHub CLI
gh workflow run build.yml -f build_type=debug
gh workflow run build.yml -f build_type=release
```

### CI Automático
- **Push/PR**: roda lint (ktlint + detekt), testes unitários, build debug/release
- **Tags `v*`**: cria release automático com APK assinado

### Artifacts
- `kanlist-debug-apk` - APK debug (30 dias)
- `kanlist-release-apk` - APK release (90 dias)

## 📋 Requisitos

- Android 8.0+ (API 26)
- Kotlin 1.9.23
- Gradle 8.6
- JDK 21

## 🔑 APIs Utilizadas (Sem Chave Necessária)

- **AniList** (GraphQL) - Principal: busca, detalhes, sazonal, personagens, relacionados
- **MangaDex** (REST) - Capítulos reais de mangás/manhwas/manhuas
- **Kitsu** (REST) - Episódios, cruzamento de IDs
- **MyAnimeList/Jikan** (REST) - Busca complementar, detalhes
- **MyMemory** - Tradução automática de sinopses (EN → PT)

## 📄 Licença

MIT License - Veja [LICENSE](LICENSE) para detalhes.