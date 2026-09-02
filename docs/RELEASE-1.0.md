# Checkout Lab v1.0.0 — MovFast Ranger 2N

Data: 2026-09-02. App: `br.com.elatech.checkoutlab`, `versionName 1.0.0`
(`versionCode 12`). Aparelho: MovFast Ranger 2(N), Android 13.

## O que é

Checkout móvel **offline** de aprendizagem: catálogo fictício, leitura por bip,
carrinho, item inexistente, fechamento de venda simulado — sem ERP, pagamento,
nuvem, rede ou dados reais. Fronteira preservada para um middleware futuro
(pastas `services/middleware` e `contracts/` reservadas, nada implementado).

## Escopo entregue (Fases 1–5)

### Scanner

- **Contrato confirmado no aparelho**: ação `com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST`,
  código em `EXTRA_BARCODE_DECODING_DATA`, simbologia em `EXTRA_BARCODE_DECODING_SYMBOLE`.
- **Costura `ScannerSource`**: o domínio não conhece o mecanismo.
  - `BroadcastScannerSource` — receiver registrado em runtime (`RECEIVER_EXPORTED`);
    manifesto é barrado no Android 13. Usado na prova e no diagnóstico.
  - `SdkScannerSource` — **XCScanner SDK** (aar vendorada, commit fixo, SHA-256,
    Apache-2.0 — `app/libs/PROVENANCE.md`). Callback `onResult`; aplica
    `ScannerConfig` na configuração do serviço. É a fonte do checkout.
- Compatibilidade verificada em execução: `sdk=1.3.56.1.14 service=1.3.62.1.4 match=true`.
- Decisão registrada em **ADR 0002** (broadcast na prova, SDK no produto;
  configuração é device-global → deploy alvo é coletor dedicado).

### Checkout (domínio puro + Room)

- `Money` em centavos inteiros, `Product`, `Cart` (add/incrementar/quantidade/remover/limpar),
  `CheckoutController` (debounce de 400 ms para leitura repetida), `CompletedSale`.
- Persistência **Room**: `products`, `sales`, `sale_lines`; catálogo fictício
  semeado no 1º create. Interfaces `Catalog`/`SaleHistory` mantidas — os testes
  usam as versões em memória.

### Telas

| Tela | Função |
| --- | --- |
| Checkout (`MainActivity`) | total, carrinho, +/−/remover, finalizar venda simulada, limpar; código desconhecido abre cadastro de produto fictício |
| Ajustes do scanner | edita e persiste `ScannerConfig` (beep, volume, gatilho, sufixo, saída, simbologias); aplica via SDK |
| Histórico de vendas | lista `CompletedSale` do Room (somente leitura) |
| Diagnóstico | mostra a leitura + dump de extras; alterna Broadcast/SDK em execução |

Todas em retrato fixo.

### Qualidade

- **17 testes JVM** (`MoneyTest`, `CartTest`, `CheckoutFlowTest`) — `./gradlew testDebugUnitTest`.
- **Matriz de testes manuais** — `docs/testing/matriz-fase5.md` (M1–M20, R1–R2).
- Callback do SDK entregue na thread principal.

## Verificado no aparelho

- Bip EAN-13 e QR → `onResult` do SDK → item no carrinho, total em centavos correto.
- Código desconhecido (QR) → cadastro na hora → entra no carrinho.
- Finalizar → grava `sales` + `sale_lines`, limpa o carrinho.
- Venda sobreviveu a `adb install -r` de nova versão (Room em disco).
- Ajustes: salvar → `applyConfig ok` + `shared_prefs/scanner_config.xml` atualizado.

## Limitações conhecidas

- `allowMainThreadQueries()` — aceitável pela base trivial; produção usaria I/O fora da thread principal.
- Carrinho em aberto não sobrevive à morte do processo; só a venda concluída é persistida.
- `ScannerConfig` afeta a configuração global do coletor — seguro só em coletor dedicado.
- Room `version = 1` sem migrations.

## Fora do 1.0 (Fase 6)

Integração com sistema real (middleware): exige sistema-alvo, contratos, regras
de sincronização/idempotência, LGPD/auditoria e ADR aprovado. Não iniciada.

## Como construir e instalar

```bash
cd apps/handheld-checkout
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=$HOME/Library/Android/sdk \
./gradlew assembleDebug testDebugUnitTest
adb -d install -r app/build/outputs/apk/debug/app-debug.apk
adb -d shell am start -n br.com.elatech.checkoutlab/.MainActivity
```

## Histórico Git relevante

Ver `git log` e `docs/PLANO_CONTINUO.md` (registro de continuidade). Marcos:
prova de broadcast, ADR 0002, costura `ScannerSource`, domínio Fase 4, SDK
vendorado e provado, Room, telas de ajustes/histórico, `v1.0.0`.
