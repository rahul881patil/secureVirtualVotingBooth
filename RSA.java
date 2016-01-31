import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;


public class RSA {

	public static String ALGORITHM = "RSA";
	public static final String DigiSig = "SHA1withRSA";

	// Public Encryption 
	public static byte[] pubEncrypt(String plnText, PublicKey key){
		 
		 byte[]cipherText = null;
		  try {		      
		      		final Cipher cipher = Cipher.getInstance(ALGORITHM);
		      		cipher.init(Cipher.ENCRYPT_MODE, key);
		      		cipherText = cipher.doFinal(plnText.getBytes());
		  } 
		  catch (Exception e) {
			  e.printStackTrace();
		  }
		  return cipherText;
	 }
	 
	// Decryption using private key
	 public static byte[] privDecrypt(byte[] text, PrivateKey key) {

		 byte[] plainText = null;
		 try {
			 		final Cipher cipher = Cipher.getInstance(ALGORITHM);
			 		cipher.init(Cipher.DECRYPT_MODE, key);
			 		plainText = cipher.doFinal(text);
		 } 
		 catch (Exception ex) {
	    			ex.printStackTrace();
		 }
	 	return plainText;
	 }
	 
	 // Generate Digital Signature
	 public static String digitalSignature(String txt, String fname){
	 
		 byte[] ip = DatatypeConverter.parseHexBinary(txt);
		 String signature = null;		 
		 try{
			 		Signature instance = Signature.getInstance(DigiSig);
			 		instance.initSign(PrivateKeyReader(fname));
			 		instance.update(ip);
			 		byte[] zz = instance.sign();
			 		signature = DatatypeConverter.printHexBinary(zz);					
		 }
		 catch(Exception e){
			 e.printStackTrace();
		 }
	 	 return signature;
	 }

	 // Verification of digital signature
	 public static boolean verifyDigSig(String signature, String ip, String fname){
		 
		 byte[] digiSig = DatatypeConverter.parseHexBinary(signature); 
		 byte[] msg = DatatypeConverter.parseHexBinary(ip);
		 boolean verify = false;
		 try{
			 		Signature instance = Signature.getInstance(DigiSig);
			 		instance.initVerify(PublicKeyReader(fname));
			 		instance.update(msg);
			 		verify = instance.verify(digiSig);			
		 }
		 catch(Exception e){
			 e.printStackTrace();
		 }
		 return verify;
	 }
	 
	 // load user specific public key 
	 public static void updatePubKey(PublicKey key, String fname){
		
		 try {
			 	FileOutputStream pubOut = new FileOutputStream(fname);
			 	pubOut.write(key.getEncoded());
			 	pubOut.close();
		 }
		 catch(Exception e){
			 e.printStackTrace();
		 }
	 }
	 
	 // load user specific private key
	 public static void updatePrivKey(PrivateKey key, String fname){
			
		 try {
			 	FileOutputStream privOut = new FileOutputStream(fname);
			 	privOut.write(key.getEncoded());
			 	privOut.close();
		 }
		 catch(Exception e){
			 e.printStackTrace();
		 }
	 }

	 // load public key for use
	 public static PublicKey PublicKeyReader(String filename) {

		 	PublicKey key =null;
		    try{
		    	
		    		File f = new File(filename);
		    		FileInputStream fis = new FileInputStream(f);
		    		DataInputStream dis = new DataInputStream(fis);
		    		byte[] keyBytes = new byte[(int)f.length()];
		    		dis.readFully(keyBytes);
		    		dis.close();
		    		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		    		KeyFactory kf = KeyFactory.getInstance("RSA");
		    	 	key =  kf.generatePublic(spec);
		  }
		  catch(Exception e)
		  {
			  		e.printStackTrace();
		  }
		    return key;
	 }
	 
	 // load private key for use
	 public static PrivateKey PrivateKeyReader(String filename) {
	 
		 PrivateKey key =null;
		 try{
		    
			 		File f = new File(filename);
			 		FileInputStream fis = new FileInputStream(f);
			 		DataInputStream dis = new DataInputStream(fis);
			 		byte[] keyBytes = new byte[(int)f.length()];
			 		dis.readFully(keyBytes);
			 		dis.close();

			 		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			 		KeyFactory kf = KeyFactory.getInstance("RSA");
			 		key = kf.generatePrivate(spec);
		  }
		  catch(Exception e)
		  {
			  		e.printStackTrace();
		  }
		  return key;
	 }

	 
	 // Decode data and convert to usable format
	 public static byte[] decodeData(String str , String fileName){
			
			byte[] bytes = DatatypeConverter.parseHexBinary(str);
			bytes = RSA.privDecrypt(bytes, RSA.PrivateKeyReader(fileName));
			return bytes;
		}

	// Encode data and convert to usable format
	public static String encodeData(String str , String fileName){
			
			byte[] bytes = RSA.pubEncrypt(str,RSA.PublicKeyReader(fileName));
			String zz = DatatypeConverter.printHexBinary(bytes);
			return zz;
		}

	// Random 8 digit number generator
	public static int randomNumGen(){
		
		Random random=new Random();
	    int randomNumber=0;
	    boolean loop=true;
	    while(loop) {
	        randomNumber=random.nextInt();
	        if(Integer.toString(randomNumber).length()==8 && !Integer.toString(randomNumber).startsWith("-")) {
	            loop=false;
	        }
	    }
		return randomNumber;
		
	}
	
	
}
