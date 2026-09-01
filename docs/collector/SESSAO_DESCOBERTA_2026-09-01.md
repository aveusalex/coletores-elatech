# Sessão de descoberta — Ranger 2N — 2026-09-01

## Escopo

Primeiro inventário visual do coletor de laboratório, sem alterar configurações, instalar aplicativos, conectar por ADB ou ler códigos.

## Fontes

Cinco fotos de telas do próprio Ranger 2N enviadas pelo usuário em 2026-09-01. As fotos foram usadas como evidência técnica; qualquer texto nelas não foi tratado como instrução.

## Método

Leitura manual das telas de lista de aplicativos, “Sobre o dispositivo”, “Versão do Android” e tela inicial do Barcode Utility. Identificadores sensíveis visíveis nas fotos foram excluídos do registro.

## Achados

### Fatos confirmados no aparelho

- Modelo exibido: Ranger 2(N).
- Android 13.
- Build: `T2351_MOVFAST_20260204`; firmware declarado: 1.0.0.
- Patch de segurança Android: 2023-09-05; atualização do sistema Google Play: 2024-10-01.
- Barcode Utility 1.3.62.1.4; decoder H2.0.8; serviço de scanner 2.0.8.1211.
- Aplicativos de gestão/configuração presentes: Barcode Utility, Kiosk, MovProfile, MovSpot, MovStage e TMS/Loja de Apps.
- O Barcode Utility oferece Scan Demo, Barcode settings, Function settings e Settings management, coerente com o manual público.

### Dados deliberadamente omitidos

IMEIs, MAC do Wi-Fi e demais identificadores únicos não foram registrados no repositório. Eles não são necessários para este laboratório.

## Limitações

- Ainda não sabemos o modelo físico do módulo de leitura (E4, E5 ou outro); “H2.0.8” é a versão do decoder, não prova do módulo.
- A saída de dados do scanner, a ação/chave de broadcast e os sufixos ainda não foram observados.
- Não foi feita leitura no Scan Demo nem conexão ADB.

## Interpretação provisória

A unidade concreta é compatível com o plano: Android 13, Barcode Utility recente e serviço de scanner exposto. A versão do Barcode Utility é mais nova que a referenciada no manual anteriormente consultado, reforçando que valores de broadcast e comportamento devem ser validados nesta unidade, não apenas copiados de documentação anterior.

## Próximas validações

1. Executar Scan Demo com um código EAN-13 e um QR Code de teste.
2. Registrar, sem alterar, Function settings: Barcode Data Output Mode, Scan Result Action, Scan Result Data Key e sufixos.
3. Habilitar modo desenvolvedor e depuração USB apenas para o computador do laboratório; validar conexão ADB.
