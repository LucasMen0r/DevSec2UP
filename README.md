
# Índice

- Funcionalidades  
- Notas de Segurança  
- Pré-requisitos  
- Tecnologias Utilizadas  
- Geração de Código QR para TOTP  
- Instalação  
- Uso  
- Estrutura de Arquivos  
- Contribuição  
- Licença  
- Aviso Legal  

## Funcionalidades

- **Geração de Senhas**: Opção para gerar automaticamente senhas fortes com comprimento e conjuntos de caracteres personalizáveis.  
- **Verificação de Vazamento de Senhas**: As senhas geradas são verificadas em bancos de dados de vazamentos (como o HaveIBeenPwned) para garantir que não foram comprometidas.  
- **Segurança das Senhas**:  
  - Criptografia das senhas armazenadas usando algoritmos padrão da indústria (AES-256).  
  - Integração com a API do HaveIBeenPwned para verificar senhas comprometidas.  
  - Operações seguras de cópia para a área de transferência (a área de transferência é limpa após um curto período).  
- **Interface Amigável**: Interface de linha de comando com opções claras de menu para adicionar, recuperar, atualizar e excluir credenciais.  
- **Autenticação em Dois Fatores (2FA)**: Suporte para TOTP (Senha de Uso Único baseada em Tempo) para maior segurança das contas.  
- **Senha Mestra**: Protege o acesso a todas as credenciais armazenadas.  
- **Auditoria e Verificação de Vazamentos**: Verifique facilmente se suas senhas foram expostas em vazamentos de dados conhecidos.  

## Notas de Segurança

- **Criptografia Avançada**: Todas as credenciais armazenadas são protegidas usando AES-GCM para criptografia autenticada.  
- **Validação de Entrada**: As entradas fornecidas pelos usuários são rigorosamente validadas para evitar ataques de injeção ou entradas inseguras.  
- **Limpeza de Dados Sensíveis**: Mecanismos estão implementados para limpar chaves de criptografia e dados sensíveis da memória quando o aplicativo é encerrado.  
- A senha mestra nunca é armazenada; apenas um hash é mantido usando BCrypt.  
- As operações de cópia para a área de transferência são limpas após um curto tempo para evitar vazamentos.  
- As senhas nunca são registradas ou exibidas em texto simples.  

## Pré-requisitos

- Java Development Kit (JDK) 22 ou superior  
- Maven 3.6.0 ou superior  
- Git (opcional, para controle de versão)  

## Tecnologias Utilizadas

- **Java 22**: Linguagem de programação principal  
- **Maven**: Gerenciador de projetos e ferramenta de build  

**Dependências:**  
- jBCrypt: Para hash de senhas  
- Gson: Para serialização JSON  
- Apache Commons Codec: Para utilitários de codificação/decodificação  

## Geração de Código QR para TOTP

Para configurar facilmente a Autenticação em Dois Fatores (2FA) com aplicativos autenticadores (como Google Authenticator, Microsoft Authenticator ou Authy), você pode converter sua URL TOTP em um código QR usando uma das seguintes ferramentas online gratuitas:

- https://www.qr-code-generator.com/  
- https://www.the-qrcode-generator.com/  
- https://www.qrstuff.com/  
- https://www.unitag.io/qrcode  
- https://www.google.com/chart?cht=qr&chs=300x300&chl=YOUR_TOTP_URL (substitua YOUR_TOTP_URL pela sua URL TOTP real)  

**Instruções:**  
1. Copie sua URL TOTP (ex.: `otpauth://totp/YourApp:username?secret=BASE32SECRET&issuer=YourApp`).  
2. Cole-a em um dos sites geradores de QR Code acima.  
3. Escaneie o QR Code gerado com seu aplicativo autenticador.  

## Instalação

Clone o repositório:  

```bash
git clone https://github.com/LucasMen0r/DevSec2UP.git  
cd GerenciadorSenhaSegura  
```  

Compile o projeto com Maven:  

```bash
mvn clean package  
```  

O JAR executável será gerado no diretório `target/`.  

## Uso

Execute o aplicativo:  

```bash
java -jar target/gerenciadorsenhasegura-1.0-SNAPSHOT-jar-with-dependencies.jar  
```  

**Configuração inicial:**  
- Você será solicitado a criar uma senha mestra. Esta senha será necessária para acessar suas credenciais.  

**Autenticação em Dois Fatores (2FA):**  
- Configure o TOTP para uma camada extra de segurança. Armazene seu segredo TOTP com segurança.  

**Opções do menu principal:**  
- Listar todas as credenciais  
- Adicionar nova credencial  
- Excluir uma credencial  
- Copiar senha para a área de transferência  
- Verificar se alguma senha foi comprometida  
- Sair  

**Geração de Senhas:**  
- Escolha o comprimento da senha e os tipos de caracteres (maiúsculas, minúsculas, dígitos, símbolos).  

**Verificação de Vazamento de Senhas:**  
- Insira uma senha para verificar se ela foi exposta em vazamentos de dados conhecidos, utilizando a API do HaveIBeenPwned.  

## Notas de Segurança

- Todas as credenciais são criptografadas em repouso usando AES-256.  
- A senha mestra nunca é armazenada; apenas um hash é mantido usando BCrypt.  
- As operações de cópia para a área de transferência são limpas após um curto período para evitar vazamentos.  
- As senhas nunca são registradas ou exibidas em texto simples.  

## Estrutura de Arquivos

- `src/main/java/` – Código-fonte da aplicação  
- `lib/` – Bibliotecas externas (se houver)  
- `target/` – Binários compilados e JARs empacotados  

## Contribuição

Contribuições são bem-vindas! Por favor, faça um fork do repositório e envie um pull request. Para mudanças significativas, abra uma issue primeiro para discutir o que você gostaria de modificar.  

## Licença

Este projeto está licenciado sob a Licença MIT. Veja o arquivo LICENSE para mais detalhes.  

## Aviso Legal

Este projeto é para fins educacionais. Use por sua conta e risco. Sempre faça backup de suas credenciais e nunca compartilhe sua senha mestra.
