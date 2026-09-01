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

### Prova de leitura e configuração observada

- O **Scan Demo** leu com sucesso um Code 128, um EAN-13 e um QR Code na mesma sessão. A evidência mostra três leituras, sem falha observável.
- Modo de leitura: **Single Scan**.
- Saída de dados: **Output to broadcast/focus**; charset UTF-8.
- Formatação: prefixos vazios; primeiro sufixo `ENTER`; segundo sufixo vazio.
- Limite de múltiplos códigos: 1; região de decodificação: 100% do frame.
- ECI handling está habilitado. Leitura abaixo de 5% de bateria está desabilitada.
- A mira liga durante a leitura; iluminação e strobo estão habilitados.
- `Pass Scan Key Value` está desabilitado.
- **White list está habilitada.** Ainda não foi aberto o conteúdo da lista; ela pode restringir quais aplicativos recebem ou acionam a leitura.
- Ações/campos de broadcast estavam visíveis, mas os valores de Scan Result Action, Scan Result Data Key, Permission, PackageName e ClassName não aparecem nas imagens. Não foram inferidos nem alterados.

### Dados deliberadamente omitidos

IMEIs, MAC do Wi-Fi e demais identificadores únicos não foram registrados no repositório. Eles não são necessários para este laboratório.

## Limitações

- Ainda não sabemos o modelo físico do módulo de leitura (E4, E5 ou outro); “H2.0.8” é a versão do decoder, não prova do módulo.
- A ação/chave de broadcast e o conteúdo da White list ainda não foram observados.
- Não foi feita conexão ADB.

## Interpretação provisória

A unidade concreta é compatível com o plano: Android 13, Barcode Utility recente, serviço de scanner exposto e prova de leitura em três simbologias. A saída `broadcast/focus` confirma que um receptor Android é uma rota aplicável. A versão do Barcode Utility é mais nova que a referenciada no manual anteriormente consultado, reforçando que valores de broadcast e comportamento devem ser validados nesta unidade, não apenas copiados de documentação anterior. A White list precisa ser entendida antes de criarmos o app, pois pode bloquear a entrega da leitura ao novo pacote.

## Próximas validações

1. Abrir, sem alterar, White list e os campos Scan Result Action e Scan Result Data Key; registrar seus valores.
2. Habilitar modo desenvolvedor e depuração USB apenas para o computador do laboratório; validar conexão ADB.
