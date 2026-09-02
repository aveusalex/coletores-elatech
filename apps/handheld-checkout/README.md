# Handheld Checkout Lab

Aplicativo Android nativo de diagnóstico para o Ranger 2N. Nesta fase não possui catálogo, carrinho, rede ou SDK proprietário: apenas recebe e exibe um resultado de leitura do scanner, com dump completo dos extras.

## Contrato de scanner — CONFIRMADO no aparelho (2026-09-01)

| Campo | Valor |
| --- | --- |
| Application ID | `br.com.elatech.checkoutlab` |
| Broadcast action | `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` |
| Extra do código | `EXTRA_BARCODE_DECODING_DATA` (String) |
| Extra da simbologia | `EXTRA_BARCODE_DECODING_SYMBOLE` (String) — `EAN-13`, `QRCODE`, ... |
| Extras de tempo | `TIMESTAMP_START`, `TIMESTAMP_END` (Long) |
| Registro do receiver | **runtime** na `MainActivity`, `RECEIVER_EXPORTED` |

Notas:

- A ação é **implícita**. No Android 13 um receiver de manifesto é bloqueado
  (`W/BroadcastQueue: Background execution not allowed`); por isso o registro é em runtime.
- A ação paralela `..._INPUT` (caminho teclado/foco) carrega o mesmo dado e
  `EXTRA_BARCODE_CLEAN`; o app a **ignora** para ter um evento por bip.
- Os campos "Settings broadcast options" do Barcode Utility
  (`android.intent.scanResult` / `scanKey` / Broadcast Receiver PackageName/ClassName)
  **não influenciam** esta firmware — ver `docs/scanner/integracao-xcscanner.md`.
- A permissão `android.permission.CAMERA` continua declarada só porque é a permissão
  configurada no coletor; o app não abre a câmera.

## Limites deliberados

- sem internet, login, ERP, pagamento, dados comerciais ou produtos reais;
- sem alteração automática das configurações do Barcode Utility;
- sem XCScanner SDK nesta fase.

## Compilar e instalar

No Mac com o Ranger autorizado por USB:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ./gradlew assembleDebug
/Users/alexecheverria/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```
