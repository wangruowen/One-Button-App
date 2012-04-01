import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class RDP {
	private final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private final String RDP_TEMPLATE_FILE = "rdp_template.txt";
	private final String RDP_ENCRYPT_UTIL = "cryptRDP5.exe";
	private static final int DEFAULT_RDP_PORT = 3389;
	
	/**
	 * Encrypt the plaintext password. This is needed to ensure full login automation of the RDP deployment on Windows
	 * @param plainTextPwd
	 * @return encrypted pwd
	 */
	private String encryptRDPPwd(String plainTextPwd){
		Runtime rt = Runtime.getRuntime();
		BufferedReader output = null;
		Process proc = null;
		String encryptedPwd = "";
		
		try {
			//call a third-party app and encrypt the RDP plaintext password
			proc = rt.exec("." + File.separator + RDP_ENCRYPT_UTIL + " " + plainTextPwd);
		    output = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		    encryptedPwd = output.readLine();
		} catch (IOException e) {e.printStackTrace();}
		
		return encryptedPwd;
	}
 
	/**
	 * Compose the RDP command which will be platform dependent
	 * @param user
	 * @param requestID
	 * @param platform
	 * @return
	 */
	private String getRDPCommand(String user, String requestID, Platform platform){
		String rdp_command = "";
		
		//Determine which client is executing RDP and adjust the command accordingly
		switch (platform){
		case Windows:
			rdp_command = "cmd /c " + TEMP_DIR + user + "_" + requestID + ".rdp";
			break;
		default:
			rdp_command = "";
		}
		
		return rdp_command;
	}
	
	/**
	 * Use the Window's-based RDP template to create an RDP file
	 * @param user
	 * @param password
	 * @param ipAddress
	 */
	private void createRDPFromTemplate(String user, String password, String ipAddress, String requestID){
		//write the new rdp template file
		FileWriter fstream = null;

		try {
			fstream = new FileWriter(TEMP_DIR + user + "_" + requestID + ".rdp");
		} catch (IOException e) {e.printStackTrace();}
		
		BufferedWriter out = new BufferedWriter(fstream);

		try {
			Scanner rdpTemplate = new Scanner (new File("." + File.separator + RDP_TEMPLATE_FILE));
			while(rdpTemplate.hasNextLine()){
				String line = rdpTemplate.nextLine();
				String newLine = "";
				
				//replace the keywords in the template with actual user values
				if(line.indexOf("<<USERNAME>>") > 0)
					newLine = line.replaceAll("<<USERNAME>>", user);
				else if(line.indexOf("<<PASSWORD>>") > 0)
					newLine = line.replaceAll("<<PASSWORD>>", encryptRDPPwd(password));
				else if(line.indexOf("<<IPADDRESS>>") > 0)
					newLine = line.replaceAll("<<IPADDRESS>>", ipAddress);
				else
					newLine = line;
				
				out.write(newLine + "\n");
					out.flush();				
			}

			out.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	/**
	 * Actually execute the RDP command
	 * @param rdp_command
	 */
	private void execRDPCommand(String rdp_command){
		
		System.out.println("Now launching RDP...");
			try {
				Runtime rt = Runtime.getRuntime();
				rt.exec(rdp_command);
			}
			catch (Exception e) {e.printStackTrace();}			
		System.out.println("RDP launch complete!");
	}

	/**
	 * Exec the RDP connection for the first time. Notice, password and ip address must be provided
	 * @param user
	 * @param password
	 * @param ipAddress - this is the host's outward facing IP address. NOT the local-based ip address.
	 */
	public void exec(String user, String password, String ipAddress, String requestID, Platform platform){
		System.out.println(TEMP_DIR);
		
		//create an RDP file for this reservation
		createRDPFromTemplate(user, password, ipAddress, requestID);
	
		execRDPCommand(getRDPCommand(user, requestID, platform));
	}
	
	/**
	 * exec RDP command which can be called when the RDP file has already been initially created for the OBA
	 * @param user
	 * @param requestID
	 * @param platform
	 */
	public void exec(String user, String requestID, Platform platform){
		File rdpFile = new File(TEMP_DIR + user + "_" + requestID + ".rdp");
		
		//Exit if the rdp does not exist
		if(!rdpFile.exists()){
			System.out.println("Missing RDP File!");
			System.exit(1);
		}else{
			execRDPCommand(getRDPCommand(user, requestID, platform));
		}
	}
	
	/**
	 * Determine if the host is ready for an RDP connection
	 * @param ipAddress
	 * @return
	 */
	public static boolean isHostRDPReady(String ipAddress){
		try {
			@SuppressWarnings("unused")
			Socket socket = new Socket(ipAddress, DEFAULT_RDP_PORT);
		}catch(Exception e) {
			return false;
		}
		
		return true;
	}
}