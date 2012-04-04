import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Calendar;

/**
 * Each OBABean is corresponding to a reservation
 * @author Americo Rodriguez
 */
public class OBABean {
	
	private int requestId;	
	private int imageId;
	private String imageName;
	private String username;
	private String password;
	private String ipAddress;
	private Calendar startTime;	
	private Calendar endTime;
	private long duration;	
	private Platform clientPlatform;	
	private boolean isReserved;
	
	/**
	 * Default Constructor
	 */
	public OBABean(){}
	
	/**
	 * Constructor
	 * @param imageId
	 * @param imageName
	 * @param username
	 * @param password
	 * @param ipAddress
	 * @param clientPlatform
	 * @param startTime
	 * @param endTime
	 * @param duration
	 */
	public OBABean(int imageId, String imageName, final String username, final String password, String ipAddress, 
				   Platform clientPlatform, Calendar startTime, Calendar endTime, long duration, boolean isReserved) {
		
		setImageId(imageId);
		setImageName(imageName);
		setUsername(username);
		setPassword(password);
		this.ipAddress = ipAddress;
		setClientPlatform(clientPlatform);
		setStartTime(startTime);
		setEndTime(endTime);
		setDuration(duration);
		this.isReserved = isReserved;
	}
	
	/**
	 * 
	 * @param conn_data
	 */
	private void termLaunch(String[] conn_data) {
		String term = null, ssh_command = null;
		// In order to automatic use ssh to login, we need "sshpass" to provide
		// the password to the shell
		String[] commands = null;
		switch (getClientPlatform()) {
		case Windows:
			term = "./putty.exe";
			ssh_command = getUsername() + "@" + getIpAddress();
			commands = new String[] {term, "-ssh", ssh_command, "-pw", getPassword()};
			break;
		case Linux:
			term = "/usr/bin/gnome-terminal";
			ssh_command = "expect -c 'set password " + getPassword()
					+ "; spawn ssh -o StrictHostKeyChecking=no " + getUsername()
					+ "@" + getIpAddress()
					+ "; expect assword; send \"$password\r\"; interact'";
			commands = new String[] { term, "-e", ssh_command };
			break;
		case Mac:
			// Too many nested script commands, the single/double quotes get
			// mixed together, we have to write the entire script to a file,
			// and use osascript to execute this script file.
			String script = "tell app \"Terminal\"\nActivate\ndo script ";
			script += "\"expect -c 'set password "
					+ getPassword()
					+ "; spawn ssh -o StrictHostKeyChecking=no "
					+ getUsername()
					+ "@"
					+ getIpAddress()
					+ "; expect assword; send \\\"$password\\r\\\"; interact'\"\n";
			script += "end tell";
			
			//write the above commands to a script
			try {
				Writer script_file = new OutputStreamWriter(new FileOutputStream("tmp_script"), "UTF-8");
				script_file.write(script);
				script_file.close();
			} catch (FileNotFoundException e1) {e1.printStackTrace();} 
			  catch (UnsupportedEncodingException e) {e.printStackTrace();}
			  catch (IOException e) {e.printStackTrace();}

			//create the command which will call the script
			commands = new String[] { "osascript", "tmp_script" };
			break;
		case Android:
			break;
		default:
			break;
		}

		Runtime rt = Runtime.getRuntime();
		try {
			System.out.println("Now launch the terminal");
			rt.exec(commands);

			// Delete the temp script file if it exists
			Thread.sleep(1000);
			File tmp_script = new File("tmp_script");
			if (tmp_script.exists()) {
				tmp_script.delete();
			}
		} catch (IOException e) {e.printStackTrace();}
		  catch (InterruptedException e) {e.printStackTrace();}
	}
	
	/**
	 * Launch the RDP client
	 * @param conn_data has the ipAddress, username, and password
	 */
	private void rdpLaunch(String[] conn_data){
		RDP rdp = new RDP();
		
		//Determine which client is executing RDP and adjust the command accordingly
		switch (getClientPlatform()){
		case Windows:
			try{
				rdp.exec(getUsername(), getPassword(), getIpAddress(), Integer.toString(getRequestId()), getClientPlatform());
			} 
			catch (Exception e1) {e1.printStackTrace();}
			break;
		default:
			System.out.println("RDP capability is only available for Windows-based clients!");
		}
	}
	
	/**
	 * Start the connection to the remote host/image
	 */
	public void start() {
		String[] conn_data = {getIpAddress(), getUsername(), getPassword()};
		
		try {
			// Wait for a short time after establishing a reservation and connecting to the host
			// since the VCL needs time to process remote IP in its firewall.
			Thread.sleep(30000); // 30 second wait seems to work well for both RDP and SSH.
		} catch (InterruptedException e) {e.printStackTrace();}

		//Determine if you need to connect to the host via RDP or SSH
		if (RDP.isHostRDPReady(getIpAddress())) {
			rdpLaunch(conn_data);	//use RDP to login to the terminal
		} else {
			termLaunch(conn_data);  //use ssh to login to the terminal
		}
		
		//Signify that the OBA is reserved
		setReservedIndicator(true);
	}


	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}


	/**
	 * @return the requestId
	 */
	public int getRequestId() {
		return requestId;
	}


	/**
	 * @param imageId the imageId to set
	 */
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}


	/**
	 * @return the imageId
	 */
	public int getImageId() {
		return imageId;
	}


	/**
	 * @param imageName the imageName to set
	 */
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}


	/**
	 * @return the imageName
	 */
	public String getImageName() {
		return imageName;
	}


	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}


	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}


	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * @param start the start to set
	 */
	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}


	/**
	 * @return the start
	 */
	public Calendar getStartTime() {
		return startTime;
	}


	/**
	 * @param end the end to set
	 */
	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}


	/**
	 * @return the end
	 */
	public Calendar getEndTime() {
		return endTime;
	}


	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}


	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}


	/**
	 * @param clientPlatform the clientPlatform to set
	 */
	public void setClientPlatform(Platform clientPlatform) {
		this.clientPlatform = clientPlatform;
	}


	/**
	 * @return the clientPlatform
	 */
	public Platform getClientPlatform() {
		return clientPlatform;
	}


	/**
	 * @param isReserved the isReserved to set
	 */
	public void setReservedIndicator(boolean isReserved) {
		this.isReserved = isReserved;
	}


	/**
	 * @return the isReserved
	 */
	public boolean isReserved() {
		return isReserved;
	}


	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	
//	public static void main(String[] args){
//		OBABean oba = new OBABean();
//		
//		oba.setClientPlatform(Platform.Windows);
//		oba.setUsername("arodrig3");
//		oba.setPassword("JHdf9v");
//		oba.setIpAddress("152.1.13.225");
//		
//		oba.setIpAddress("152.1.13.209");
//		oba.setPassword("flipper1");
//		
//		oba.start();
//	}
}