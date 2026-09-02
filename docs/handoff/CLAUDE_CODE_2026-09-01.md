# Handoff para Claude Code — Checkout Lab MovFast Ranger 2N

## Como usar este documento

Abra uma nova sessão do Claude Code no repositório `aveusalex/coletores-elatech`, peça que ele leia este arquivo e continue a partir de **Próxima ação imediata**. Este handoff é a memória operacional do trabalho; não depende do histórico desta conversa.

## Objetivo delimitado

Construir, como aprendizagem, um checkout móvel **offline** no MovFast Ranger 2N: produtos fictícios, leitura por bip, carrinho, item inexistente e fechamento de venda simulado. O desenho deve manter uma fronteira clara para um middleware futuro, mas não deve integrar ERP, Clip Store, pagamentos, dados comerciais, nuvem ou serviços externos nesta etapa.

## Estado atual

### Fatos confirmados

- Coletor: MovFast Ranger 2(N), Android 13 / API 33, ABI `arm64-v8a`.
- Barcode Utility: versão 1.3.62.1.4; serviço do scanner: 2.0.8.1211.
- O leitor físico leu Code 128, EAN-13 e QR Code no Scan Demo.
- Configuração observada do scanner, **ainda não alterada**:
  - saída: `Output to broadcast/focus`;
  - ação: `android.intent.scanResult`;
  - chave do resultado: `scanKey`;
  - permissão: `android.permission.CAMERA`;
  - PackageName atual: `com.android.scantest`;
  - ClassName atual: `ScanTestActivity`.
- `com.android.scantest` não está instalado no Ranger; o destino atual do broadcast está inativo.
- ADB por USB está autorizado neste Mac. Use sempre `adb -d` para evitar atingir outro aparelho.
- Serviços de scanner identificados por ADB: `com.xcheng.datawedge` 1.2.9 e `com.xcheng.scanner4710` 1.1.0.
- O app de diagnóstico foi compilado, instalado e aberto com sucesso no Ranger:
  - Application ID: `br.com.elatech.checkoutlab`;
  - receiver: `br.com.elatech.checkoutlab.scanner.ScanResultReceiver`;
  - versão: `0.1.0-diagnostic`;
  - sem rede, SDK proprietário, catálogo, carrinho ou dados reais.

### Estado que depende do usuário agora

O app **Checkout Lab** está aberto no Ranger. O usuário precisa tocar em **Autorizar permissão do broadcast** e aceitar o aviso Android. A permissão aparece porque o Barcode Utility está configurado com `android.permission.CAMERA`; o app não abre nem usa a câmera.

### Hipóteses ainda em prova

- O Barcode Utility entregará o broadcast ao receiver próprio quando seu PackageName/ClassName forem atualizados.
- A permissão concedida ao app é necessária para essa entrega.
- A campo ClassName aceitará o nome de classe completo do receiver. Se a entrega falhar, investigar a expectativa exata do Barcode Utility antes de fazer outras mudanças.

### Limitações

- White list aparece habilitada, mas nesta versão é apenas uma chave; tocar nela não abre configuração. Seu efeito ainda não foi comprovado.
- Não foi recebido ainda nenhum bip no app próprio.
- Não foi adotado XCScanner SDK. Broadcast Android é a primeira rota; SDK só entra se a prova falhar ou houver necessidade de controlar o scanner.
- Não registrar serial completo, IMEI, MAC, credenciais ou dados comerciais.

## Próxima ação imediata

1. Confirmar com o usuário que a permissão foi concedida no Checkout Lab.
2. Pedir autorização explícita **antes** de alterar o Barcode Utility.
3. Com autorização, no Barcode Utility → Function settings → Settings broadcast options, alterar somente:

   ```text
   Broadcast Receiver PackageName: br.com.elatech.checkoutlab
   Broadcast Receiver ClassName: br.com.elatech.checkoutlab.scanner.ScanResultReceiver
   ```

   Preservar ação `android.intent.scanResult`, chave `scanKey`, permissão `android.permission.CAMERA`, modo de saída e demais configurações. Registrar a alteração, pois ela é reversível e muda o comportamento do coletor.
4. Com o Checkout Lab aberto, ler um EAN-13 de teste. Esperado: a tela mostra código, horário e ação.
5. Se não chegar evento, coletar apenas diagnóstico:

   ```bash
   /Users/alexecheverria/Library/Android/sdk/platform-tools/adb -d logcat -d -s ScanResultReceiver ActivityManager
   ```

   Não experimentar aleatoriamente outros valores; registrar a falha e comparar com a documentação/SDK.

## Comandos validados no Mac

```bash
# verificar conexão autorizada
/Users/alexecheverria/Library/Android/sdk/platform-tools/adb devices -l

# compilar
cd /Users/alexecheverria/Documents/Elatech/coletores-elatech/apps/handheld-checkout
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/Users/alexecheverria/Library/Android/sdk \
./gradlew clean assembleDebug --no-daemon

# instalar depois de autorização explícita
/Users/alexecheverria/Library/Android/sdk/platform-tools/adb -d install -r app/build/outputs/apk/debug/app-debug.apk

# abrir o app
/Users/alexecheverria/Library/Android/sdk/platform-tools/adb -d shell am start -n br.com.elatech.checkoutlab/.MainActivity
```

Ferramentas instaladas no Mac durante esta sessão: Homebrew OpenJDK 17.0.20.1 e Gradle 9.7.1. O projeto fixa seu Gradle Wrapper em 8.7.

## Arquivos que devem ser lidos primeiro

1. `AGENTS.md` na raiz do projeto Elatech e as instruções que ele manda ler.
2. `docs/PLANO_CONTINUO.md` — fonte de verdade de fases e pendências.
3. `docs/collector/SESSAO_DESCOBERTA_2026-09-01.md` — evidências do aparelho.
4. `docs/scanner/integracao-xcscanner.md` — contrato de scanner e decisão sobre SDK.
5. `docs/development/conectar-e-instalar.md` — procedimento de desenvolvimento.
6. `apps/handheld-checkout/README.md` e `apps/handheld-checkout/app/src/main/...` — implementação atual.

## Regras de trabalho confirmadas pelo usuário

- Cada entrega estável deve seguir **validar → commit → push para `main`**; não deixar conhecimento relevante somente na conversa.
- Atualizar `docs/PLANO_CONTINUO.md` e a documentação afetada em toda descoberta, alteração ou passagem de fase.
- Pedir autorização explícita antes de instalar/desinstalar app, alterar Barcode Utility, mexer em Kiosk/MDM, conectar sistemas externos ou usar dados reais.
- O repositório é uma wiki técnica contínua e a memória persistente do laboratório.

## Histórico Git relevante

- `18e7295` — base inicial da wiki.
- `bcb1343` — contrato de broadcast observado.
- `aded1ce` — app de diagnóstico compilável.
- `cc8f244` — instalação do app registrada e regra de commit/push documentada.

## Prompt curto para iniciar a nova sessão

```text
Leia docs/handoff/CLAUDE_CODE_2026-09-01.md e continue o laboratório MovFast Ranger 2N. Não altere o Barcode Utility sem pedir autorização ao usuário. Primeiro confirme se ele concedeu a permissão no app Checkout Lab; depois execute a prova de broadcast seguindo o handoff. A cada entrega estável, valide, atualize a documentação/plano, faça commit e push para main.
```
