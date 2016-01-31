import java.io.*;
import java.net.*;
import java.util.*;


public class VoterCli {
	
	//global variable
	public static int VCport = 0;
	public static String LaDomain = null;
	public static String VfDomain = null;
	public static int VFport = 0;
	public static int choice = 0;
	
	public static void main(String[] args) {

		try{
				/*if(args.length != 5 ){
					System.out.println("\nNeed 5 parameters, Usage :\n1: 0/1 to select LA or VF\n2: LADomain\n3: VFDoamin\n4: LA Port\n5:VF Port \n");
					System.exit(0);
				}*/
			
				choice = 1; //Integer.parseInt(args[0]);
				LaDomain = "localhost"; //args[1];
				VfDomain = "localhost"; //args[2];
				VCport = 8881; //Integer.parseInt(args[3]);
				VFport = 9991; //Integer.parseInt(args[4]);
			
				Scanner in = new Scanner(System.in);
				if( choice == 0){
					
					System.out.println("Userid : ");
					String str1 = in.next();
					System.out.println("SSN : ");				
					String str2 = in.next();
					
					StringBuilder outStr = new StringBuilder();
					Socket client = new Socket(LaDomain,VCport);
					PrintWriter outToserver = new PrintWriter(client.getOutputStream(), true);
						
						
						//change keys related to user 
						String pubKeyF = str1+"pubKey.txt";
						String privKeyF = str1+"privKey.txt";
						RSA.updatePubKey(RSA.PublicKeyReader(pubKeyF), "pubKeyVT.txt");
						RSA.updatePrivKey(RSA.PrivateKeyReader(privKeyF), "privKeyVT.txt");
						
						//Data to Server
						outStr.append(str1).append(" ").append(str2);
						
						//Step 2 : Public Encryption 
						String zz = RSA.encodeData(outStr.toString(), "pubKeyLA.txt");
						outToserver.println(zz);
				
						//Data From Server LA
						BufferedReader inputFromserver  = new BufferedReader (new InputStreamReader (client.getInputStream()));
						String ipSrv = inputFromserver.readLine();
						byte[] ip = RSA.decodeData(ipSrv, "privKeyVT.txt");
						ipSrv = new String(ip);
						String [] ipArray = ipSrv.split(" ");
						
						//Step 5 : Decision of LA 
						if(ipArray[0].equals("ok"))
								System.out.println("VF Number : " + ipArray[1]);
						else
								System.out.println("You're NOT eligible to vote");				
						client.close();
				}
				else{
						// Step 7 and 8 : Connect to VF server
						System.out.println("VF Num : ");
						String str1 = in.next();
						Socket client = new Socket(VfDomain,VFport);
						PrintWriter outToserver = new PrintWriter(client.getOutputStream(), true);
						
	
						//Step 9 : Data to Server using public key encryption
						String zz = RSA.encodeData(str1, "pubKeyVF.txt");
						outToserver.println("VC "+ zz);
				
						//Data From Server
						BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));						
						String servResponce = br.readLine();
						
						// Step 10 
						if(servResponce.equals("invalid")){
							System.out.println("Invalid Verification");
							// terminate the connection
						}else {
							
							String choice = new String(); 
							while(!choice.equals("4")){
								System.out.println("\nPlease enter a number (1-4)");
								System.out.println("1. Vote\n2. My vote history\n3. View latest result\n4. Quit");
								choice = in.next();
								if(!choice.equals("4")){
									// Step 11 : Voting
									if(choice.equals("1")){
										System.out.println("Please enter a number (1-2)\n1. Bob\n2. John");
										String vote = in.next();
										// Step 12 : Public key encryption of Vote
										vote = RSA.encodeData(vote, "pubKeyVF.txt");
										outToserver.println("v "+ vote);											
									}else if(choice.equals("2")){
										outToserver.println("nv "+ choice);
										System.out.println("Voted at : "+ br.readLine());
									}else if(choice.equals("3")){
										outToserver.println("nv "+ choice);
										System.out.println("History : "+ br.readLine());
									}									
								}								
							}// end while
							if(choice.equals("4")){
								outToserver.println("nv "+ choice);
							}							
						}
						client.close();					
				}
				in.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}	
	}
}
