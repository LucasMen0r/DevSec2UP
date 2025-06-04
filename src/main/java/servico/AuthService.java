package servico;
import utils.InputSanitizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;
public class AuthService {
/**
 * Lida com a autenticação do usuário com senha mestra e verificação TOTP.
 * Garante acesso seguro usando credenciais com hash e códigos baseados em tempo.
 */
    private static final String PASSWORD_FILE = "master_password.dat";
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_PASSWORD_LENGTH = 64;
    private static final int MAX_TOTP_LENGTH = 6;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private final Scanner scanner;
    /**
     * Constrói o AuthService, inicializando a autenticação com validação de senha e TOTP.
     * 
     * Gera ou carrega um segredo seguro.
     *
     * @param scanner Scanner usado para ler a entrada de informação do usuário
     * 
     * @throws Exception se a autenticação falhar depois de uma certa quantidade de vezes
     * 
     */
    public AuthService(Scanner ler) throws Exception {
        this.scanner = ler;
        String masterPasswordHash = loadOrCreatePassword();
        String totpSecret = ServicoTOTP.loadOrCreateSecret();

        System.out.println("\nAutenticação em dois fatores (TOTP) está ativada.");
        System.out.println("Use este segredo no seu aplicativo autenticador, caso ainda não esteja registrado.:");
        System.out.println(ServicoTOTP.getBase32Secret(totpSecret));
        System.out.println("Ou, caso prefira, leia o QR code:");
        System.out.println(ServicoTOTP.getOtpAuthUrl(totpSecret, "user@example.com", "SecurePasswordManager"));

        String sessionPassword = null;

        for (int attempts = 1; attempts <= MAX_ATTEMPTS; attempts++) {
            try {
                System.out.print("\nEntre com a senha: ");
                String inputPassword = InputSanitizer.sanitize(scanner.nextLine(), MAX_PASSWORD_LENGTH, false);

                if (!BCrypt.checkpw(inputPassword, masterPasswordHash)) {
                    System.out.println("Senha incorreta.");
                    continue;
                }
                System.out.print("Erro no código TOTP: ");
                String inputCode = InputSanitizer.sanitize(scanner.nextLine(), MAX_TOTP_LENGTH, true);

                if (!NUMBER_PATTERN.matcher(inputCode).matches()) {
                    throw new IllegalArgumentException("Apenas números neste espaço.");
                }
                if (ServicoTOTP.validateCode(totpSecret, inputCode)) {
                    System.out.println("Autenticação bem sucedida.");
                    sessionPassword = inputPassword;
                    break;
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Entrada inválida. " + InputSanitizer.escapeForLog(ex.getMessage()));
            }
        }
        if (sessionPassword == null) {
            throw new Exception("Número máximo de falhas na autenticação.");
        }
        String salt = ServicoEncriptacao.getOrCreatePersistentSalt();
        ServicoEncriptacao.setSessionKeyAndSalt(sessionPassword, salt);
    }
    /**
     *Carrega o hash existente da senha mestra ou solicita que o usuário crie uma nova.
     
      *Verifica a senha em relação a vazamentos de dados conhecidos e impõe um comprimento mínimo.
     *
     * @return a versão da senha master após o hash
     * 
     * @throws Exception em caso de erro na leitura ou na gravação da senha
     */
    String loadOrCreatePassword() throws Exception {
        Path path = Paths.get(PASSWORD_FILE);

        if (Files.exists(path)) {
            return Files.readString(path).trim();
        }
        System.out.println("No master password found. Please create one now.");

        String newPassword;

        while (true) {
            try {
                System.out.print("Nova senha: ");
                newPassword = InputSanitizer.sanitize(scanner.nextLine(), MAX_PASSWORD_LENGTH, false);
                int breachCount = ChecagemVazamento.checkPassword(newPassword);
                if (breachCount < 0) {
                    System.out.println("Erro ao checar a senha em vazamentos de infromação. Por faovr, tente novamente.");
                    continue;
                } else if (breachCount > 0) {
                    System.out.printf(
                            "Essa senha apareceu em %d falha(as). Favor esvolher uma senha mais forte.%n",
                            breachCount
                    );
                    continue;
                }
                if (newPassword.length() < 8) {
                    System.out.println("A senha deve ter 8(oito) caracteres, tente novamente.");
                    continue;
                }
                System.out.print("Confirme a senha: ");
                String inputPassword = InputSanitizer.sanitize(scanner.nextLine(), MAX_PASSWORD_LENGTH, false);
                if (!newPassword.equals(inputPassword)) {
                    System.out.println("As senhas não são correspondentes, tente novamente.");
                    continue;
                }
                break;
            } catch (IllegalArgumentException ex) {
                System.out.println("Entrada inválidaa. " + InputSanitizer.escapeForLog(ex.getMessage()));
            }
        }
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        Files.writeString(path, hash);
        System.out.println("Senha salva.");
        return hash;
    }
}