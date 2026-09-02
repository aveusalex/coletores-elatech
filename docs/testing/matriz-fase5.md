# Matriz de testes — Fase 5 (qualidade de laboratório)

Aparelho: MovFast Ranger 2N, Android 13. App: `br.com.elatech.checkoutlab`.
Fonte de leitura em produção: `SdkScannerSource` (XCScanner SDK). Deploy alvo:
coletor dedicado ao checkout.

## Automatizado (`./gradlew testDebugUnitTest`)

| Área | Casos | Arquivo |
| --- | --- | --- |
| Dinheiro em centavos | formatação pt-BR, soma, multiplicação, rejeição de negativo, `ofReais` | `MoneyTest` |
| Carrinho | adicionar/incrementar, ordem de inserção, `setQuantity` (inclui 0 = remove, negativo lança, sku inexistente no-op), `clear` | `CartTest` |
| Fluxo de checkout | bip conhecido soma, repetido incrementa, debounce 400 ms, código desconhecido, cadastrar-e-adicionar, finalizar (grava histórico + limpa), venda vazia = null, `setQuantity(0)` remove | `CheckoutFlowTest` |

17 casos. Sem dependência de aparelho.

## Manual no aparelho

| # | Caso | Passos | Esperado |
| --- | --- | --- | --- |
| M1 | EAN-13 conhecido | Bipar `7896445490550` | Linha "Água mineral 500ml" R$ 2,50, total soma, status "Adicionado" |
| M2 | QR conhecido | Cadastrar um QR no catálogo, bipar | Adiciona igual ao EAN |
| M3 | Código desconhecido | Bipar código fora do catálogo | Abre "Cadastrar produto fictício"; ao confirmar, entra no carrinho |
| M4 | Leitura repetida rápida | Bipar o mesmo código 2× em < 400 ms | Conta **uma** vez (debounce) |
| M5 | Leitura repetida normal | Bipar o mesmo código 2× com pausa | Quantidade vai a 2, total dobra |
| M6 | Ajuste de quantidade | `+` / `−` / `Remover` na linha | Total e contagem acompanham; `−` em 1 remove a linha |
| M7 | Finalizar venda | Carrinho com itens → "Finalizar venda" | Status "Venda XXXX registrada"; carrinho zera |
| M8 | Finalizar vazio | "Finalizar venda" sem itens | Toast "Carrinho vazio"; nada gravado |
| M9 | Histórico | Abrir "Histórico" | Lista a venda de M7 com linhas e total |
| M10 | Persistência — reabrir app | Fechar e abrir o app | Histórico mantém as vendas (carrinho em aberto pode ser perdido) |
| M11 | Persistência — reinício do aparelho | Reiniciar o Ranger, abrir o app | Histórico mantém as vendas; catálogo semeado intacto |
| M12 | Persistência — atualização do APK | `adb install -r` de nova versão | Histórico e catálogo mantidos (migração Room quando houver mudança de schema) |
| M13 | Tela bloqueada | Bloquear/desbloquear durante uso | App volta ao mesmo estado; leitura volta a funcionar |
| M14 | Rotação | Girar o aparelho | Sem efeito — telas fixas em retrato |
| M15 | Ajustes do scanner | Mudar beep/volume/sufixo/simbologias → "Salvar e aplicar" | Toast "Ajustes aplicados"; `applyConfig ok` no logcat; `shared_prefs/scanner_config.xml` atualizado; efeito no próximo bip |
| M16 | Ajustes persistem | Reabrir "Ajustes" | Campos refletem o que foi salvo |
| M17 | Saída broadcast puro | Ajustes → `output = BROADCAST_ONLY` → salvar; abrir um campo de texto e bipar | O código **não** é digitado no campo (sem wedge/ENTER) |
| M18 | Compatibilidade de versão | Abrir "Ajustes" ou "Diagnóstico" | "Serviço: sdk=… service=… match=true" |
| M19 | Diagnóstico — Broadcast | "Diagnóstico" → fonte Broadcast → bipar | "Última leitura" mostra código, simbologia, hora, extras |
| M20 | Diagnóstico — SDK | "Diagnóstico" → "Trocar fonte" → bipar | Mesmo resultado via `onResult` do SDK |

## Recuperação do banco

| # | Caso | Passos | Esperado |
| --- | --- | --- | --- |
| R1 | Limpeza de dados | Config Android → Apps → Checkout Lab → Limpar dados | Próxima abertura re-semeia o catálogo; histórico vazio |
| R2 | Arquivo WAL | Ler o DB por ADB | `run-as br.com.elatech.checkoutlab cat databases/checkoutlab.db*` (os 3 arquivos: `.db`, `.db-wal`, `.db-shm`) — o coletor não tem `sqlite3` |

## Limitações conhecidas (laboratório)

- `allowMainThreadQueries()`: aceitável pela base trivial; produção usaria consultas fora da thread principal.
- Carrinho em aberto vive em memória; não sobrevive à morte do processo. Só a venda concluída é persistida.
- `ScannerConfig` é aplicado na configuração **global** do coletor (device-global). Só é seguro num coletor dedicado ao app.
