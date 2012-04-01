import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;

/**
 * each OBABean is corresponding to a reservation
 * @author Minh Tuan PHAM
 *
 */
public class OBABean {

	private int id;
	
	private int request_id;
	
	private int image_id;

	private String image_name;

	private String username;

	private String password;

	private String start;
	
	private String end;

	private String duration;

	private int initial_loading_time;
	
	private Platform client_plat;

	private String startTime;

	private int active_req_id;
	
	private boolean isReserved;
	
	public OBABean(int image_id, String image_name, final String username,
			final String password, Platform client_plat, String startTime,
			String duration, boolean isReserved) {
		this.image_id = image_id;
		this.image_name = image_name;
		this.username = username;
		this.password = password;
		this.client_plat = client_plat;
		this.active_req_id = -1;
		this.initial_loading_time = -1;
		this.startTime = startTime;
		this.duration = duration;
		this.isReserved = isReserved;
	}
	
	/*
	 * This method make a reservation for this OBABean
	 * using the methods in the connector
	 * 
	 * @param	The connector is passed by the controller when we want to make a reservation for this OBABean
	 * @return 
	 */
	public boolean makeReservation(OBALogic VCLConnector) {
		/*
		String[] params = new String[3];

		params[0] = Integer.toString(image_id);
		params[1] = startTime;
		params[2] = duration; // Resever for one hour

		HashMap result = (HashMap) xmlRPCcall("XMLRPCaddRequest", params);

		boolean success_in_resv = false;
		if (result.get("status").equals("success")) {
			int request_id = Integer.parseInt((String) result.get("requestid"));
			this.active_req_id = request_id;
			System.out
					.println("Succeed in making the reservation, request id is: "
							+ request_id);

		} else {
			System.err.println("Fail to make a reservation.");
		}

		return success_in_resv;
		*/
		return false;
	}
	
	/**
	 * 
	 * @param VCLConnector
	 * @return
	 */
	public boolean endReservation(OBALogic VCLConnector) {
		return false;
	}
	
	/**
	 * Start the OBA
	 */
	public void start() {
		// TestOBA oba = new TestOBA(args[0], args[1], platform);

		// oba.getImageID();
		//if (makeReservation()) {
			//String[] conn_data = getConnectData();

			// Now launch a Linux terminal to SSH to the reserved machine.
			try {
				// Wait for a short time between getConnectData and termLaunch,
				// since the VCL needs time to process
				// remote IP in its firewall.
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//termLaunch(conn_data);
			// oba.cancelReservation();
		//}
	}
	
	/**
	 * 
	 * @param conn_data
	 */
	public void termLaunch(String[] conn_data) {
		String term = null, ssh_command = null;
		// In order to automatic use ssh to login, we need "sshpass" to provide
		// the password to the shell
		String[] commands = null;
		switch (this.client_plat) {
		case Windows:
			term = "./putty.exe";
			ssh_command = conn_data[1] + "@" + conn_data[0];
			commands = new String[] { term, "-ssh", ssh_command, "-pw",
					conn_data[2] };
			break;
		case Linux:
			term = "/usr/bin/gnome-terminal";
			ssh_command = "expect -c 'set password " + conn_data[2]
					+ "; spawn ssh -o StrictHostKeyChecking=no " + conn_data[1]
					+ "@" + conn_data[0]
					+ "; expect assword; send \"$password\r\"; interact'";
			commands = new String[] { term, "-e", ssh_command };
			break;
		case Mac:
			// Too many nested script commands, the single/double quotes get
			// mixed together, we have to write the entire script to a file,
			// and use osascript to execute this script file.
			String script = "tell app \"Terminal\"\nActivate\ndo script ";
			script += "\"expect -c 'set password "
					+ conn_data[2]
					+ "; spawn ssh -o StrictHostKeyChecking=no "
					+ conn_data[1]
					+ "@"
					+ conn_data[0]
					+ "; expect assword; send \\\"$password\\r\\\"; interact'\"\n";
			script += "end tell";
			try {
				Writer script_file = new OutputStreamWriter(
						new FileOutputStream("tmp_script"), "UTF-8");
				script_file.write(script);
				script_file.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			commands = new String[] { "osascript", "tmp_script" };
			break;
		case Android:

			break;

		default:
			break;
		}
		// System.out.println(ssh_command);

		// Using string array is due to the requirement of the argument accepted
		// by rt.exec.
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getImage_id() {
		return this.image_id;
	}

	public String getImage_name() {
		return this.image_name;
	}

}
