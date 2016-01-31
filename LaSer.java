
import java.io.*;
import java.net.*;

public class LaSer {
	
	public static String userName = null;
	public static String VfDomain = null;
	public static String ssn = null;
	public static int random = 0;
	public static int VFnumPresent = 0;
	public static int LAport = 0;
	public static int VFport = 0;
	public static ServerSocket listen = null;
	public static Socket serSock = null;
	
	public static void main(String[] args) {
		
		/*if(args.length != 3){
			System.out.println("\nNeed 2 parameter, Usage :\n1: LA Port\n3: VF Doamin\n3: VF Port \n");
			System.exit(0);
		}*/
			
		LAport = 8881; //Integer.parseInt(args[0]);
		VfDomain = "localhost"; //args[1];
		VFport = 9991; //Integer.parseInt(args[2]);
		
		try{
				listen = new ServerSocket(LAport);	
				while(true){
					serSock = listen.accept();	
					VerifyVC();
				}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void checkVerifNum(){
		
		try {
				String s = null;
				String replace = new String();
				
				BufferedReader br = new BufferedReader(new FileReader("verify.txt"));
				String fin = new String();
                while((s=br.readLine())!=null){
                	String[] ip = s.split(" ");
	                    if(ip[0].equals(ssn)){
	                    	if(!ip[1].equals("0")){
	                    		random = Integer.parseInt(ip[1]);
	                    		fin = fin.concat(s).concat("\n");
	                    		VFnumPresent = 1;
	                    	}
	                    	else{
	                    			random = RSA.randomNumGen();
	                    			ip[1] =   Integer.toString(random);
	                    			replace = replace.concat(ip[0]).concat(" ").concat(ip[1]);
	                    			fin = fin.concat(replace).concat("\n");
	                    			VFnumPresent = 0;
	                    	}
	                    }
	                    else{
	                    	fin = fin.concat(s).concat("\n");
	                    }
				}
                br.close();        
                BufferedWriter bw = new BufferedWriter(new FileWriter("verify.txt"));
	            bw.write(fin);
	            bw.close();           
			}
			catch(Exception e){
					e.printStackTrace();
			}
	}
	
	public static boolean  VerifyVC(){
		
		boolean verified = false;
		try {
											
				// read received data of client.....
				BufferedReader str = new BufferedReader( new InputStreamReader( (serSock.getInputStream())));	
				PrintWriter out = new PrintWriter(serSock.getOutputStream(), true);		
				
				//Step 3 : Private Key decryption 
				if(isCitizen(RSA.decodeData(str.readLine(), "privKeyLA.txt"))){
					verified = true;
					
					// will get v number if not in file else generate random number
					checkVerifNum(); 
					String zz = "ok" + " "+random;
					String z1 = RSA.encodeData(zz, "pubKeyVT.txt");
					out.println(z1);
			
					//Write Data to VF server if VF number is not present  
					if(VFnumPresent == 0){
						//Step 4 : connect to VF Server
						connectToVFServer();
					}
					
				}
				else{
					verified = false;
					String zz = "no";				
					String z1 = RSA.encodeData(zz, "pubKeyVT.txt");
					out.println(z1);
				}
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
		return verified;
	}
	
	public static void connectToVFServer(){
		
		try {
				Socket conToVF = new Socket(VfDomain, VFport);
				PrintWriter outToVF = new PrintWriter(conToVF.getOutputStream(), true);
				String outD = Integer.toString(random);
				
				// Step 4 : Public key Encryption
				outD = RSA.encodeData(outD, "pubKeyVF.txt");
				// Step 4 : Provide Digital Signatute using private key
				String sign = RSA.digitalSignature(outD, "privKeyLA.txt");				
				// Step 4 : E(priv(LA), E(Pub(VF), vnumber))
				String signedData = "LA "+ sign +" space "+ outD;
				
				outToVF.println(signedData);
				conToVF.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	public static boolean isCitizen(byte[] bytes) {
		
		boolean check = false;			
		try {
				String[] userData = new String(bytes).split(" ");
				System.out.println("User Name : " + userData[0]);
				BufferedReader bf = new BufferedReader(new FileReader("Status.txt"));
				String str1 = new String();
				while((str1 = bf.readLine()) !=null){
						String[] usr = str1.split(" ");
						if(usr[0].equals(userData[0]) && usr[1].equals(userData[1]) && usr[2].equals("citizen")){
							userName = userData[0];
							ssn = userData[1];
							check = true;
							break;
						}
				}	
				bf.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return check; 
	}
}
