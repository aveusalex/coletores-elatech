# Como registrar conhecimento

Este repositório é também a memória técnica do laboratório. A documentação não é uma etapa de encerramento.

## Regra de atualização

Ao descobrir, testar ou alterar algo, inclua na mesma mudança:

1. O que foi feito e em qual aparelho/ambiente.
2. Evidência: fonte, versão, comando seguro, captura sem dados sensíveis ou resultado observado.
3. Status: **fato confirmado**, **hipótese** ou **pendência**.
4. Reversão, se a alteração tiver mudado uma configuração do coletor.
5. Link para o documento do assunto no índice de `docs/`.

## Registro do aparelho

Não publicar serial completo, IMEI, IMSI, número de telefone, Wi-Fi, credenciais ou dados de clientes. Para cada sessão, registrar apenas os campos do [modelo de evidência](docs/collector/ranger-2n.md#registro-da-unidade-de-teste).

## Decisões técnicas

Decisões que alteram arquitetura, dependência, contrato ou política de dados devem ganhar um ADR em `docs/adr/`. Use o formato de `0001-offline-first-e-fronteira-de-middleware.md`.

## Dados de teste

Use itens inventados. Preço é sempre representado em centavos inteiros. Nunca exporte base comercial para este repositório.
