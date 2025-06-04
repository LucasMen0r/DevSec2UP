package model;
/**
 *  São as credenciais salvas do usuário para o serviço.
 */
public record Credential(String serviceName, String username, String encryptedPassword) {
	/**
	 * Constructs a new Credential.
	 *
	 * @param serviceName     nome do serviço (por exemplo, Gmail)
	 * 
	 * @param username        nome de usuário associado ao serviço
	 * 
	 * @param encryptedPassword senha, já devidamente encriptada
	 */
	public Credential {
	}
	@Override
	public String toString() {
		return "Service: " + serviceName + ", Username: " + username;
	}
}