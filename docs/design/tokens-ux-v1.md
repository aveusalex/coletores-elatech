# UX v1 — tokens da identidade Elatech (implementados)

Origem: Claude Design, sistema "Industry" + marca Elatech. Entregue em
2026-09-02 (`.design-src/Elatech Checkout Lab.dc.html`, não versionado).
Implementado no app em `res/values/colors.xml` e `res/values/themes.xml`.

## Cores (tema claro)

| Token | Hex | Uso |
| --- | --- | --- |
| `elatech_orange` | `#F5821F` | primária. **Nunca** texto branco em cima (2,4:1). |
| `elatech_orange_pressed` | `#C25E06` | primária pressionada |
| `elatech_orange_on_light` | `#C25E06` | laranja como texto/ícone sobre claro (4,6:1) |
| `elatech_navy` | `#14386B` | top bar / botão secundário (9,1:1 c/ branco) |
| `elatech_navy_deep` | `#0C2445` | `on_primary` (6,9:1 sobre laranja), status bar |
| `elatech_navy_container` | `#E8EEF7` | faixa de info, banner "incrementado" |
| `elatech_orange_soft` | `#FFB067` | caps "ELATECH" na subbar, ação do snackbar |
| `bg` | `#F7F7F8` | fundo |
| `surface` | `#FFFFFF` | superfícies |
| `on_surface` | `#1D1F20` | texto (15,4:1) |
| `on_surface_variant` | `#5D5D60` | texto secundário (6,3:1) |
| `outline` `#B7B7BA` · `divider` `#E7E7EA` | | bordas |
| `success` `#0F5C39` / `success_container` `#E4F3EA` | | "adicionado", status compatível |
| `warning` `#8A4B00` / `warning_container` `#FFF1DA` | | "código desconhecido" |
| `error` `#B3261E` / `error_container` `#FBE9E7` / `on_error_container` `#8C1D18` | | erro, incompatível |
| neutros `n50…n800` | `#F5F5F8`…`#2B2B2D` | rampa Industry |

Banner de leitura: wait `#E7E7EA`/`#5D5D60` · added `#E4F3EA`/`#0F5C39` ·
incremented `#E8EEF7`/`#14386B` · unknown `#FFF1DA`/`#8A4B00` · sale-done
`#0F5C39`/`#FFFFFF`.

## Tipografia

Barlow (corpo) + Barlow Condensed (títulos, rótulos, total). OFL, embarcadas
em `res/font` (`LICENSE-fonts-OFL.txt`). Estilos em `themes.xml`:
`TextTotal` (Condensed 40sp/700, tabular), `TextSection` (Condensed 22sp/600),
`TextTopbarTitle` (Condensed 20sp/600), `TextBodyLarge` (Barlow 16sp),
`TextBodyMedium` (Barlow 14sp), `TextCaps` (Barlow 12sp/600, +0.12em, caps),
`TextMono` (monospace).

## Forma / espaço

Raio 4dp padrão (`Shape.Elatech.Small`), 0dp em cards wireframe. Espaço
4/8/12/16/24dp. Elevação: 0 conteúdo, 2dp barra inferior, 3dp sheet/dialog,
6dp snackbar. Botão primário 56dp, secundário 48dp, alvo mínimo 48dp.

## Ícones

Lucide, 24dp, traço 1,5dp, como `VectorDrawable` em `res/drawable/ic_*`:
check-circle, alert-triangle, x-circle, barcode, settings, history, trash,
plus, minus, arrow-left, refresh, activity, chevron-down/up, more-vert, e o
selo Elatech (`ic_elatech_mark`, estrela de 4 pontas — **placeholder**,
trocar pelo SVG oficial quando chegar).

## Pendências de identidade

- **Logo oficial**: `ic_elatech_mark` é placeholder. Substituir por
  `VectorDrawable` da marca (horizontal + símbolo).
- **Tema escuro**: não desenhado nesta v1. Tokens já são semânticos; adicionar
  `values-night/` quando houver a paleta escura.
