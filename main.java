import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.mysql.cj.xdevapi.Statement;


public class main {
    public static String getMd5(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } 
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static KeyPair getKeyPair(String masterPass) throws NoSuchAlgorithmException, NoSuchProviderException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        random.setSeed(md.digest(masterPass.getBytes()));
        keyGen.initialize(1024, random);
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }
    
    public static String encryptRSA(String message, PublicKey publickey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
    {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publickey);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(messageBytes);
        String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        return encodedMessage;
    }
    
    public static String decryptRSA(String message, PrivateKey privatekey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
    {
    	byte[] encryptedMessageBytes = Base64.getDecoder().decode(message.getBytes("UTF-8"));
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privatekey);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        return decryptedMessage;
    }

	public static void main(String[] args) throws Exception {
        database database = new database();
        database.readDataBase();
        user User = new user();
		boolean mainMenuBool = true;
		boolean loggedInBool = true;
		boolean setpass = true;
		boolean getpass = true;
		mainmenu:
		while(mainMenuBool) {
			System.out.println("--------------------------");
			System.out.println("Welcome to the SecurePass.");
			System.out.println();
			System.out.println("1 - Login");
			System.out.println("2 - Register");
			System.out.println();
			System.out.println("0 - Exit");
			System.out.println("--------------------------");
			System.out.print("Your Choice: ");
			try {
				Scanner sc= new Scanner(System.in);
				int enterValueMainMenu = sc.nextInt();
				if(enterValueMainMenu == 1) {
					System.out.print("email: ");
					sc= new Scanner(System.in);
					String email = sc.nextLine();
					System.out.print("Password: ");
					String password = sc.nextLine();
					if(database.loginSuccess(email,getMd5(password))) {
						System.out.println("You successfully logged in.");
						User.setEmail(email);
						KeyPair keypair = getKeyPair(password);
						User.setPublic(keypair.getPublic());
						User.setPrivate(keypair.getPrivate());
						while(loggedInBool) {				
						System.out.println("--------------------------");
						System.out.println();
						System.out.println("1 - Get passwords");
						System.out.println("2 - Set Password for a new app");
						System.out.println();
						System.out.println("9 - Logout");
						System.out.println("0 - Exit");
						System.out.println("--------------------------");
						System.out.print("Your Choice: ");
						try {
						Scanner loggedIn= new Scanner(System.in);
						int enterValueLoggedIn = loggedIn.nextInt();
						if(enterValueLoggedIn == 1) {
							HashMap<String,String> appData = database.getPasswords(User.getEmail());
							System.out.println("--------------------------");
					        for (String i : appData.keySet()) {
					            System.out.println("App: " + i + " Password: " + decryptRSA(appData.get(i),User.getPrivateKey()));
					        }
							System.out.println("--------------------------");
							
							
							System.out.println("Press Enter To Continue...");
							Scanner entertocontinueScanner= new Scanner(System.in);
							String entertocontinue = entertocontinueScanner.nextLine();
						}
						else if(enterValueLoggedIn == 2){
							System.out.println("App Name:");
							Scanner setapp= new Scanner(System.in);
							String appName = setapp.nextLine();
							System.out.println("App Pass:");
							String appPass = setapp.nextLine();
							database.setAppPassword(User.getEmail(), appName, encryptRSA(appPass, User.getPublicKey()));
						}
						else if(enterValueLoggedIn == 9){
							System.out.println("Successfully Logged Out.");
							User.logout();
							break;
						}
						else if(enterValueLoggedIn == 0){
							System.out.println("Successfully Exit.");
							User.logout();
							database.close();
							break mainmenu;
						}
						} catch (InputMismatchException e) {
							System.out.println("You need to enter an Integer.");
						}
					}
					}
					
				}
				else if(enterValueMainMenu == 2) {
					System.out.print("email: ");
					sc= new Scanner(System.in);
					String email = sc.nextLine();
					System.out.print("Password: ");
					String password = sc.nextLine();
					System.out.print("Password Again: ");
					String password2 = sc.nextLine();
					if(password.equals(password2))
			        database.register(email,getMd5(password));					
				}			
				else if(enterValueMainMenu == 0) {
					System.out.println("Successfully Exit.");
					database.close();
					break;
				}
			} catch (InputMismatchException e) {
				System.out.println("You need to enter an Integer.");
			}
			
		}
	}

}
