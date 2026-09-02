# Handheld Checkout Lab — v1.0.0

Aplicativo Android nativo para o MovFast Ranger 2N. **Offline**: sem rede, ERP,
pagamento ou dados reais. Notas de versão: `docs/RELEASE-1.0.md`.

## Telas

- `MainActivity` — checkout: bipe adiciona/incrementa, código desconhecido abre
  cadastro de produto fictício, "Finalizar" grava venda simulada e limpa.
  Debounce de 400 ms para leitura repetida.
- `ScannerSettingsActivity` — edita e persiste `ScannerConfig` (beep, volume,
  gatilho, sufixo, saída, simbologias); aplica no serviço via SDK.
- `SalesHistoryActivity` — lista as vendas gravadas (somente leitura).
- `DiagnosticActivity` — leitura + dump de extras; alterna Broadcast/SDK em execução.

Todas em retrato fixo.

## Camadas

- `domain/` — `Money` (centavos), `Product`, `Cart`, `Catalog`/`SaleHistory`
  (interfaces), `CompletedSale`, `CatalogSeed` (massa fictícia).
- `checkout/CheckoutController` — liga `ScannerSource` → `Catalog` → `Cart` → `SaleHistory`.
- `data/` — Room: `AppDatabase` (`products`/`sales`/`sale_lines`), `RoomCatalog`,
  `RoomSaleHistory`. Seed no 1º create. `allowMainThreadQueries` (tradeoff de lab).
- `scanner/` — `ScannerSource` (costura) com `BroadcastScannerSource` e
  `SdkScannerSource`; `ScannerConfig` + `ScannerConfigStore` (SharedPreferences).

## Scanner — contrato confirmado no aparelho

| Campo | Valor |
| --- | --- |
| Ação (broadcast) | `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST` |
| Código | extra `EXTRA_BARCODE_DECODING_DATA` |
| Simbologia | extra `EXTRA_BARCODE_DECODING_SYMBOLE` (`EAN-13`, `QRCODE`, ...) |
| Entrega (broadcast) | receiver **em runtime**, `RECEIVER_EXPORTED` — manifesto é barrado no Android 13 |
| Fonte do checkout | `SdkScannerSource` (XCScanner SDK, aar em `app/libs/` — ver `PROVENANCE.md` e `docs/adr/0002-*`) |
| Compatibilidade | `sdk=1.3.56.1.14 service=1.3.62.1.4 match=true` |

O caminho `android.intent.scanResult` / `scanKey` / Broadcast Receiver
PackageName/ClassName do Barcode Utility é **inerte** nesta firmware —
`docs/scanner/integracao-xcscanner.md`.

`android.permission.CAMERA` continua declarada só porque é a permissão
configurada no coletor; o app não abre a câmera.

## Testes

```bash
./gradlew testDebugUnitTest   # 17 casos: Money, Cart, fluxo de checkout
```

Matriz de testes manuais no aparelho: `docs/testing/matriz-fase5.md`.

## Compilar e instalar

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=$HOME/Library/Android/sdk \
./gradlew assembleDebug
adb -d install -r app/build/outputs/apk/debug/app-debug.apk
adb -d shell am start -n br.com.elatech.checkoutlab/.MainActivity
```
