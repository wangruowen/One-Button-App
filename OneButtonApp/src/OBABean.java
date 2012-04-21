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
 * 
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
	private int duration;
	private Platform clientPlatform;
	private boolean isReserved;
	private int initialLoadingTime;
	private int tmp_login_mode;
	private int status;
	private boolean is_autoextend;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	private OBAEntry ownerEntry;

	public static final int SSH_LOGIN = 0;
	public static final int RDP_LOGIN = 1;

	public static final int UNKNOWN_STATUS = 0;
	public static final int READY = 1;
	public static final int LOADING = 2;
	public static final int TIMEDOUT = 3;
	public static final int FAILED = 4;
	public static final int FUTURE = 5;

	/**
	 * Default Constructor
	 */
	public OBABean() {
	}

	/**
	 * Constructor
	 * 
	 * @param imageId
	 * @param imageName
	 * @param username
	 * @param password
	 * @param requestId
	 * @param ipAddress
	 * @param clientPlatform
	 * @param startTime
	 * @param endTime
	 * @param duration
	 */
	public OBABean(int imageId, String imageName, final String username,
			final String password, int requestId, String ipAddress,
			Platform clientPlatform, Calendar startTime, Calendar endTime,
			int duration, int status, boolean isReserved, OBAEntry ownerEntry,
			boolean auto_extend) {

		setImageId(imageId);
		setImageName(imageName);
		setUsername(username);
		setPassword(password);
		setRequestId(requestId);
		this.ipAddress = ipAddress;
		setClientPlatform(clientPlatform);
		setStartTime(startTime);
		setEndTime(endTime);
		setDuration(duration);
		setStatus(status);
		this.isReserved = isReserved;
		this.initialLoadingTime = -1;
		this.ownerEntry = ownerEntry;
		if (ownerEntry != null) {
			this.tmp_login_mode = ownerEntry.getLoginMode();
		} else {
			this.tmp_login_mode = -1;
		}
		setIs_autoextend(auto_extend);
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
			commands = new String[] { term, "-ssh", ssh_command, "-pw",
					getPassword() };
			break;
		case Linux:
			term = "/usr/bin/gnome-terminal";
			ssh_command = "expect -c 'set password " + getPassword()
					+ "; spawn ssh -o StrictHostKeyChecking=no "
					+ getUsername() + "@" + getIpAddress()
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

			// write the above commands to a script
			try {
				Writer script_file = new OutputStreamWriter(
						new FileOutputStream("tmp_script"), "UTF-8");
				script_file.write(script);
				script_file.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// create the command which will call the script
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launch the RDP client
	 * 
	 * @param conn_data
	 *            has the ipAddress, username, and password
	 */
	private void rdpLaunch(String[] conn_data) {
		RDP rdp = new RDP();

		// Determine which client is executing RDP and adjust the command
		// accordingly
		switch (getClientPlatform()) {
		case Windows:
			try {
				rdp.exec(getUsername(), getPassword(), getIpAddress(),
						Integer.toString(getRequestId()), getClientPlatform());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		default:
			System.out
					.println("RDP capability is only available for Windows-based clients!");
		}
	}

	/**
	 * Start the connection to the remote host/image
	 */
	public void start() {
		try {
			// Wait for a short time after establishing a reservation and
			// connecting to the host
			// since the VCL needs time to process remote IP in its firewall.
			Thread.sleep(5000); // 30 second wait seems to work well for both
								// RDP and SSH.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		directStart();
	}

	public void directStart() {
		String[] conn_data = { getIpAddress(), getUsername(), getPassword() };

		if (this.tmp_login_mode == SSH_LOGIN) {
			termLaunch(conn_data);
		} else if (this.tmp_login_mode == RDP_LOGIN) {
			rdpLaunch(conn_data);
		} else {
			// Determine if you need to connect to the host via RDP or SSH
			if (RDP.isHostRDPReady(getIpAddress())) {
				this.tmp_login_mode = RDP_LOGIN;
				this.ownerEntry.setLoginMode(RDP_LOGIN);
				rdpLaunch(conn_data); // use RDP to login to the terminal
			} else {
				this.tmp_login_mode = SSH_LOGIN;
				this.ownerEntry.setLoginMode(SSH_LOGIN);
				termLaunch(conn_data); // use ssh to login to the terminal
			}
		}

		// Signify that the OBA is reserved
		setReservedIndicator(true);
	}

	/**
	 * @param requestId
	 *            the requestId to set
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
	 * @param imageId
	 *            the imageId to set
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
	 * @param imageName
	 *            the imageName to set
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
	 * @param username
	 *            the username to set
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
	 * @param password
	 *            the password to set
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
	 * @param start
	 *            the start to set
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
	 * @param end
	 *            the end to set
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
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param clientPlatform
	 *            the clientPlatform to set
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
	 * @param isReserved
	 *            the isReserved to set
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
	 * @param ipAddress
	 *            the ipAddress to set
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

	public int getInitialLoadingTime() {
		return initialLoadingTime;
	}

	public void setInitialLoadingTime(int remain_time) {
		this.initialLoadingTime = remain_time;
	}

	public int getLogin_mode() {
		return tmp_login_mode;
	}

	public void setLogin_mode(int login_mode) {
		this.tmp_login_mode = login_mode;
	}

	public OBAEntry getOwnerEntry() {
		return ownerEntry;
	}

	public void setOwnerEntry(OBAEntry ownerEntry) {
		this.ownerEntry = ownerEntry;
		this.tmp_login_mode = ownerEntry.getLoginMode();
	}

	public boolean isIs_autoextend() {
		return is_autoextend;
	}

	public void setIs_autoextend(boolean is_autoextend) {
		this.is_autoextend = is_autoextend;
	}
}
