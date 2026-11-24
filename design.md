# Kanlist — Design System

Fonte única de verdade para a UI do app mobile (Expo). Baseado na print de referência: rotina diária suave, base creme, cards brancos arredondados, tiles pastel, acento laranja e marrom chocolate.

## Personalidade
Calmo, acolhedor, organizado. Parece um diário de rotina — não um painel técnico. Muito respiro, cantos generosos, nada de bordas duras. Ilustrações em clay 3D (mascote raposa, pilha de mangás + TV) dão o toque anime sem poluir.

## Tipografia
- Família: **Urbanist** (geométrica arredondada, igual à print). Pesos 400 / 500 / 600 / 700 / 800.
- Display (saudação): 30 / 700, letter-spacing -0.5, nome em 18 / 500 ao lado.
- Título de seção: 19 / 700.
- Título de card: 16 / 600.
- Corpo: 14 / 500, line-height 20.
- Secundário/legenda: 12–13 / 500, cor `mutedForeground`.
- Números grandes (stats): 28 / 800.

## Cores — Claro
| Token | Hex | Uso |
|---|---|---|
| background | `#F6F1EE` | Fundo de tela (creme rosado) |
| card | `#FFFFFF` | Cards, círculos do WeekStrip |
| foreground | `#1E1B19` | Texto principal |
| mutedForeground | `#8E8782` | Texto secundário |
| border | `#EEE6E1` | Divisores sutis |
| primary | `#F07C1F` | Checks, progresso, chips ativos |
| ink | `#2A2624` | Dia selecionado, pílulas escuras |
| brown | `#5C2A0D` | Botão do hero, FAB |
| hero | `#FFD6BA` | Card destaque (pêssego) |
| heroForeground | `#3A2214` | Texto no card destaque |

Tiles pastel por tipo de mídia (fundo / ícone):
- Anime `#FFDCC4 / #E8741A` · Filme `#FDE3A7 / #B7791F` · OVA/ONA `#FFD9D2 / #D9534F`
- Donghua `#FBD5EE / #C2449B` · Mangá `#E3EFCB / #5E8B23` · Manhwa `#D7E9F7 / #2F7BBF`
- Manhua `#E6DEFA / #6C4FD0` · Novel `#F3E6D8 / #8A5A2B` · One-shot `#DDF1EC / #2C8C78` · Doujin `#F7DDE4 / #B24668`

## Cores — Escuro (quente)
background `#151210` · card `#211C19` · foreground `#F4ECE6` · mutedForeground `#9E948D` · border `#2E2723` · primary `#FF8B2E` · ink `#F4ECE6` · brown `#FF8B2E` · hero `#3A251A` · heroForeground `#FFE3CF`. Tiles pastel escurecem para ~18% de opacidade da cor do ícone.

## Forma & Espaço
- Raios: tela 0 · card 22 · hero 26 · tile 14 · pílula 999 · círculo do dia 999.
- Padding horizontal da tela: 22. Gap entre cards de lista: 12. Gap entre seções: 28.
- Sombra de card: `0 6px 18px rgba(92, 42, 13, 0.06)`. FAB: `0 10px 24px rgba(92,42,13,0.35)`.
- Alvos de toque ≥ 44pt.

## Componentes
- **WeekStrip**: rótulos Seg…Dom em cima (13/500 muted; hoje em 700 ink), círculos brancos 46pt com número; selecionado = círculo `ink` com número branco. Ponto laranja abaixo indica atividade no dia.
- **HeroCard**: fundo `hero`, título 19/700, texto 13/500, botão pílula `brown` com texto branco, ilustração à direita sangrando levemente.
- **RoutineRow** (lista principal): check circular à esquerda (laranja preenchido quando feito hoje, contorno cinza quando não) ligado por linha tracejada vertical; card branco com tile pastel (capa da obra), título 16/600, subtítulo muted ("Ep 12 de 24 · Sequência 3 dias"), divisor vertical e coluna com ícone de relógio + duração.
- **Chip**: pílula 34pt, fundo card + borda; ativo = `ink` com texto branco.
- **ProgressBar**: trilho `border`, preenchimento `primary`, altura 6, raio pílula. **ProgressRing**: SVG, traço 8.
- **FAB**: círculo 58pt `brown` com "+" branco, canto inferior direito acima da tab bar.
- **Sheet**: bottom sheet com raio 28 no topo, alça 40×5.
- **Tab bar**: fundo card, sem borda superior, ícones Ionicons; ativo = `brown`/`primary`.

## Movimento
Entrada de tela com fade + subida (Animated, `useNativeDriver: false`), escalonada 60ms por seção. Check com escala 0.85→1 e haptic leve (nativo). Nada de animação em loop.

## Voz (PT-BR)
Frases curtas e calorosas: "Bom dia, Mira", "Continue de onde parou", "Nada por aqui ainda". Usar "Assistindo"/"Lendo" conforme o tipo da obra.
