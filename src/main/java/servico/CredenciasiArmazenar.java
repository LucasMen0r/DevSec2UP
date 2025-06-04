package servico;
import model.Credential;
import utils.InputSanitizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
/**
 * Funcionalidade responsável tanto slavar quanto acessar as credenciais no arquivo encriptografado.
 */
public class CredenciasiArmazenar {
    private static final Path FILE_PATH = Paths.get("credenciais.dat");
    /**
     * Salva a lista de credenciais em um arquivo devidamente criptografado
     *
     * @param credentials Lista de credenciais a serem salvas.
     *                      
     * @throws Exception Caso ocorra algum erro de encriptação ou na gravação do arquivo
     *                      
     */
    public static void saveCredentials(List<Credential> credentials) throws Exception {
        List<String> encryptedLines = new ArrayList<>();
        for (Credential cred : credentials) {
            String serviceName;
            String username;
            String encryptedPassword;
            //professor, por alguma razão as String só podem ser acessadas pelos respectivos métodos quando estão em inglês
            try {
                // Garante que todos os métodos estão devidamente sanitizados
                serviceName = InputSanitizer.sanitize(cred.serviceName(), 50, false);
                username = InputSanitizer.sanitize(cred.username(), 50, false);
                encryptedPassword = InputSanitizer.sanitize(cred.encryptedPassword(), 128, false);

                String line = String.format("%s,%s,%s", serviceName, username, encryptedPassword);
                encryptedLines.add(ServicoEncriptacao.encrypt(line));
            } catch (IllegalArgumentException e) {
                System.err.println("Credenciasi inválidas serão ignoradas: " + e.getMessage());
            }
        }
        // Cria um backup do arquivo atual caso este já exista
        if (Files.exists(FILE_PATH)) {
            Files.copy(FILE_PATH, Paths.get("credentciais_backup.dat"), StandardCopyOption.REPLACE_EXISTING);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
            for (String line : encryptedLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IOException("Erro na gravação do arquivo das credenciais: " + e.getMessage(), e);
        }
    }
    /**
     * Carrega e decripta as credenciais no arquivo.
     *
     * @return A lista das credenciasi.
     * 
     * @throws Exception Caso ocorra algum erro na leitura ou decriptação do arquivo.
     */
    public static List<Credential> loadCredentials() throws Exception {
        List<Credential> credentials = new ArrayList<>();

        if (!Files.exists(FILE_PATH)) {
            return credentials;
        }
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String decrypted = ServicoEncriptacao.decrypt(line);
                    String[] parts = decrypted.split(",", 3);
                    if (parts.length == 3) {
                        // Sanitiza e valida as partes já decriptadas
                        String serviceName = InputSanitizer.sanitize(parts[0], 50, false);
                        String username = InputSanitizer.sanitize(parts[1], 50, false);
                        String encryptedPassword = InputSanitizer.sanitize(parts[2], 128, false);
                        credentials.add(new Credential(serviceName, username, encryptedPassword));
                    } else {
                        System.err.println("Formato inválido: " + decrypted);
                    }
                } catch (IllegalArgumentException ex) {
                    System.err.println("Formato de credenciais inválido: " + ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("Erro de decriptação: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IOException("Erro na leitura do arquivo de credenciasi: " + e.getMessage(), e);
        }
        return credentials;
    }
}