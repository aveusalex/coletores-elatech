# XCScanner SDK — procedência do artefato

| Campo | Valor |
| --- | --- |
| Arquivo | `xcscanner_qrcode_v1.3.56.1.14-release.aar` |
| Origem | https://github.com/XCApex/XCScannerSDK |
| Ramo | `movfast` |
| Commit fixado | `2f813e44bc2d9fcf1756b067ea23ade37c132b18` (2026-03-06) |
| Caminho no repo de origem | `app/src/main/libs/xcscanner_qrcode_v1.3.56.1.14-release.aar` |
| Tamanho | 58245 bytes |
| SHA-256 | `ae1aba417327c9a9c2b58a07307ebd361f9fc3cccc7c1759dbe404c995ea7801` |
| Versão declarada (AndroidManifest meta-data) | `scanner_sdk_version = 1.3.56.1.14` |
| Licença | Apache-2.0 (ver `LICENSE-xcscanner.txt`) |
| Baixado em | 2026-09-01, via `gh api` (GitHub Contents API) |
| Autorização | Usuário autorizou explicitamente o uso do SDK em 2026-09-01. |

## Compatibilidade

Regra da tabela oficial (`docs/SDK_Service_Mapping_Table.md` do repo de origem):
para SDK >= `1.3.49.0.13`, o serviço serve se `serviceVersion >= sdkVersion`.
Aparelho de laboratório: Barcode Utility `1.3.62.1.4`, serviço exibido `2.0.8.1211`.
`SdkScannerSource.describeVersions()` loga `sdk=`, `service=` e `match=` no `init`
para conferência em execução.

## Como atualizar

1. Escolher a nova aar em `app/src/main/libs/` do repo de origem, no commit desejado.
2. Baixar, recalcular o SHA-256, atualizar esta tabela e `app/build.gradle.kts`.
3. Rodar `./gradlew assembleDebug testDebugUnitTest` e revalidar `match=true` no aparelho.
