# Plano contínuo e dump de contexto

> Esta é a fonte de verdade operacional do projeto. Atualize-a ao terminar uma fase, mudar escopo, descobrir uma limitação do aparelho ou tomar uma decisão técnica. Não registrar credenciais, dados comerciais, serial completo, IMEI ou IMSI.

## Objetivo

Aprender a construir e operar, de forma isolada, um checkout móvel no MovFast Ranger 2N: cadastrar produtos fictícios, bipar códigos, montar carrinho, alertar itens inexistentes e fechar vendas simuladas. O desenho preserva uma fronteira para um middleware futuro, mas não cria nem integra middleware nesta etapa.

## Estado atual — 2026-09-01

- **Fatos confirmados na fonte:** Ranger 2N é Android; Barcode Utility suporta broadcast; XCScanner SDK possui documentação e fonte pública no ecossistema MovFast/MERTECH.
- **Ainda não confirmado no aparelho:** firmware, variante do scanner, versão do Barcode Utility/serviço, ação/chave efetivamente configuradas e conexão ADB.
- **O que existe no repositório:** wiki técnica, arquitetura, ADR e convenções de documentação.
- **O que não existe:** aplicativo, APK, dados reais, conexão com Clip Store/ERP, pagamento ou infraestrutura de middleware.

## Timeline

╭─ FASE 1 — Base de conhecimento e direção ─╮
│ ✅ done · Escopo offline, seguro e didático definido. │
│ ✅ done · Wiki, fontes, arquitetura e ADR publicados. │
│ ✅ done · Fronteira para middleware futuro registrada. │
╰─────────────────────────────────────────────╯

╭─ FASE 2 — Descoberta controlada do aparelho ─╮
│ 🔄 doing · 🔴 VOCÊ ESTÁ AQUI │
│ ✅ done · Registrar Android, firmware e Barcode Utility. │
│ ⏳ pending · Validar Scan Demo com EAN-13 e QR Code de teste. │
│ ⏳ pending · Habilitar depuração USB e confirmar conexão ADB. │
╰────────────────────────────────────────────────╯

╭─ FASE 3 — Prova de integração do scanner ─╮
│ ⏳ pending · Criar app Android mínimo de diagnóstico. │
│ ⏳ pending · Receber um evento por bip via broadcast. │
│ ⏳ pending · Comparar serviço/SDK e decidir se SDK será usado. │
╰───────────────────────────────────────────────╯

╭─ FASE 4 — Checkout offline de aprendizagem ─╮
│ ⏳ pending · Criar catálogo local, carrinho e venda simulada. │
│ ⏳ pending · Tratar código desconhecido e leitura repetida. │
│ ⏳ pending · Guardar vendas e produtos em banco local Room. │
╰────────────────────────────────────────────────╯

╭─ FASE 5 — Qualidade e operação de laboratório ─╮
│ ⏳ pending · Testar reinício, bateria, tela bloqueada e rotação. │
│ ⏳ pending · Executar casos de EAN, QR, item desconhecido e duplicidade. │
│ ⏳ pending · Decidir se Kiosk é útil somente para demonstração controlada. │
╰──────────────────────────────────────────────────╯

╭─ FASE 6 — Decisão de evolução para middleware ─╮
│ ⏳ pending · Identificar sistema-alvo e requisito real de integração. │
│ ⏳ pending · Definir contrato, segurança, sincronização e idempotência. │
│ ⏳ pending · Autorizar explicitamente qualquer conexão externa. │
╰────────────────────────────────────────────────────╯

[██████░░░░░░░░] ~40%

## Sequência detalhada

### Fase 2 — descoberta do aparelho

**Objetivo:** provar as capacidades do Ranger concreto sem alterar sua operação além da depuração temporária.

1. Identificar a unidade por apelido interno; registrar versão de Android, patch, build/firmware, Barcode Utility e serviço de scanner.
2. Abrir Barcode Utility → **Scan Demo** e ler ao menos um EAN-13 e um QR Code de teste conhecidos.
3. Registrar as opções existentes: modo de saída, ação, chave, sufixos, simbologias habilitadas e feedback sonoro/vibração. Não alterar opções protegidas/de debug.
4. Habilitar Opções do desenvolvedor e Depuração USB somente no aparelho de laboratório; conectar pelo USB-C de dados e aceitar a chave RSA do computador de desenvolvimento.
5. Confirmar que `adb devices` exibe o aparelho autorizado. Desconectar e revogar a autorização ao fim da sessão se o computador não for dedicado.

**Critério de passagem:** registro completo da unidade, leituras demonstradas e conexão ADB autorizada.

**Documentos a atualizar:** `collector/ranger-2n.md`, `development/conectar-e-instalar.md` e esta página.

### Fase 3 — prova de integração do scanner

**Objetivo:** provar a entrega confiável de uma leitura ao aplicativo, antes de criar o checkout.

1. Criar projeto Android Kotlin mínimo, sem catálogo, login ou internet.
2. Implementar receptor de broadcast configurável para a ação/chave observadas no aparelho.
3. Mostrar valor recebido, simbologia quando disponível e horário; guardar somente log local de diagnóstico.
4. Testar leitura simples, repetida, item EAN e QR; confirmar exatamente um evento por bip.
5. Repetir após bloquear/desbloquear e reiniciar o Ranger.
6. Consultar a versão do serviço. Só se o broadcast não cobrir a necessidade, trazer o XCScanner SDK com versão/commit fixados e documentados.

**Critério de passagem:** leitura recebida de forma previsível em aplicativo próprio e configuração exportada/revisada.

**Documentos a atualizar:** `scanner/integracao-xcscanner.md`, novo registro de sessão em `docs/collector/` e esta página.

### Fase 4 — checkout offline de aprendizagem

**Objetivo:** construir o fluxo completo sem dados ou efeitos externos.

1. Modelar `Product`, `CartLine`, `SaleDraft` e `SaleCompleted`; preço em centavos inteiros.
2. Criar banco Room/SQLite com massa fictícia e cadastro local de itens.
3. Implementar tela de carrinho: leitura adiciona/incrementa item; leitura desconhecida mostra alerta e opção de cadastrar produto fictício.
4. Implementar remoção, alteração de quantidade, total e confirmação de compra simulada.
5. Salvar histórico local e limpar carrinho ao concluir.

**Critério de passagem:** demonstração reproduzível de cadastro → bip conhecido → bip desconhecido → fechamento simulado.

**Documentos a atualizar:** `architecture/visao-geral.md`, `contracts/`, casos de teste e esta página.

### Fase 5 — qualidade de laboratório

**Objetivo:** descobrir falhas reais de uso antes de considerar qualquer evolução.

1. Executar matriz de testes para códigos suportados, duplicidade, alta velocidade, bateria baixa, reinício e retorno do app.
2. Registrar falhas, causa provável, reprodução e decisão de correção.
3. Testar recuperação do banco local e atualização do app de depuração.
4. Avaliar Kiosk apenas se o fluxo já for estável e houver necessidade de demonstração em modo restrito.

**Critério de passagem:** todos os casos críticos documentados e demonstráveis sem perda de dados fictícios.

### Fase 6 — decisão de middleware

**Objetivo:** decidir com evidência se uma integração é necessária e qual problema ela resolveria.

Pré-condições obrigatórias:

- sistema-alvo identificado e autorizado;
- contratos de dados e dono operacional definidos;
- regras de sincronização offline, conflito e idempotência especificadas;
- autenticação, LGPD, auditoria e suporte avaliados;
- ADR aprovado antes de criar serviço ou conectar qualquer ambiente.

## Dump obrigatório ao fim de cada sessão

Ao encerrar um trabalho relevante, atualizar a seção abaixo e, se houver mudança de decisão, criar/atualizar um ADR. O texto deve permitir que outra pessoa retome o trabalho sem acessar esta conversa.

### Registro de continuidade

| Data | Fase | O que foi feito | Fatos confirmados | Pendências / bloqueios | Arquivos alterados | Próxima ação |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-01 | 1 | Repositório e wiki inicial publicados. | Fontes do R2N, Barcode Utility e XCScanner SDK registradas. | Teste no aparelho ainda não realizado. | `README.md`, `docs/**`, `CONTRIBUTING.md` | Iniciar descoberta controlada do Ranger. |
| 2026-09-01 | 2 | Inventário visual da unidade concluído, sem mudar configurações. | Ranger 2(N), Android 13, build `T2351_MOVFAST_20260204`, Barcode Utility 1.3.62.1.4 e serviço 2.0.8.1211. | Falta Scan Demo, configuração de saída e ADB. | `collector/ranger-2n.md`, `collector/SESSAO_DESCOBERTA_2026-09-01.md`, este plano. | Validar leitura no Scan Demo. |

## Regras de avanço

- Não pular uma fase apenas porque o próximo passo parece simples.
- Toda descoberta no aparelho deve ter data, versão e evidência, e deve ser marcada como fato ou hipótese.
- Nenhuma integração com sistemas reais é implícita; requer nova autorização e ADR.
- O repositório, e não o histórico desta conversa, é a memória operacional do projeto.

## O que destrava AGORA

- Ligar o Ranger 2N e disponibilizá-lo com cabo USB-C de dados.
- Autorizar uma sessão de descoberta sem dados reais: coleta de versões, Scan Demo e ADB.
- Definir qual computador será o ambiente de desenvolvimento inicial.
