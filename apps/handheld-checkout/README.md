# Handheld Checkout Lab

Aplicativo Android nativo de diagnóstico para o Ranger 2N. Nesta primeira entrega ele não possui catálogo, carrinho, rede ou SDK proprietário: apenas recebe e exibe um resultado de leitura enviado pelo Barcode Utility.

## Contrato configurado no aparelho de laboratório

| Campo | Valor |
| --- | --- |
| Application ID | `br.com.elatech.checkoutlab` |
| Broadcast action | `android.intent.scanResult` |
| Extra do código | `scanKey` |
| Permissão observada | `android.permission.CAMERA` |
| Receiver | `br.com.elatech.checkoutlab.scanner.ScanResultReceiver` |

O app declara a permissão de câmera porque ela está configurada como permissão do broadcast no coletor. Ele não abre nem usa a câmera; a solicitação em tempo de execução existe apenas para provar se o serviço exige essa permissão para entregar o evento.

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

A troca do PackageName/ClassName no Barcode Utility só ocorre após a instalação e com autorização explícita do usuário.
