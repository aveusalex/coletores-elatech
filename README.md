# Coletores Elatech

Base de conhecimento e futuro código para experiências controladas com coletores de dados da Elatech.

O primeiro laboratório é um checkout móvel didático no **MovFast Ranger 2N**: cadastrar produtos fictícios, ler códigos de barras, montar carrinho, indicar item desconhecido e concluir uma venda simulada. O objetivo inicial é aprender o dispositivo e sua integração Android — não operar vendas reais.

## Estado atual

- Documentação e arquitetura inicial: em construção.
- Teste de leitura no Ranger 2N: ainda não executado.
- Aplicativo Android: ainda não iniciado.
- Middleware, ERP, estoque, pagamento e emissão fiscal: fora do escopo atual.

## Comece aqui

- [Índice da base de conhecimento](docs/README.md)
- [Ranger 2N: fatos, capacidades e pendências](docs/collector/ranger-2n.md)
- [Integração com o scanner e SDK](docs/scanner/integracao-xcscanner.md)
- [Preparação do computador e instalação de app](docs/development/conectar-e-instalar.md)
- [Arquitetura evolutiva: app offline até middleware](docs/architecture/visao-geral.md)
- [Plano contínuo e dump de contexto](docs/PLANO_CONTINUO.md)

## Princípios de segurança e escopo

- Usar somente produtos, preços e vendas fictícios até autorização explícita para outro escopo.
- Não conectar a Clip Store, ERP, meios de pagamento, impressoras fiscais ou bancos de produção.
- Não versionar APKs, credenciais, serial completo do aparelho, backups, dados pessoais ou configurações exportadas que contenham dados sensíveis.
- Tratar toda configuração de scanner como dependência versionada: registrar modelo, firmware, versão do Barcode Utility, modo de saída, ação, chave e sufixo.

## Estrutura prevista

```text
apps/handheld-checkout/  futuro app Android offline
services/middleware/     futuro serviço de integração, ainda sem implementação
contracts/               contratos de dados entre app e middleware
docs/                    wiki técnica, decisões e runbooks
samples/                 massa de teste não comercial
```

## Como contribuir para o conhecimento

Toda descoberta precisa entrar em `docs/` no mesmo conjunto de mudanças que a comprovou. Consulte [CONTRIBUTING.md](CONTRIBUTING.md).
