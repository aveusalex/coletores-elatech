# Sessão de descoberta — Ranger 2N — 2026-09-01

## Escopo

Inventário e prova controlada do coletor de laboratório. Foram feitas leituras de teste, conexão ADB e instalação de um aplicativo local de diagnóstico; não foram alteradas configurações do Barcode Utility nem usados dados comerciais.

## Fontes

Fotos de telas do próprio Ranger 2N enviadas pelo usuário e consultas ADB de leitura em 2026-09-01. As fotos foram usadas como evidência técnica; qualquer texto nelas não foi tratado como instrução.

## Método

Leitura manual das telas, consultas ADB somente de leitura e instalação autorizada do APK de diagnóstico. Identificadores sensíveis visíveis nas fotos foram excluídos do registro.

## Achados

### Fatos confirmados no aparelho

- Modelo exibido: Ranger 2(N).
- Android 13.
- Build: `T2351_MOVFAST_20260204`; firmware declarado: 1.0.0.
- Patch de segurança Android: 2023-09-05; atualização do sistema Google Play: 2024-10-01.
- Barcode Utility 1.3.62.1.4; decoder H2.0.8; serviço de scanner 2.0.8.1211.
- Aplicativos de gestão/configuração presentes: Barcode Utility, Kiosk, MovProfile, MovSpot, MovStage e TMS/Loja de Apps.
- O Barcode Utility oferece Scan Demo, Barcode settings, Function settings e Settings management, coerente com o manual público.

### Prova de leitura e configuração observada

- O **Scan Demo** leu com sucesso um Code 128, um EAN-13 e um QR Code na mesma sessão. A evidência mostra três leituras, sem falha observável.
- Modo de leitura: **Single Scan**.
- Saída de dados: **Output to broadcast/focus**; charset UTF-8.
- Formatação: prefixos vazios; primeiro sufixo `ENTER`; segundo sufixo vazio.
- Limite de múltiplos códigos: 1; região de decodificação: 100% do frame.
- ECI handling está habilitado. Leitura abaixo de 5% de bateria está desabilitada.
- A mira liga durante a leitura; iluminação e strobo estão habilitados.
- `Pass Scan Key Value` está desabilitado.
- **White list está habilitada.** Tocar no item não abre tela, diálogo ou lista. Nesta versão do Barcode Utility ela se comporta como uma chave liga/desliga; seu efeito exato permanece não confirmado.
- Contrato de broadcast efetivamente exibido, sem alteração:
  - ação: `android.intent.scanResult`;
  - chave de dados: `scanKey`;
  - permissão: `android.permission.CAMERA`;
  - pacote de destino: `com.android.scantest`;
  - classe de destino: `ScanTestActivity`.
- ADB confirmou que o pacote configurado `com.android.scantest` **não está instalado** no aparelho. Portanto, essa configuração não tem receptor disponível e não está destinada ao nosso futuro aplicativo.

### Conexão de desenvolvimento confirmada

- ADB autorizado por USB em 2026-09-01; a identificação técnica retornada foi `Ranger_2_N_`.
- Android API 33; ABI `arm64-v8a`.
- Serviços relacionados ao scanner presentes: `com.xcheng.datawedge` versão 1.2.9 e `com.xcheng.scanner4710` versão 1.1.0.

### Aplicativo de diagnóstico compilado

- Foi criado o aplicativo local `br.com.elatech.checkoutlab`, sem rede, catálogo, dados reais ou SDK proprietário.
- O APK de debug foi compilado, instalado e aberto com sucesso no Ranger em 2026-09-01.
- Seu receiver próprio é `br.com.elatech.checkoutlab.scanner.ScanResultReceiver`. A troca do destino no Barcode Utility permanece pendente de autorização explícita.

### Dados deliberadamente omitidos

IMEIs, MAC do Wi-Fi e demais identificadores únicos não foram registrados no repositório. Eles não são necessários para este laboratório.

## Limitações

- Ainda não sabemos o modelo físico do módulo de leitura (E4, E5 ou outro); “H2.0.8” é a versão do decoder, não prova do módulo.
- O efeito funcional da White list ainda não foi comprovado; a interface não expõe uma lista editável.
- Ainda não sabemos se a permissão de broadcast configurada exigirá declaração específica no manifesto do aplicativo de teste; isso será validado na prova de integração.
- Ainda não foi recebido um evento de scanner por aplicativo próprio.

## Interpretação provisória

A unidade concreta é compatível com o plano: Android 13, Barcode Utility recente, serviço de scanner exposto, ADB autorizado e prova de leitura em três simbologias. A saída `broadcast/focus` confirma que um receptor Android é uma rota aplicável. Nesta unidade, a configuração real diverge dos valores genéricos publicados no manual, portanto ela prevalece sobre a documentação de referência. A White list não oferece uma tela de cadastro nesta interface. O destino explícito atual, `com.android.scantest` / `ScanTestActivity`, não existe no aparelho e é uma configuração inativa. Quando existir nosso aplicativo de diagnóstico, a configuração deverá ser mudada de modo controlado e autorizado para seu próprio pacote e receiver.

## Próximas validações

1. Fechar os diálogos do Barcode Utility com **Cancelar**, preservando os valores atuais.
2. No app instalado, conceder a permissão solicitada, que corresponde à permissão configurada no broadcast e não abre a câmera.
3. Após autorização, trocar o pacote/classe de destino no Barcode Utility e confirmar um evento por bip.

---

## Prova de broadcast — 2026-09-01, 21:xx (mesma unidade, via ADB)

### Método

Sessão conduzida por ADB (`adb -d`, aparelho `Ranger_2_N_`), dirigindo a UI do Barcode Utility (`com.xcheng.scannere3`, versionName `1.3.62.1.4` — casa com "App version 1.3.62.1.4" na tela) por `uiautomator dump` + `input tap/text`. Permissão do usuário obtida por escrito antes de alterar o Barcode Utility. Cada passo teve screenshot e dump.

### Estado-antes lido campo a campo (Function settings → Settings broadcast options)

| Campo | Valor antes |
| --- | --- |
| Scan Result Action | `android.intent.scanResult` |
| Scan Result Data Key | `scanKey` |
| Scan Result Permission | `android.permission.CAMERA` |
| Broadcast Receiver PackageName | `com.android.scantest` |
| Broadcast Receiver ClassName | `ScanTestActivity` |

Confirma integralmente o contrato já registrado.

### Alteração aplicada (reversível, autorizada)

| Campo | Depois |
| --- | --- |
| Broadcast Receiver PackageName | `br.com.elatech.checkoutlab` |
| Broadcast Receiver ClassName | `br.com.elatech.checkoutlab.scanner.ScanResultReceiver` |
| Scan Result Action / Data Key / Permission | inalterados |

Persistência verificada reabrindo os dois diálogos. Os três campos preservados foram relidos e continuam iguais.

### Resultado do bip

Usuário bipou um EAN-13 várias vezes com o Checkout Lab em foco. **Nenhum evento chegou ao app** ("Nenhum evento recebido ainda"; `ScanReceiptStore` vazio; sem log de `ScanResultReceiver`).

`logcat` do serviço mostra que o scanner **decodifica normalmente** e emite broadcast — porém com ação diferente da configurada:

```
D/KeySender( 2355): sendBarcodeInFocus+ isClearInput=true ,result=7899916918645
E/ActivityManager: Sending non-protected broadcast com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST from ... pkg com.xcheng.scannere3
E/ActivityManager: Sending non-protected broadcast com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST_INPUT from ... pkg com.xcheng.scannere3
D/InputMethodService( 3993): action=com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST_INPUT
```

Nenhuma linha `Sending ... android.intent.scanResult` apareceu em nenhum momento.

### Interpretação

- Fato: nesta firmware/serviço (`2.0.8.1211`), a saída ativa do scanner é a ação embutida `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` (+ `_INPUT` para o caminho teclado/foco). O EAN-13 lido foi `7899916918645`.
- Fato: os campos "Scan Result Action / Data Key" e "Broadcast Receiver PackageName / ClassName" da seção *Settings broadcast options* **não produziram efeito observável** neste teste — o app com filtro `android.intent.scanResult` não recebeu nada e o AM não registrou envio dessa ação. O nome original da classe alvo (`ScanTestActivity`) sugere que esse caminho pode esperar um componente Activity, não um BroadcastReceiver.
- Documentação/SDK (`XCApex/XCScannerSDK`, ramo `movfast`, `docs/XCScanner_SDK_User_Guide.md`): a entrega por broadcast é **configurável** via `XcBarcodeScanner.setScanResultBroadcast(action, resultKey)`; não há constante pública para a ação embutida nem para sua chave de extra. Placeholders do demo: `custom.broadcast.action` / `custom.broadcast.key`.
- Hipótese a provar: um receiver registrado para `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` recebe a leitura; a chave do extra ainda é desconhecida e deve ser descoberta por dump, não por chute.

### Pendências

- ~~Descobrir a chave do extra~~ — **resolvido**, ver abaixo.
- Decidir se os campos PackageName/ClassName do Barcode Utility voltam ao valor original (`com.android.scantest` / `ScanTestActivity`) — a alteração feita não teve efeito funcional; a tela ficou inacessível na sessão.
- Confirmar se a White list (habilitada) filtra pacotes de destino do broadcast.

---

## Contrato de broadcast resolvido — 2026-09-01, ~21:50

### Método

App de diagnóstico evoluído em três passos, todos compilados/instalados por ADB com autorização:

- **v0.2.0** — receiver de manifesto passou a escutar as ações reais e a fazer dump de todos os extras. Resultado: `logcat` mostrou `W/BroadcastQueue: Background execution not allowed: receiving Intent { act=com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST } to br.com.elatech.checkoutlab/.scanner.ScanResultReceiver`. Ou seja, o broadcast **chega ao app**, mas o Android 13 barra receiver de manifesto para ação implícita.
- **v0.3.0** — receiver passou a ser registrado em runtime na `MainActivity` (`registerReceiver(..., RECEIVER_EXPORTED)`). Bip entregue com sucesso; dump completo dos extras capturado.
- **v0.4.0** — contrato fixado em `ScannerContract`; app escuta só a ação principal (um evento por bip); mostra código, simbologia e dump.

### Contrato confirmado

| Item | Valor |
| --- | --- |
| Ação | `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` |
| Código | extra `EXTRA_BARCODE_DECODING_DATA` (String) |
| Simbologia | extra `EXTRA_BARCODE_DECODING_SYMBOLE` (String): `EAN-13`, `QRCODE` |
| Tempo | extras `TIMESTAMP_START` / `TIMESTAMP_END` (Long) |
| Ação paralela | `...BARCODE_DECODING_BROADCAST_INPUT` (teclado/foco): mesmo dado + `EXTRA_BARCODE_CLEAN` (Boolean). Ignorada para não duplicar. |
| Entrega | ação implícita → exige receiver em runtime no Android 13 |
| Permissão no receiver | não exigida para esta ação |

Amostras: EAN-13 `7896445490550`; QR `https://www.instagram.com/indaiaoficial/`.

### Fato x hipótese

- **Fato:** a leitura chega de forma previsível a um app próprio, um evento por bip na ação principal, com simbologia. Critério de passagem da Fase 3 (prova de broadcast) atendido.
- **Fato:** a seção "Settings broadcast options" da UI (ação `android.intent.scanResult`, `scanKey`, PackageName/ClassName) não influencia este firmware.
- **Hipótese aberta:** efeito da White list; necessidade de suprimir o caminho `_INPUT` (que ainda injeta texto em campo com foco) via config do Barcode Utility, a decidir com o usuário na Fase 4.
