# MovFast Ranger 2N

## Papel no laboratório

O Ranger 2N será a unidade Android onde o app de checkout de teste será instalado. O scanner profissional entrega a leitura ao Android; ele não substitui o aplicativo nem define as regras de carrinho, catálogo ou venda.

## Fatos confirmados na documentação pública

| Item | Informação |
| --- | --- |
| Sistema | Android 13 AER/Enterprise, conforme revisão pública do produto |
| Tela | 5,45 pol., 1440 × 720, touch |
| Memória | 4 GB RAM e 64 GB de armazenamento |
| Leitor | profissional 1D/2D; revisão pública atual cita E5, documentação anterior cita XC E4 |
| Conectividade | USB-C OTG, Wi-Fi, 4G e Bluetooth |
| Entrada de leitura | botões físicos laterais; o leitor é gerido pelo Barcode Utility |
| Robustez | IP67 e queda declarada de 1,5 m |

Fontes: [datasheet R2N](https://movfast.com.br/hubfs/%28NOVO%29%20R2N%20DATASHEET%20-%20251218-1.pdf?hsLang=pt-br), [guia rápido Ranger 2](https://24252647.fs1.hubspotusercontent-na1.net/hubfs/24252647/Central%20de%20Ajuda/Guia%20R%C3%A1pido%20-%20Quick%20Guide%20-%20Ranger2%20Rev.03.pdf).

## Atenção: revisões de hardware

Há documentos públicos com E4 e outros com E5, além de diferenças de processador. Isso não é contradição operacional para o piloto, mas impede afirmar qual variante está em mãos apenas pelo nome comercial. A integração deve descobrir as versões efetivas antes de depender de uma API específica.

## Registro da unidade de teste

Preencher somente durante a sessão física, sem serial completo nem IMEI:

| Campo | Valor |
| --- | --- |
| Identificador interno do aparelho | PENDENTE |
| Android / número de versão | PENDENTE |
| Patch de segurança | PENDENTE |
| Firmware/build | PENDENTE |
| Barcode Utility: versão | PENDENTE |
| Serviço XCScanner: versão | PENDENTE |
| Scanner detectado (E4/E5/outro) | PENDENTE |
| Data e responsável pelo teste | PENDENTE |

## O que não fazer nesta fase

- Não fazer reset, root, troca de firmware, alteração de configurações de depuração protegidas ou ativação de Kiosk.
- Não conectar a contas, dados ou redes de produção.
- Não assumir que uma configuração encontrada em tutorial serve para esta unidade.
