package servico;
import model.Credential;
import utils.InputSanitizer;
import utils.PasswordGenerator;
import org.mindrot.jbcrypt.BCrypt;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
/**
 * Lida com a interação do usuário para gerenciar credenciais, incluindo listar, adicionar, remover, pesquisar, descriptografar e copiar senhas.
 */
public class GerenciadorCredenciais {
	private final List<Credential> credentials;
	private final Scanner ler = new Scanner(System.in);
	/**
	 * Inicializa o gerenciador de credenciais com uma lista de credenciais
	 *
	 * @param credentials As credenciais a serem gerenciadas
	 */
	public GerenciadorCredenciais(List<Credential> credentials) {
		this.credentials = credentials;
	}
	/**
	 * Displays the interactive menu for managing credentials.
	 */
	public void showMenu() {
		while (true) {
			System.out.println("\n=== Credential Manager ===");
			System.out.println("1. Lista todas as credenciais");
			System.out.println("2. Adiciona novas credenciais");
			System.out.println("3. Deleta uma credencial");
			System.out.println("4. Copy password to clipboard");
			System.out.println("5. Check if any password has been compromised");
			System.out.println("6. Exit");
			System.out.print("Choose an option: ");
			String option = ler.nextLine();

			// Menu options handled using a switch expression
			switch (option) {
				case "1" -> listCredentials();
				case "2" -> addCredential();
				case "3" -> removeCredential();
				case "4" -> copyPasswordToClipboard();
				case "5" -> checkCompromisedPasswords();
				case "6" -> {
					saveAndExit();
					return;
				}
				default -> System.out.println("Invalid option. Try again.");
			}
		}
	}
	/**
	 * Lists all stored credentials with index, service name, and username.
	 */
	private void listCredentials() {
		if (credentials.isEmpty()) {
			System.out.println("No credentials stored.");
			return;
		}
		System.out.println("Stored Credentials:");
		for (int i = 0; i < credentials.size(); i++) {
			Credential c = credentials.get(i);
			System.out.printf("%d. Service: %s | Username: %s%n", i + 1, c.serviceName(), c.username());
		}
	}
	/**
	 * Adds a new credential, with an option to generate a secure password.
	 */
	void addCredential() {
    String service;
    String username;
    String choice;

    try {
        System.out.print("Entre com o nome do serviço: ");
        service = InputSanitizer.sanitize(ler.nextLine(), 50, false);

        System.out.print("Entre com o nome do usuário: ");
        username = InputSanitizer.sanitize(ler.nextLine(), 50, false);

        System.out.print("Gerar uma senha forte? (s/n): ");
        choice = InputSanitizer.sanitize(ler.nextLine().toLowerCase(), 1, false);

        // Input validation for user choice
        while (!choice.equals("y") && !choice.equals("n")) {
            System.out.print("Input inválido. Por favor tecle 's' para sim ou 'n' para não: ");
            choice = InputSanitizer.sanitize(ler.nextLine().toLowerCase(), 1, false);
        }
    } catch (IllegalArgumentException ex) {
        System.out.println("Invalid input. " + ex.getMessage());
        return;
    }
    // Define a senha com base na escolha do usuário
    String password;
    if (choice.equals("y")) {
        // Allow the user to define the password length and characteristics
        int passwordLength = askPasswordLength();
        boolean includeUppercase = askIncludeOption("Inclir letras em caixa alta?");

        boolean includeLowercase = askIncludeOption("Inclir letras em caixa baixa??");

        boolean includeNumbers = askIncludeOption("Incluir números?");

        boolean includeSymbols = askIncludeOption("Incluir símbolos?");

        // Validate if at least one option is selected
        if (!includeUppercase && !includeLowercase && !includeNumbers && !includeSymbols) {
            System.out.println("Error: At least one character type must be selected.");
            return;
        }
        password = PasswordGenerator.generate(passwordLength, includeUppercase, includeLowercase, includeNumbers, includeSymbols);

    } else {
        System.out.print("Enter password: ");
        try {
            password = InputSanitizer.sanitize(ler.nextLine(), 64, false);
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid password. " + ex.getMessage());
            return;
        }
    }
    // Encrypt password and store new credential
    try {
        String encryptedPassword = ServicoEncriptacao.encrypt(password);
        credentials.add(new Credential(service, username, encryptedPassword));
        System.out.println("Credential added successfully.");
    } catch (Exception e) {
        System.err.println("Error encrypting password: " + e.getMessage());
    }
}
/**
 * Asks the user for the password length and validates the input.
 *
 * @return The password length.
 */
private int askPasswordLength() {
    int length = 0;
    while (length <= 0) {
        try {
            System.out.print("Enter password length (minimum 8): ");
            length = Integer.parseInt(ler.nextLine());
            if (length < 8) {
                System.out.println("Password length must be at least 8 characters.");
                length = 0;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }
    return length;
}
/**
 * Asks the user whether to include a specific character set in the password.
 *
 * @param message The message to display to the user.
 * @return True if the user wants to include the character set, otherwise false.
 */
private boolean askIncludeOption(String message) {
    while (true) {
        System.out.print(message + " (y/n): ");
        String input = ler.nextLine().toLowerCase();
        if (input.equals("y")) {
            return true;
        } else if (input.equals("n")) {
            return false;
        } else {
            System.out.println("Invalid input. Please enter 'y' for yes or 'n' for no.");
        }
    }
}
	/**
	 * Removes a credential from the list based on user input.
	 */
	void removeCredential() {
		listCredentials();
		if (credentials.isEmpty()) return;
		System.out.print("Enter number to remove: ");
		int index = getIntInput() - 1;

		if (index >= 0 && index < credentials.size()) {
			Credential removed = credentials.remove(index);
			System.out.println("Removed: " + removed.serviceName());
		} else {
			System.out.println("Invalid index.");
		}
	}
	/**
	 * Copies a decrypted password to the clipboard after verifying the master password.
	 */
	private void copyPasswordToClipboard() {
		if (credentials.isEmpty()) {
			System.out.println("No credentials stored.");
			return;
		}
		listCredentials();
		System.out.print("Enter number to copy password: ");
		int index = getIntInput() - 1;

		if (index < 0 || index >= credentials.size()) {
			System.out.println("Invalid index.");
			return;
		}
		System.out.print("Re-enter master password to confirm: ");
		String inputPassword = ler.nextLine().trim();

		try {
			// Load stored hash from a file
			java.nio.file.Path passwordPath = java.nio.file.Paths.get("master_password.dat");
			if (!java.nio.file.Files.exists(passwordPath)) {
				System.out.println("master_password.txt file not found. Please set up your master password again.");
				return;
			}

			String storedHash = java.nio.file.Files.readAllLines(passwordPath).getFirst();
			if (!BCrypt.checkpw(inputPassword, storedHash)) {
				System.out.println("Incorrect master password. Access denied.");
				return;
			}

			// Decrypt and copy password
			Credential selected = credentials.get(index);
			String decrypted = ServicoEncriptacao.decrypt(selected.encryptedPassword());
			copyToClipboard(decrypted);
			System.out.printf("Password for %s copied to clipboard.%n", selected.serviceName());
		} catch (IOException e) {
			System.err.println("Error reading master_password.dat: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Error decrypting password: " + e.getMessage());
		}
	}
	/**
	 * Copies a string to the system clipboard.
	 *
	 * @param text The text to copy.
	 */
	private void copyToClipboard(String text) {
		try {
			StringSelection selection = new StringSelection(text);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, null);
		} catch (Exception e) {
			System.err.println("Clipboard operation not supported: " + e.getMessage());
		}
	}
	/**
	 * Checks all stored passwords against breach data via API.
	 */
	private void checkCompromisedPasswords() {
		if (credentials.isEmpty()) {
			System.out.println("No credentials stored.");
			return;
		}
		System.out.println("Checking all stored passwords for breaches...");
		boolean anyCompromised = false;
		for (Credential c : credentials) {
			try {
				String decrypted = ServicoEncriptacao.decrypt(c.encryptedPassword());
				int count = ChecagemVazamento.checkPassword(decrypted);
				if (count > 0) {
					System.out.printf(
							"WARNING: Password for service '%s' (username: %s) was found %d times in breaches!%n",
							c.serviceName(), c.username(), count
					);
					anyCompromised = true;
				}
			} catch (Exception e) {
				System.err.println("Error checking password for service '" + c.serviceName() + "': " + e.getMessage());
			}
		}
		if (!anyCompromised) {
			System.out.println("No compromised passwords found in your credentials.");
		}
	}
	/**
	 * Saves credentials and exits the application.
	 */
	private void saveAndExit() {
		try {
			CredenciasiArmazenar.saveCredentials(credentials);
			System.out.println("Credentials saved. Exiting...");
		} catch (Exception e) {
			System.err.println("Error saving credentials: " + e.getMessage());
		}
	}
	/**
	 * Reads and validates integer input from the user.
	 *
	 * @return Validated integer or -1 if input is invalid.
	 */
	private int getIntInput() {
		try {
			return Integer.parseInt(ler.nextLine().trim());
		} catch (NumberFormatException e) {
			System.out.println("Invalid input. Please enter a number.");
			return -1;
		}
	}
}