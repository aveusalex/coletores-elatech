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
| White list | habilitada | possível bloqueio ao novo app; inspecionar lista antes de implementar |
| Pass Scan Key Value | desabilitado | o app não deve depender de evento da tecla física |

Os campos de ação/chave de broadcast estavam acessíveis na interface, porém seus valores não foram exibidos nas fotos. Eles permanecem pendentes de leitura direta. Não substituir por valores publicados sem antes conferir a configuração real.

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
3. Configurar saída broadcast sem alterar outras configurações desnecessárias.
4. Executar um app de diagnóstico que registra somente o conteúdo recebido, tipo de código e horário.
5. Confirmar uma entrega por bip; bloquear/desbloquear e reiniciar o coletor; repetir.
6. Exportar a configuração, revisá-la e guardar em local privado caso contenha dados do ambiente.

## Critérios para rejeitar a hipótese

Interromper a adoção do SDK e pedir suporte da MovFast se: o serviço não responder, a versão divergir de modo incompatível, houver licença inválida, o broadcast for inconsistente ou o leitor duplicar/perder eventos.
