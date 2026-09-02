# Plano contínuo e dump de contexto

> Esta é a fonte de verdade operacional do projeto. Atualize-a ao terminar uma fase, mudar escopo, descobrir uma limitação do aparelho ou tomar uma decisão técnica. Não registrar credenciais, dados comerciais, serial completo, IMEI ou IMSI.

## Objetivo

Aprender a construir e operar, de forma isolada, um checkout móvel no MovFast Ranger 2N: cadastrar produtos fictícios, bipar códigos, montar carrinho, alertar itens inexistentes e fechar vendas simuladas. O desenho preserva uma fronteira para um middleware futuro, mas não cria nem integra middleware nesta etapa.

## Estado atual — 2026-09-01

- **Fatos confirmados na fonte:** Ranger 2N é Android; Barcode Utility suporta broadcast; XCScanner SDK possui documentação e fonte pública no ecossistema MovFast/MERTECH.
- **Ainda não confirmado no aparelho:** variante física do scanner, efeito funcional da White list, necessidade da permissão de broadcast no manifesto e entrega da leitura ao nosso aplicativo.
- **O que existe no repositório:** wiki técnica, arquitetura, ADR e convenções de documentação.
- **O que não existe:** aplicativo, APK, dados reais, conexão com Clip Store/ERP, pagamento ou infraestrutura de middleware.

## Timeline

╭─ FASE 1 — Base de conhecimento e direção ─╮
│ ✅ done · Escopo offline, seguro e didático definido. │
│ ✅ done · Wiki, fontes, arquitetura e ADR publicados. │
│ ✅ done · Fronteira para middleware futuro registrada. │
╰─────────────────────────────────────────────╯

╭─ FASE 2 — Descoberta controlada do aparelho ─╮
│ 🔄 doing · 🔴 VOCÊ ESTÁ AQUI │
│ ✅ done · Registrar Android, firmware e Barcode Utility. │
│ ✅ done · Validar Scan Demo com Code 128, EAN-13 e QR Code. │
│ ✅ done · Registrar ação/chave/permissão e destino do broadcast. │
│ ✅ done · Verificar que White list é somente chave nesta interface. │
│ ✅ done · Habilitar depuração USB e confirmar conexão ADB. │
╰────────────────────────────────────────────────╯

╭─ FASE 3 — Prova de integração do scanner ─╮
│ ✅ done · App de diagnóstico criado, compilado, instalado, aberto. │
│ ✅ done · Permissão CAMERA concedida (ADB: granted=true). │
│ ✅ done · Descoberto: firmware emite `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST`; │
│          o caminho `android.intent.scanResult`/`scanKey`/PackageName/ClassName é INERTE. │
│ ✅ done · v0.2.0→v0.3.0: manifest receiver é barrado no Android 13; runtime receiver entrega. │
│ ✅ done · Um evento por bip, previsível (EAN-13 e QR), com simbologia. Critério atendido. │
│ ✅ done · Contrato fixado em `ScannerContract`; caminho morto removido do código (v0.4.1). │
│ ✅ done · ⚠️ ALTERADO · Decisão registrada — ADR 0002: broadcast agora, XCScanner SDK no produto. │
│ ⏳ pending · Barcode Utility PackageName/ClassName ficam como estão (inertes, documentado). │
╰───────────────────────────────────────────────╯

╭─ FASE 3.5 — Adoção do XCScanner SDK (produto) · 🔴 VOCÊ ESTÁ AQUI ─╮
│ ✅ done · Costura `ScannerSource` + `BroadcastScannerSource` (transporte atual). │
│ ✅ done · `ScannerConfig` (beep, volume, gatilho, sufixo, saída, simbologias). │
│ ✅ done · 🔒 AUTORIZADO · aar vendorado: `xcscanner_qrcode_v1.3.56.1.14`, commit fixo, SHA-256 → `app/libs/PROVENANCE.md`. │
│ ✅ done · `SdkScannerSource`: bind do serviço OK, `getSdkVersion=1.3.56.1.14`, `applyConfig` aplicado (serviço respondeu). │
│ ✅ done · Toggle Broadcast/SDK na tela de diagnóstico. │
│ ✅ done · Entrega por callback provada: `onResult sym=EAN-13` `7896445490550`. │
│ ✅ done · Versões: `sdk=1.3.56.1.14 service=1.3.62.1.4 match=true`. │
│ ✅ done · Checkout (`MainActivity`) migrado p/ `SdkScannerSource` + `CHECKOUT_DEFAULT` (saída broadcast puro → sem `_INPUT`). │
│ ⚠️ nota · Config do scanner é device-global. Deploy alvo = coletor dedicado (kiosk). │
╰───────────────────────────────────────────────╯

╭─ FASE 4 — Checkout offline de aprendizagem ─╮
│ ✅ done · Domínio: Money (centavos), Product, Cart, Catalog, Sale, CheckoutController. │
│ ✅ done · Tela de checkout: total, carrinho, +/−/remover, finalizar, limpar. │
│ ✅ done · Código desconhecido → cadastro de produto fictício. Leitura repetida → debounce 400ms. │
│ ✅ done · 7 testes JVM do fluxo (add/incremento/total/debounce/desconhecido/venda). │
│ ⏳ pending · Trocar `InMemoryCatalog`/`InMemorySaleHistory` por Room. │
│ ⏳ pending · Tela de ajustes do scanner (ScannerConfig) — depende do SDK p/ aplicar. │
╰────────────────────────────────────────────────╯

╭─ FASE 5 — Qualidade e operação de laboratório ─╮
│ ⏳ pending · Testar reinício, bateria, tela bloqueada e rotação. │
│ ⏳ pending · Executar casos de EAN, QR, item desconhecido e duplicidade. │
│ ⏳ pending · Decidir se Kiosk é útil somente para demonstração controlada. │
╰──────────────────────────────────────────────────╯

╭─ FASE 6 — Decisão de evolução para middleware ─╮
│ ⏳ pending · Identificar sistema-alvo e requisito real de integração. │
│ ⏳ pending · Definir contrato, segurança, sincronização e idempotência. │
│ ⏳ pending · Autorizar explicitamente qualquer conexão externa. │
╰────────────────────────────────────────────────────╯

[████████████▓] ~90%

## Sequência detalhada

### Fase 2 — descoberta do aparelho

**Objetivo:** provar as capacidades do Ranger concreto sem alterar sua operação além da depuração temporária.

1. Identificar a unidade por apelido interno; registrar versão de Android, patch, build/firmware, Barcode Utility e serviço de scanner.
2. Abrir Barcode Utility → **Scan Demo** e ler ao menos um EAN-13 e um QR Code de teste conhecidos.
3. Registrar as opções existentes: modo de saída, ação, chave, sufixos, simbologias habilitadas e feedback sonoro/vibração. Não alterar opções protegidas/de debug.
4. Habilitar Opções do desenvolvedor e Depuração USB somente no aparelho de laboratório; conectar pelo USB-C de dados e aceitar a chave RSA do computador de desenvolvimento.
5. Confirmar que `adb devices` exibe o aparelho autorizado. Desconectar e revogar a autorização ao fim da sessão se o computador não for dedicado.

**Critério de passagem:** registro completo da unidade, leituras demonstradas e conexão ADB autorizada.

**Documentos a atualizar:** `collector/ranger-2n.md`, `development/conectar-e-instalar.md` e esta página.

### Fase 3 — prova de integração do scanner

**Objetivo:** provar a entrega confiável de uma leitura ao aplicativo, antes de criar o checkout.

1. Criar projeto Android Kotlin mínimo, sem catálogo, login ou internet.
2. Implementar receptor de broadcast configurável para a ação/chave observadas no aparelho.
3. Mostrar valor recebido, simbologia quando disponível e horário; guardar somente log local de diagnóstico.
4. Testar leitura simples, repetida, item EAN e QR; confirmar exatamente um evento por bip.
5. Repetir após bloquear/desbloquear e reiniciar o Ranger.
6. Consultar a versão do serviço. Só se o broadcast não cobrir a necessidade, trazer o XCScanner SDK com versão/commit fixados e documentados.

**Critério de passagem:** leitura recebida de forma previsível em aplicativo próprio e configuração exportada/revisada.

**Documentos a atualizar:** `scanner/integracao-xcscanner.md`, novo registro de sessão em `docs/collector/` e esta página.

### Fase 4 — checkout offline de aprendizagem

**Objetivo:** construir o fluxo completo sem dados ou efeitos externos.

1. Modelar `Product`, `CartLine`, `SaleDraft` e `SaleCompleted`; preço em centavos inteiros.
2. Criar banco Room/SQLite com massa fictícia e cadastro local de itens.
3. Implementar tela de carrinho: leitura adiciona/incrementa item; leitura desconhecida mostra alerta e opção de cadastrar produto fictício.
4. Implementar remoção, alteração de quantidade, total e confirmação de compra simulada.
5. Salvar histórico local e limpar carrinho ao concluir.

**Critério de passagem:** demonstração reproduzível de cadastro → bip conhecido → bip desconhecido → fechamento simulado.

**Documentos a atualizar:** `architecture/visao-geral.md`, `contracts/`, casos de teste e esta página.

### Fase 5 — qualidade de laboratório

**Objetivo:** descobrir falhas reais de uso antes de considerar qualquer evolução.

1. Executar matriz de testes para códigos suportados, duplicidade, alta velocidade, bateria baixa, reinício e retorno do app.
2. Registrar falhas, causa provável, reprodução e decisão de correção.
3. Testar recuperação do banco local e atualização do app de depuração.
4. Avaliar Kiosk apenas se o fluxo já for estável e houver necessidade de demonstração em modo restrito.

**Critério de passagem:** todos os casos críticos documentados e demonstráveis sem perda de dados fictícios.

### Fase 6 — decisão de middleware

**Objetivo:** decidir com evidência se uma integração é necessária e qual problema ela resolveria.

Pré-condições obrigatórias:

- sistema-alvo identificado e autorizado;
- contratos de dados e dono operacional definidos;
- regras de sincronização offline, conflito e idempotência especificadas;
- autenticação, LGPD, auditoria e suporte avaliados;
- ADR aprovado antes de criar serviço ou conectar qualquer ambiente.

## Dump obrigatório ao fim de cada sessão

Ao encerrar um trabalho relevante, atualizar a seção abaixo e, se houver mudança de decisão, criar/atualizar um ADR. O texto deve permitir que outra pessoa retome o trabalho sem acessar esta conversa.

### Registro de continuidade

| Data | Fase | O que foi feito | Fatos confirmados | Pendências / bloqueios | Arquivos alterados | Próxima ação |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-01 | 1 | Repositório e wiki inicial publicados. | Fontes do R2N, Barcode Utility e XCScanner SDK registradas. | Teste no aparelho ainda não realizado. | `README.md`, `docs/**`, `CONTRIBUTING.md` | Iniciar descoberta controlada do Ranger. |
| 2026-09-01 | 2 | Inventário visual da unidade concluído, sem mudar configurações. | Ranger 2(N), Android 13, build `T2351_MOVFAST_20260204`, Barcode Utility 1.3.62.1.4 e serviço 2.0.8.1211. | Falta Scan Demo, configuração de saída e ADB. | `collector/ranger-2n.md`, `collector/SESSAO_DESCOBERTA_2026-09-01.md`, este plano. | Validar leitura no Scan Demo. |
| 2026-09-01 | 2 | Scan Demo e Function settings inspecionados sem alterações. | Leitura de Code 128, EAN-13 e QR; modo `broadcast/focus`, UTF-8, Single Scan e MultiBarcodes 1. | White list está ativa; ação/chave de broadcast e ADB pendentes. | `collector/SESSAO_DESCOBERTA_2026-09-01.md`, `scanner/integracao-xcscanner.md`, este plano. | Inspecionar White list e valores de broadcast. |
| 2026-09-01 | 2 | Contrato de broadcast registrado sem salvar alterações no aparelho. | Ação `android.intent.scanResult`, chave `scanKey`, permissão `android.permission.CAMERA`, destino `com.android.scantest` / `ScanTestActivity`; White list é apenas chave nesta tela. | Validar por ADB a identidade/entrega e criar receiver próprio antes de trocar o destino. | `collector/SESSAO_DESCOBERTA_2026-09-01.md`, `scanner/integracao-xcscanner.md`, este plano. | Habilitar ADB e instalar app de diagnóstico. |
| 2026-09-01 | 2 | ADB autorizado e inventário técnico complementar lido. | Ranger visível por USB; API 33, `arm64-v8a`; `com.android.scantest` não está instalado; serviços `datawedge` 1.2.9 e `scanner4710` 1.1.0 presentes. | Falta aplicativo próprio e prova de entrega do bip. | `collector/SESSAO_DESCOBERTA_2026-09-01.md`, `scanner/integracao-xcscanner.md`, este plano. | Criar e instalar app de diagnóstico. |
| 2026-09-01 | 3 | App de diagnóstico criado e APK de debug compilado localmente. | Application ID `br.com.elatech.checkoutlab`; receiver `ScanResultReceiver`; build `assembleDebug` concluído. | Instalação no Ranger e alteração autorizada do destino no Barcode Utility. | `apps/handheld-checkout/**`, `development/conectar-e-instalar.md`, este plano. | Autorizar instalação do APK. |
| 2026-09-01 | 3 | APK de diagnóstico instalado e aberto via ADB. | Instalação retornou `Success`; pacote `br.com.elatech.checkoutlab` presente e atividade inicial iniciada. | Usuário deve conceder permissão exibida pelo app; depois, alterar com autorização o destino do Barcode Utility e testar o bip. | `collector/SESSAO_DESCOBERTA_2026-09-01.md`, `development/conectar-e-instalar.md`, este plano. | Conceder permissão no app. |
| 2026-09-01 | 3 | Handoff portátil para Claude Code criado. | Contexto, decisões, comandos validados e próxima ação documentados; não depende da conversa. | Permissão no app e prova de broadcast ainda pendentes. | `handoff/CLAUDE_CODE_2026-09-01.md`, `README.md`, este plano. | Abrir nova sessão e seguir o handoff. |
| 2026-09-01 | 3 | Prova de broadcast executada por ADB dirigindo a UI do Barcode Utility (autorizada). Alteração reversível de PackageName/ClassName aplicada e verificada. | Permissão CAMERA `granted=true`. Scanner decodifica (EAN-13 `7899916918645`) e emite `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` (+`_INPUT`), **não** `android.intent.scanResult`. Os campos Action/Package/Class da seção *Settings broadcast options* não tiveram efeito observável. SDK `movfast` confirma broadcast configurável via `setScanResultBroadcast`, sem constante pública. | App não recebeu bip. Falta descobrir a chave do extra da ação real (sem chutar). Decidir reverter os campos do Barcode Utility. Validar efeito da White list. | `collector/SESSAO_DESCOBERTA_2026-09-01.md`, este plano. | Autorizar diagnóstico v0.2.0 que loga todos os extras da ação real. |
| 2026-09-01 | 3 | Diagnóstico evoluído v0.2.0→v0.3.0→v0.4.0. Contrato de broadcast **confirmado** no aparelho. | Manifest receiver é barrado no Android 13 (`Background execution not allowed`); **runtime receiver** (`RECEIVER_EXPORTED`) entrega. Ação `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST`, código em `EXTRA_BARCODE_DECODING_DATA`, simbologia em `EXTRA_BARCODE_DECODING_SYMBOLE` (`EAN-13`, `QRCODE`), tempos `TIMESTAMP_START/END`. `_INPUT` é caminho paralelo (ignorado). Sem permissão exigida no receiver. Amostras: EAN-13 `7896445490550`, QR instagram. | Reverter os 2 campos do Barcode Utility (sem impacto funcional; tela ficou inacessível). Decidir sobre suprimir o caminho `_INPUT`. Decidir SDK vs broadcast. `prefs` exportado guardado no scratchpad (não commitado). | `apps/handheld-checkout/**`, `scanner/integracao-xcscanner.md`, `collector/SESSAO_DESCOBERTA_2026-09-01.md`, este plano. | Fechar Fase 3 e iniciar Fase 4 (catálogo/carrinho offline). |
| 2026-09-01 | 3 | Um bip = um evento confirmado (v0.4.0). Caminho morto (`android.intent.scanResult`/`scanKey`) removido do código: `ScanResultReceiver` e o `<receiver>` do manifesto apagados; `ScannerContract` só com a ação real (v0.4.1). Decisão de arquitetura registrada em **ADR 0002**. | Broadcast em runtime atende à Fase 4. Decisão: broadcast agora atrás de uma costura; **XCScanner SDK no produto** (config reproduzível, controle de gatilho/modos, desliga saída teclado). Campos do Barcode Utility: ficam como estão (inertes), sem reversão — sem impacto. | Caminho `_INPUT` ainda injeta texto em campo com foco → tratar na Fase 4 (sem EditText focado na tela de bipagem) ou mudar saída p/ broadcast puro com autorização. | `apps/handheld-checkout/**`, `docs/adr/0002-*`, `scanner/integracao-xcscanner.md`, `apps/handheld-checkout/README.md`, este plano. | Iniciar Fase 3.5 (costura `ScannerSource`) e Fase 4 (catálogo/carrinho). |
| 2026-09-01 | 3.5/4 | Costura `ScannerSource` + `BroadcastScannerSource` (v0.5.0). Domínio da Fase 4: `Money` (centavos), `Product`, `Cart`, `Catalog`, `Sale`, `CheckoutController` (debounce 400ms). Tela de checkout (`MainActivity`) + diagnóstico movido p/ `DiagnosticActivity` (v0.6.0). 7 testes JVM verdes. Fluxo validado no aparelho por `am broadcast` simulando a ação real: bip conhecido soma correto (R$5,75 = 2,50+3,25), repetido incrementa (Café qtd 2 = R$37,80), desconhecido abre cadastro. | Deploy alvo = coletor dedicado (kiosk). `applyConfig` no broadcast é no-op (só o SDK aplica). `am broadcast` de teste exige receiver runtime + `RECEIVER_EXPORTED` (é o caso). Screenshot de finalizar venda ficou bloqueado por timeout de tela do aparelho; coberto por teste JVM. | `apps/handheld-checkout/**` (scanner/, domain/, checkout/, layouts, testes), este plano. | Room (persistir catálogo/vendas) + trazer SDK sob autorização. |
| 2026-09-01 | 3.5 | Usuário autorizou o SDK. aar `xcscanner_qrcode_v1.3.56.1.14-release.aar` vendorada em `app/libs/` (commit `2f813e4` do ramo `movfast`, SHA-256 `ae1aba41…`, Apache-2.0, procedência em `PROVENANCE.md`). `SdkScannerSource` implementa `ScannerSource` via `com.xcheng.scanner.XcBarcodeScanner`: `init`/`deInit`, callback `ScannerSymResult`, `applyConfig` mapeando `ScannerConfig` p/ `setOutputMethod`/`setSuccessNotification`/`setScanVolume`/`setScanMode`/`setTextSuffix`/`enableBarcodeType`/`saveSettings`. Toggle Broadcast↔SDK na tela de diagnóstico (v0.7.0). | No aparelho: `init` OK, `getSdkVersion=1.3.56.1.14`, `applyConfig ok` e o serviço `XCScanner` respondeu (`configDecoderTag`). `getServiceVersion()` veio vazio logo após `init` (timing — reconsultar). Build + 7 testes verdes. | Falta bip físico p/ provar `onResult`. Checkout ainda usa Broadcast. Timeout de tela do coletor atrapalha screenshots. | `apps/handheld-checkout/app/libs/**`, `scanner/SdkScannerSource.kt`, `DiagnosticActivity`, `build.gradle.kts`, `docs/adr/0002-*`, este plano. | Provar entrega SDK por bip; migrar checkout p/ SDK; Room. |
| 2026-09-01 | 3.5 | Entrega do SDK provada no aparelho (via toggle na tela de diagnóstico): `onResult sym=EAN-13` código `7896445490550`. `getServiceVersion` reconsultado = `1.3.62.1.4`; `sdk=1.3.56.1.14` → `match=true`. Checkout (`MainActivity`) migrado p/ `SdkScannerSource`; `CheckoutController.attach` aplica `CHECKOUT_DEFAULT` (saída `BROADCAST_ONLY` → mata o `_INPUT`). v0.8.0. Build + 7 testes verdes. | `svc power stayon true` usado p/ contornar o timeout de tela do coletor durante o teste (reverter). Fase 3.5 concluída. | Reverter `stayon`. Migrar `Catalog`/`SaleHistory` p/ Room. Persistir `ScannerConfig` + tela de ajustes. | `MainActivity`, `CheckoutController`, `SdkScannerSource`, `build.gradle.kts`, `docs/adr/0002-*`, este plano. | Fase 4 — Room. |

## Regras de avanço

- Não pular uma fase apenas porque o próximo passo parece simples.
- Toda descoberta no aparelho deve ter data, versão e evidência, e deve ser marcada como fato ou hipótese.
- Nenhuma integração com sistemas reais é implícita; requer nova autorização e ADR.
- O repositório, e não o histórico desta conversa, é a memória operacional do projeto.

## O que destrava AGORA

- **Fase 3.5 — costura `ScannerSource`**: definir a interface e a implementação
  `BroadcastScannerSource` (a atual), para o domínio da Fase 4 não depender do
  mecanismo. Não precisa de autorização.
- **Fase 3.5 — SDK (requer autorização no momento de trazer o artefato)**: fixar
  release/commit do `XCApex/XCScannerSDK@movfast`, registrar procedência +
  checksum, validar `getServiceVersion` contra `2.0.8.1211`. Ver ADR 0002.
- **Fase 3.5 — provar entrega SDK**: bipar com a tela de diagnóstico em modo
  SDK e confirmar `onResult`. Depois, migrar `MainActivity` (checkout) de
  `BroadcastScannerSource` para `SdkScannerSource` e aplicar
  `ScannerConfig.CHECKOUT_DEFAULT` (saída broadcast puro → mata o `_INPUT`).
- **Fase 4 — Room**: trocar `InMemoryCatalog` e `InMemorySaleHistory` por
  implementações Room (KSP), mantendo as interfaces `Catalog`/`SaleHistory`.
  Seed fictício na primeira execução.
- **Fase 4 — tela de ajustes do scanner** (`ScannerConfig`): agora tem efeito
  via `SdkScannerSource`; persistir a escolha e aplicar no `start`.

### Encerrado / não-fazer

- Reversão dos campos `Broadcast Receiver PackageName/ClassName` do Barcode
  Utility: **não será feita** — são inertes nesta firmware, sem impacto. Estado
  documentado em `scanner/integracao-xcscanner.md`.
- Caminho `android.intent.scanResult` / `scanKey`: morto nesta firmware, removido
  do código, não reintroduzir.
