# Conectar o Ranger 2N ao computador e instalar um app de teste

## Propósito

Procedimento para implantar um app de laboratório em um único coletor, sem MDM, Kiosk, dados comerciais ou distribuição para usuários finais.

## Pré-requisitos

- Ranger 2N desbloqueado, carregado e com cabo USB-C que suporte dados.
- Computador com Android Studio e Android SDK Platform Tools.
- Autorização explícita para usar opções de desenvolvedor no aparelho de teste.

## Passos no coletor

1. Em **Configurações → Sobre o dispositivo**, tocar repetidamente em **Número da versão** até liberar o modo de desenvolvedor. Esse é o caminho documentado pela MovFast para R2/R2N.
2. Em **Configurações → Sistema → Avançado → Opções do desenvolvedor**, habilitar **Depuração USB** apenas para o computador de desenvolvimento.
3. Conectar por USB-C e aceitar no coletor a chave RSA mostrada para aquele computador. Não aceitar chaves de computadores desconhecidos.

Fontes: [MovFast — ativar modo de desenvolvedor](https://movfast.com.br/pt-br/centraldeajuda/como-ativar-o-modo-de-desenvolvedor-r2-e-r2k) e [Android Developers — dispositivo físico](https://developer.android.com/studio/run/device).

## Verificação no computador

No Android Studio, selecionar o aparelho físico e usar **Run** para compilar e instalar a variante de depuração. Alternativamente, depois de reconhecer o aparelho, a ferramenta Android `adb` permite verificar a conexão e instalar um APK de teste.

```text
adb devices
adb -d install caminho/para/app-debug.apk
```

O segundo comando é referência para a fase de implementação; não deve ser usado antes de haver um APK próprio e validado. Fonte: [Android Debug Bridge](https://developer.android.com/tools/adb).

## Procedimento validado para o Checkout Lab

Em 2026-09-01, este Mac reconheceu o Ranger autorizado por USB. O projeto de diagnóstico foi compilado com JDK 17, Gradle Wrapper 8.7 e Android SDK local. Para repetir a compilação:

```text
cd apps/handheld-checkout
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
ANDROID_HOME=/Users/alexecheverria/Library/Android/sdk \
./gradlew assembleDebug --no-daemon
```

O APK resultante é `app/build/outputs/apk/debug/app-debug.apk`. Depois de autorização explícita para instalar no aparelho conectado:

```text
adb -d install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm path br.com.elatech.checkoutlab
```

Antes de qualquer bip de teste, abrir o app e conceder a permissão que ele solicita. Ela corresponde à permissão configurada no broadcast do Barcode Utility; o aplicativo não abre nem utiliza a câmera.

Em 2026-09-01, a instalação desse APK retornou `Success`, o pacote foi encontrado pelo ADB e a atividade inicial foi aberta no Ranger. A próxima prova ainda depende de o usuário conceder essa permissão e autorizar a troca do destino do broadcast para o receiver do Checkout Lab.

## Critérios de sucesso

- O aparelho aparece como autorizado, não apenas conectado.
- O Android Studio instala uma variante de debug no aparelho correto.
- O app abre sem depender de rede.
- A depuração USB pode ser revogada ao encerrar a sessão.

## Reversão e segurança

- Ao terminar, desconectar o cabo e revogar autorizações USB de depuração se o computador não for dedicado ao laboratório.
- Não habilitar depuração sem fio nesta primeira etapa.
- Em Windows, pode ser necessário driver USB do fabricante; em macOS, a documentação Android indica que normalmente não há driver adicional. Confirmar no computador que será usado, sem instalar drivers genéricos de fontes desconhecidas.
