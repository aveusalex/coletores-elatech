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
- **White list está habilitada.** Tocar no item não abre tela, diálogo ou lista. Nesta versão do Barcode Utility ela se comporta como uma chave liga/desliga; seu efeito exato permanece não confirmado.
- Contrato de broadcast efetivamente exibido, sem alteração:
  - ação: `android.intent.scanResult`;
  - chave de dados: `scanKey`;
  - permissão: `android.permission.CAMERA`;
  - pacote de destino: `com.android.scantest`;
  - classe de destino: `ScanTestActivity`.
- ADB confirmou que o pacote configurado `com.android.scantest` **não está instalado** no aparelho. Portanto, essa configuração não tem receptor disponível e não está destinada ao nosso futuro aplicativo.

### Conexão de desenvolvimento confirmada

- ADB autorizado por USB em 2026-09-01; a identificação técnica retornada foi `Ranger_2_N_`.
- Android API 33; ABI `arm64-v8a`.
- Serviços relacionados ao scanner presentes: `com.xcheng.datawedge` versão 1.2.9 e `com.xcheng.scanner4710` versão 1.1.0.

### Aplicativo de diagnóstico compilado

- Foi criado o aplicativo local `br.com.elatech.checkoutlab`, sem rede, catálogo, dados reais ou SDK proprietário.
- O APK de debug foi compilado com sucesso em 2026-09-01, mas ainda não foi instalado no Ranger.
- Seu receiver próprio é `br.com.elatech.checkoutlab.scanner.ScanResultReceiver`. A troca do destino no Barcode Utility permanece pendente de autorização explícita.

### Dados deliberadamente omitidos

IMEIs, MAC do Wi-Fi e demais identificadores únicos não foram registrados no repositório. Eles não são necessários para este laboratório.

## Limitações

- Ainda não sabemos o modelo físico do módulo de leitura (E4, E5 ou outro); “H2.0.8” é a versão do decoder, não prova do módulo.
- O efeito funcional da White list ainda não foi comprovado; a interface não expõe uma lista editável.
- Ainda não sabemos se a permissão de broadcast configurada exigirá declaração específica no manifesto do aplicativo de teste; isso será validado na prova de integração.
- Ainda não foi recebido um evento de scanner por aplicativo próprio.

## Interpretação provisória

A unidade concreta é compatível com o plano: Android 13, Barcode Utility recente, serviço de scanner exposto, ADB autorizado e prova de leitura em três simbologias. A saída `broadcast/focus` confirma que um receptor Android é uma rota aplicável. Nesta unidade, a configuração real diverge dos valores genéricos publicados no manual, portanto ela prevalece sobre a documentação de referência. A White list não oferece uma tela de cadastro nesta interface. O destino explícito atual, `com.android.scantest` / `ScanTestActivity`, não existe no aparelho e é uma configuração inativa. Quando existir nosso aplicativo de diagnóstico, a configuração deverá ser mudada de modo controlado e autorizado para seu próprio pacote e receiver.

## Próximas validações

1. Fechar os diálogos do Barcode Utility com **Cancelar**, preservando os valores atuais.
2. Criar o aplicativo de diagnóstico com pacote próprio e receptor configurável.
3. Instalar o APK após autorização explícita.
4. Após sua instalação e autorização, trocar o pacote/classe de destino e confirmar um evento por bip.
