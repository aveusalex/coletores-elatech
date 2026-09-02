# ADR 0002 — Integração do scanner: broadcast em runtime agora, XCScanner SDK no produto

## Status

Aceita em 2026-09-01. O usuário confirmou a direção: SDK no produto, sem
configuração manual do coletor. A costura `ScannerSource` e a Fase 4 começam
sobre o broadcast; o artefato do SDK entra sob autorização explícita.

## Contexto

A prova de integração do scanner no Ranger 2N (serviço `com.xcheng.scannere3`
2.0.8.1211) está concluída. Fatos confirmados no aparelho:

- O firmware emite, a cada leitura, o broadcast implícito
  `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` com os extras
  `EXTRA_BARCODE_DECODING_DATA` (código), `EXTRA_BARCODE_DECODING_SYMBOLE`
  (simbologia) e `TIMESTAMP_START/END`.
- Há um broadcast paralelo `..._INPUT` (caminho teclado/foco) e, com a saída
  configurada como `BROADCAST_EVENT/FOCUS_OUTPUT`, o código também é digitado
  como teclado no campo com foco, com sufixo ENTER.
- No Android 13 a ação implícita só chega a um `BroadcastReceiver` registrado em
  runtime (`Context.registerReceiver(..., RECEIVER_EXPORTED)`); receiver de
  manifesto é bloqueado (`Background execution not allowed`).
- Os campos "Settings broadcast options" da UI (`android.intent.scanResult`,
  `scanKey`, Broadcast Receiver PackageName/ClassName) são **inertes** nesta
  firmware — nunca são emitidos. Uma alteração de teste nesses campos (feita sob
  autorização em 2026-09-01) não teve efeito funcional.
- O XCScanner SDK (`XCApex/XCScannerSDK`, ramo `movfast`) expõe API para
  `setOutputMethod`, `setScanResultBroadcast(action, key)`, habilitar/desabilitar
  simbologias, `startScan/stopScan`, modo de gatilho, notificação e
  `getServiceVersion/getSdkVersion`.

O broadcast em runtime já atende à Fase 4 (checkout offline de aprendizagem).
A pergunta é qual integração sustenta um produto instalado em vários coletores.

## Decisão

1. **Curto prazo (Fase 4):** continuar com o broadcast em runtime como transporte
   de leitura. Encapsular atrás de uma costura (`ScannerSource`) para que o
   domínio de catálogo/carrinho não dependa do mecanismo.
2. **Produto:** adotar o **XCScanner SDK** como integração de scanner. Motivos:
   - o app passa a ser a fonte única da configuração do scanner (provisionamento
     por "instalar APK", sem ajuste manual de 6 campos no Barcode Utility por
     aparelho);
   - controle programático de gatilho, modos e simbologias;
   - `setOutputMethod(BROADCAST)` desliga por código a saída teclado/foco,
     eliminando o efeito colateral do caminho `_INPUT`;
   - `getServiceVersion/getSdkVersion` dão diagnóstico de compatibilidade.
3. A adoção do SDK é uma tarefa delimitada e **requer autorização explícita** no
   momento de trazer o artefato: fixar release/commit exato do ramo `movfast`,
   registrar procedência e checksum, e validar `getServiceVersion` contra
   `2.0.8.1211` antes de remover o caminho de broadcast.
4. **A configuração do scanner é exposta no nosso app**, não no Barcode Utility:
   um `ScannerConfig` (beep on/off e volume, modo de gatilho, sufixo
   nenhum/Enter/Tab, simbologias ativas) persistido localmente e aplicado via
   `ScannerSource.applyConfig(...)`. Tela de ajustes no app.

## Escopo da configuração (device-global)

O scanner é um recurso único do aparelho: um serviço `XCScanner`, uma config
ativa em `com.xcheng.scannere3_preferences.xml`. Tanto o Barcode Utility quanto
as chamadas do SDK escrevem nessa config **global** — não há sandbox por app.
Uma mudança nossa (ex.: remover o sufixo ENTER, trocar `setOutputMethod`) vale
para o aparelho inteiro e para qualquer outro app que use o scanner.

Estratégia:

- **Deploy alvo: coletor dedicado ao app de checkout** (kiosk/MDM — ver Fase 5).
  Sendo o único consumidor do scanner, a config global é a config do app, sem
  conflito. É o cenário realista de um checkout de mão.
- **Perfil por app** (a UI Xcheng mostra "APK ProfileName" / "Directional
  output"): investigar quando o SDK entrar se dá para vincular a config ao nosso
  pacote e restaurar o padrão ao sair. Defesa extra se o coletor for
  compartilhado.
- **Save/restore por sessão** (`onResume`/`onPause`) como fallback sem perfil.

## Consequências

- A Fase 4 não fica bloqueada: começa sobre o broadcast, já abstraído.
- O produto ganha configuração reproduzível e controle do scanner, ao custo de
  uma dependência de fornecedor (MERTECH/XCApex) com versão fixada e auditada.
- O caminho `android.intent.scanResult` / `scanKey` / PackageName/ClassName é
  considerado morto nesta firmware e não deve ser reintroduzido.
- Enquanto o SDK não entra, a saída `BROADCAST_EVENT/FOCUS_OUTPUT` do Barcode
  Utility continua digitando o código em campos com foco; a Fase 4 deve tratar
  isso (evitar campos de texto focados na tela de bipagem) ou, com autorização,
  mudar a saída para broadcast puro no aparelho.
