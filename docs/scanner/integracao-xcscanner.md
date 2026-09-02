# Integração do scanner: Barcode Utility e XCScanner SDK

## Conclusão inicial

O resultado da pesquisa é legítimo e útil: o [guia MERTECH MovFast](https://docs.mertech.ru/tsd/movfast/XCScanner_SDK.html) aponta para o [repositório público XCApex/XCScannerSDK, ramificação `movfast`](https://github.com/XCApex/XCScannerSDK/tree/movfast). Ele descreve a classe `XcBarcodeScanner`, callback de resultado, controle de leitura, configuração de simbologias, broadcast e exportação de configurações.

Isso é evidência de que há um SDK real. Não é, porém, garantia de compatibilidade automática com qualquer R2N: a unidade de teste precisa informar sua versão de serviço/SDK antes de adotarmos a dependência.

## Caminhos de integração

| Caminho | Uso | Vantagem | Risco/cuidado |
| --- | --- | --- | --- |
| Broadcast Android | primeiro teste e fallback portátil | documentado pela MovFast; não requer biblioteca | ação/chave e permissões devem ser conferidas no aparelho |
| XCScanner SDK | quando o app precisar controlar scanner e receber callback direto | controla gatilho, modos e configurações por API | acoplamento a versão/serviço do fabricante |
| Keyboard wedge | contingência | tende a funcionar com qualquer app | caracteres chegam ao foco atual; não é apropriado como integração principal |

## Broadcast documentado pela MovFast

O Barcode Utility permite configurar a saída como broadcast. Seus valores padrão documentados são:

```text
Ação: com.xcheng.scanner.action.BARCODE_DATA_DECODING_BROADCAST
Chave: EXTRA_BARCODE_DECODING_DATA
```

Esses valores são **ponto de partida**, não constantes a codificar sem teste. O aplicativo deve concentrá-los em uma configuração de integração e registrá-los em `device-notes` depois da prova no aparelho. O sufixo Enter vem habilitado por padrão na documentação; para broadcast do checkout, a intenção é evitar que ele também seja injetado como teclado.

Fonte: [manual Barcode Utility, seções Function Settings e Settings broadcast options](https://movfast.com.br/hubfs/Manual%20Barcode%20Utility%20Rev.01.pdf?hsLang=pt-br).

## Configuração confirmada em R2N-LAB-01

Em 2026-09-01, a unidade de laboratório confirmou leitura de Code 128, EAN-13 e QR Code no Scan Demo. A configuração observada, sem alteração, foi:

| Campo | Valor observado | Implicação |
| --- | --- | --- |
| Scan mode | `Single Scan` | adequado ao checkout; uma tentativa por acionamento |
| Barcode data output mode | `Output to broadcast/focus` | priorizar receptor de broadcast no app de diagnóstico |
| Charset | UTF-8 | preservar como padrão do adaptador |
| Prefixos | vazios | o código chegará sem prefixo configurado |
| Suffix 1 | `ENTER` | observar se o foco também recebe Enter; não depender dele no broadcast |
| MultiBarcodes | 1 | adequado; cada bip representa uma leitura |
| White list | habilitada; tocar no item não abre lista | nesta versão é uma chave; efeito funcional ainda precisa de prova |
| Pass Scan Key Value | desabilitado | o app não deve depender de evento da tecla física |
| Scan Result Action | `android.intent.scanResult` | filtro de intent a usar no receptor de diagnóstico |
| Scan Result Data Key | `scanKey` | extra que contém o código lido, a validar em execução |
| Scan Result Permission | `android.permission.CAMERA` | validar se o manifesto do app precisa declarar essa permissão para receber o evento |
| Broadcast Receiver PackageName | `com.android.scantest` | pacote não instalado, confirmado por ADB; destino atual é inativo |
| Broadcast Receiver ClassName | `ScanTestActivity` | classe sem pacote correspondente; trocar somente após existir receiver próprio e haver autorização |

Os valores acima foram lidos diretamente da interface em 2026-09-01, sem serem alterados. Eles substituem, para esta unidade, os valores genéricos do manual. ADB confirmou que `com.android.scantest` não está instalado, logo o destino atual é inativo. Não reutilizar esse identificador: o app de diagnóstico terá pacote e receiver próprios. Depois de instalado, a troca do destino será uma alteração controlada, registrada e feita somente com autorização.

## Contrato de broadcast CONFIRMADO no R2N-LAB-01 — 2026-09-01

Prova concluída com o app `br.com.elatech.checkoutlab` v0.3.0/v0.4.0. Leu EAN-13 e QR Code, um evento por bip na ação principal.

| Item | Valor confirmado |
| --- | --- |
| Ação principal | `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` |
| Extra do código | `EXTRA_BARCODE_DECODING_DATA` (String) |
| Extra da simbologia | `EXTRA_BARCODE_DECODING_SYMBOLE` (String) — ex.: `EAN-13`, `QRCODE` |
| Extras de tempo | `TIMESTAMP_START`, `TIMESTAMP_END` (Long) |
| Ação paralela (teclado/foco) | `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST_INPUT` — mesmo `EXTRA_BARCODE_DECODING_DATA` + `EXTRA_BARCODE_CLEAN` (Boolean). Duplicaria o evento; o app ignora. |
| Entrega | Ação **implícita**. No Android 13 exige `Context.registerReceiver(receiver, filter, RECEIVER_EXPORTED)` na Activity/Service. Receiver de manifesto é bloqueado: `W/BroadcastQueue: Background execution not allowed`. |
| Permissão | Não foi exigida permissão no receiver para esta ação. `pref_scan_result_permission = android.permission.CAMERA` só se aplica ao caminho configurável `android.intent.scanResult`, que **não é emitido** por este firmware. |

Amostras lidas: EAN-13 `7896445490550`; QR `https://www.instagram.com/indaiaoficial/`.

### Sobre os campos "Settings broadcast options" da UI

Os campos `Scan Result Action` (`android.intent.scanResult`), `Scan Result Data Key` (`scanKey`) e `Broadcast Receiver PackageName/ClassName` da tela Function settings **não tiveram efeito observável** nesta firmware: nenhuma linha `Sending ... android.intent.scanResult` no `logcat`, e o app com filtro para essa ação nada recebeu. Em 2026-09-01, sob autorização, os campos PackageName/ClassName foram trocados para o app de diagnóstico e depois a tela ficou inacessível (o "Function settings" passou a renderizar a variante *Directional output*). O `prefs` exportado guarda o estado (`pref_broadcast_receiver_pkg`/`_cls` com o valor novo). Reversão desses dois campos: pendente e sem impacto funcional. Valores originais: `com.android.scantest` / `ScanTestActivity`.

## Uso planejado do SDK

Não baixar nem embutir o SDK ainda. Na prova de leitura, consultar no aparelho a versão do serviço e, se necessário, comparar com a documentação. O guia descreve:

- `XcBarcodeScanner.init(...)` para receber callback de resultado;
- `startScan()` e `stopScan()` para controlar o serviço;
- `getServiceVersion()` e `getSdkVersion()` para diagnosticar compatibilidade;
- `setScanResultBroadcast(action, resultKey)` para definir broadcast;
- exportação/importação de configurações XML.

Quando o SDK for adotado, fixar o commit/release exato, registrar procedência e checksum do artefato. Não usar link de download de terceiros ou arquivo recebido fora da origem documentada sem revisão.

## Teste mínimo planejado

1. Abrir Barcode Utility e usar **Scan Demo** com um EAN-13 e um QR Code conhecidos.
2. Registrar versão do Barcode Utility, serviço e firmware.
3. Preservar a configuração atual até haver aplicativo próprio instalado; não modificar a saída apenas para testar menus.
4. Executar um app de diagnóstico com pacote/receiver próprios, que registra somente o conteúdo recebido, tipo de código e horário.
5. Confirmar uma entrega por bip; bloquear/desbloquear e reiniciar o coletor; repetir.
6. Exportar a configuração, revisá-la e guardar em local privado caso contenha dados do ambiente.

## Critérios para rejeitar a hipótese

Interromper a adoção do SDK e pedir suporte da MovFast se: o serviço não responder, a versão divergir de modo incompatível, houver licença inválida, o broadcast for inconsistente ou o leitor duplicar/perder eventos.
