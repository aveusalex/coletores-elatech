# ADR 0001 — Piloto offline-first com fronteira explícita de middleware

## Status

Aceita para o laboratório inicial em 2026-09-01.

## Contexto

Precisamos aprender a desenvolver no MovFast Ranger 2N por meio de um checkout de mão simples. O objetivo atual não autoriza integração com sistemas da Elatech ou processamento de transações reais, mas a solução poderá se transformar em middleware futuramente.

## Decisão

Construir primeiro um aplicativo Android Kotlin offline, com banco local Room/SQLite e massa fictícia. Separar internamente scanner, domínio de catálogo/carrinho/vendas e persistência. Reservar `services/middleware` e `contracts` no repositório, sem implementar servidor ou integração agora.

## Consequências

- O primeiro ciclo é pequeno, reversível e testável no coletor real.
- O app não fica acoplado a ERP ou banco de produção.
- A futura integração exige uma decisão arquitetural adicional e uma autorização explícita.
- Pode haver retrabalho controlado quando os contratos reais forem conhecidos; esse custo é preferível a inventar uma integração sem evidência.
