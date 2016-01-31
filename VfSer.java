
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.*;

public class VfSer {
	
	public static ServerSocket socket;
	public static int port = 0;
	public static int isclosed = 0;

	public static void main(String[] args) {

		try {					
				/*if(args.length != 1){
					System.out.println("\nNeed 1 parameter, Usage :\n1: VF Port \n");
					System.exit(0);
				}*/	
			
				port = 9991; //Integer.parseInt(args[0]);			
				socket = new ServerSocket(port);	
				while(true){
					Socket s = socket.accept();
					(new Thread(new ClientHandler(s))).start();					
				}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class ClientHandler implements Runnable {
		Socket socket;
		BufferedReader in;

		ClientHandler(Socket s) {
			socket = s;
		}

		@Override
		public void run() {
	
			try {			
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String ipFromLA = in.readLine();
					String[] ipFLA = ipFromLA.split(" ");
					if(ipFLA[0].equals("LA")){
						// verify the signature of LA
						if(RSA.verifyDigSig(ipFLA[1], ipFLA[3], "pubKeyLA.txt")){
							// Step 6 : Update the VF Number
							fileWrite(new String (RSA.decodeData(ipFLA[3], "privKeyVF.txt")));
						}
						else{
							System.out.println("Signature is not able to be verified..");
						}
					}
					if(ipFLA[0].equals("VC")){
						String vfNum = new String(RSA.decodeData(ipFLA[1], "privKeyVF.txt"));
						PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
						
						if(checkVFnum(vfNum)){
							// check person has voted or not
							if(checkVoted(vfNum))
								pr.println("voted");
							else								
								pr.println("notvoted");
								// and display drop down
							
							do{
									String check = in.readLine();
									String[] sp = check.split(" ");
									if(sp[0].equals("nv")){
										switch(Integer.parseInt(sp[1])){
											
												case 2 : // Step 13 : My vote history
															pr.println(getHistory(vfNum));
															break;
												case 3 : // Step 14 : view the latest result 
															pr.println(getResult());
															break;
												case 4 : // Step 15 : Close the connection
															socket.close();
															isclosed = 1;
															break;
											}
									}else{
												// Step 12 : accept vote with encryption 
												String Vote = new String (RSA.decodeData(sp[1], "privKeyVF.txt"));
												// Step 12 : update the vote only if person has not voted
												if(!checkVoted(vfNum))
													updateVote(Vote, vfNum);
										}
									}while(isclosed != 1);	
						}	
						// Step 10 : Send invalid VF number
						else {
							pr.println("invalid");
							socket.close();
						}
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void updateVote(String vote, String vfNum){
		try{
			// update result.txt 
			String fn = new String();
			BufferedReader bf = new BufferedReader(new FileReader("Result.txt"));
			String line = new String();
			while((line = bf.readLine())!= null){
				String[] splitLn = line.split(" ");
				if( vote.equals("1") && splitLn[0].equals("Bob")){
					int voted = Integer.parseInt(splitLn[1]) + 1;
					fn = fn.concat(splitLn[0]).concat(" ").concat(Integer.toString(voted).concat("\n"));
				}else if( vote.equals("2") && splitLn[0].equals("John")){
					int voted = Integer.parseInt(splitLn[1]) + 1;
					fn = fn.concat(splitLn[0]).concat(" ").concat(Integer.toString(voted).concat("\n"));
				}else
					fn = fn.concat(line).concat("\n");
			}
			bf.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter("Result.txt"));
            bw.write(fn);
            bw.close();
            
            // Step 12 : update voternumber.txt
            fn = new String();
            bf = new BufferedReader(new FileReader("Voternumber.txt"));
            while((line = bf.readLine()) != null){
            	String[] splitLn = line.split(" ");
            	if(splitLn[0].equals(vfNum)){
            		splitLn[1] = "1";
            		fn = fn.concat(splitLn[0]).concat(" ").concat(splitLn[1]).concat("\n");
            	}
            	else
            		fn = fn.concat(line).concat("\n");
            }
            bf.close();
            bw = new BufferedWriter(new FileWriter("Voternumber.txt"));
            bw.write(fn);
            bw.close();
           
            // Step 12 : update History.txt
            fn = new String();
            bf = new BufferedReader(new FileReader("History.txt"));
            while((line = bf.readLine()) != null){
            		fn = fn.concat(line).concat("\n");
            }
            fn = fn.concat(vfNum).concat(" ").concat(getTime()).concat("\n"); 
            bf.close();
            
            bw = new BufferedWriter(new FileWriter("History.txt"));
            bw.write(fn);
            bw.close();
    
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String getResult(){
		
		String result = new String();
		String line = new String();
		try{
				BufferedReader bf = new BufferedReader(new FileReader("Result.txt"));
				while((line = bf.readLine()) != null){
					result = result.concat(line).concat(", ");
				}
				bf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
        return result;
	}
	
	public static String getHistory(String vfNum){
		
        String fn = new String();
        String line = new String();
        try{ 
        	BufferedReader bf = new BufferedReader(new FileReader("History.txt"));
        	while((line = bf.readLine()) != null){
        		String splitLn[] = line.split(" ");
        		if(splitLn[0].equals(vfNum)){
        			fn = splitLn[1] +" "+splitLn[2];
        			break;
        		}
        	}
        	bf.close();
        }catch(Exception e){
        	e.printStackTrace();
        }
		return fn;
	}
	
	public static String getTime(){
	
    	return (new SimpleDateFormat("yyyy/MM/dd HH-mm-ss").format(Calendar.getInstance().getTime()));
	}
	
	public static boolean checkVFnum(String num){
		
		boolean check = false;
		try{
			BufferedReader bf = new BufferedReader(new FileReader("Voternumber.txt"));
			String line = new String();
			while((line = bf.readLine())!= null){
				String[] splitLn = line.split(" ");
				if(splitLn[0].equals(num)){
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
	
	public static boolean checkVoted(String vfNum){
		
		boolean check = false;
		try{
			BufferedReader bf = new BufferedReader(new FileReader("Voternumber.txt"));
			String line = new String();
			while((line = bf.readLine())!= null){
				String[] splitLn = line.split(" ");
				if(splitLn[0].equals(vfNum) && splitLn[1].equals("1")){
					check = true;
					break;
				}
			}
			bf.close();
		}catch(Exception e){
				e.printStackTrace();
		}
		return check;
	}
	
	public static void fileWrite(String vfNum ){
		
		BufferedWriter bf = null;

		try {
				File file = new File("Voternumber.txt");
				// Step 6 : create a file if does not exist
				if(!file.exists()){
					file.createNewFile();
					bf = new BufferedWriter(new FileWriter(file));
					String data = new String();
					data = vfNum + " " + "0";
					bf.write(data);
					bf.close();
				}				
				else{
					String fl = new String();
					String line = new String();
					BufferedReader br = new BufferedReader(new FileReader(file));
					while((line = br.readLine())!=null)
						fl = fl.concat(line).concat("\n");	
					line = vfNum + " " + "0";
					fl = fl.concat(line).concat("\n");
					bf = new BufferedWriter(new FileWriter(file));
					bf.write(fl);
					bf.close();
					br.close();
				}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	
}