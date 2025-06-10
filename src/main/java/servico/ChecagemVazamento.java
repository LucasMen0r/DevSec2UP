package servico;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.net.URI;
public class ChecagemVazamento {
    /**
     * Confere se a senha já foi vazada em algum vazamento de dados através da "Have I Been Pwned API".
     * 
     * @param password The password to check.
     * 
     * @return Nº de vezes que a senha foi vazada (0 = segura).
     */
    public static int checkPassword(String password) {
        try {
            // Passo 1: SHA-1 hash da senha
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = sha1.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02X", b));
            }
            String sha1Hash = sb.toString();
            String prefix = sha1Hash.substring(0, 5);
            String suffix = sha1Hash.substring(5);

            // Passo 2: Query da API com o prefixo
            URI uri = new URI("https", "api.pwnedpasswords.com", "/range/" + prefix, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(suffix)) {
                    String[] parts = line.split(":");
                    return Integer.parseInt(parts[1].trim());
                }
            }
            reader.close();
            return 0; // se não forem encontradas brechas de segurança
        } catch (Exception e) {
            System.err.println("Error checking password breach: " + e.getMessage());
            return -1;
        }
    }
}