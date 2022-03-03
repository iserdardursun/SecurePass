import java.security.PrivateKey;
import java.security.PublicKey;

public class user {
	private String email;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email=email;
	}
	
	public void setPublic(PublicKey publicKey) {
		this.publicKey=publicKey;
	}
	
	public void setPrivate(PrivateKey privateKey) {
		this.privateKey=privateKey;
	}
	
	public PublicKey getPublicKey(){
		return publicKey;
	}
	
	public PrivateKey getPrivateKey(){
		return privateKey;
	}
	
	public void logout() {
		email = "";
		privateKey = null;
		publicKey = null;
	}
}
