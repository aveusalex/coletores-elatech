# Arquitetura evolutiva

## Princípio

O primeiro produto é local e offline; a arquitetura já separa as fronteiras para uma futura camada de middleware, sem criar infraestrutura desnecessária agora.

```text
Ranger 2N
┌───────────────────────────────────────────┐
│ App checkout móvel                         │
│  scanner adapter → catálogo → carrinho     │
│                         ↓                  │
│                    vendas simuladas        │
│                         ↓                  │
│                    banco local (Room)      │
└───────────────────────────────────────────┘
                         │
                         │ Futuro, não implementado no piloto
                         ▼
┌───────────────────────────────────────────┐
│ Middleware Elatech                         │
│ contratos → autenticação → fila/sincronismo│
│ adaptadores por sistema externo             │
└───────────────────────────────────────────┘
                         │
                         ▼
                 ERP / estoque / outros
```

## Componentes do app offline

| Componente | Responsabilidade | Não pode fazer |
| --- | --- | --- |
| `scanner` | receber e normalizar leitura do Barcode Utility/SDK | decidir preço ou alterar catálogo |
| `catalog` | produtos locais fictícios e busca por código | chamar ERP |
| `cart` | quantidade, remoção e total em centavos | persistir configuração do scanner |
| `sales` | fechamento e histórico simulado | processar pagamento real |
| `data` | Room/SQLite, migrações e dados de amostra | expor banco na rede |

## Fronteira para middleware futuro

O aplicativo deve falar apenas com contratos próprios, nunca com tabelas ou APIs internas de um ERP. Quando houver integração autorizada, o middleware será responsável por autenticação, idempotência, filas, mapeamentos e auditoria. O app continuará capaz de operar offline com uma fila local; a política de sincronização será uma decisão futura, não uma suposição.

## Contratos que merecem estabilidade

- `ScanResult`: valor bruto, simbologia, instante e origem.
- `Product`: identificador próprio, código de barras, nome, preço em centavos e estado ativo.
- `CartLine`: produto, quantidade e preço unitário capturado.
- `SaleDraft` / `SaleCompleted`: itens, total, instante e identificador local.

Ainda não existe API nem formato definitivo. Os contratos só serão publicados em `contracts/` quando o app iniciar.

## Decisões que ficam para depois

- servidor, banco central e tecnologia do middleware;
- integração com Clip Store ou qualquer ERP;
- autenticação, LGPD, usuários e permissões;
- pagamentos, fiscal, impressoras e estoque oficial;
- modo Kiosk, MDM e distribuição em massa.
