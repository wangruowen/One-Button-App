import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcCommonsTransport;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransport;

public class TestOBA {
	public enum Platform {
		Windows, Linux, Mac, Android, iPhone, iPad
	}

	private int image_id;

	private Platform client_plat;

	private String username;

	private String password;

	private String startTime;

	private String duration;

	private ArrayList<Integer> activeRequests;

	private XmlRpcClient client;

	public TestOBA(int image_id, final String username, final String password,
			Platform client_plat, String startTime, String duration) {
		this.image_id = image_id;
		this.username = username;
		this.password = password;
		this.client_plat = client_plat;
		this.activeRequests = new ArrayList<Integer>();
		this.startTime = startTime;
		this.duration = duration;

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(
					"https://vcl.ncsu.edu/scheduling/index.php?mode=xmlrpccall"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		this.client = new XmlRpcClient();
		this.client.setConfig(config);

		// implement transport factory
		XmlRpcCommonsTransportFactory xmlRpcTransportFactory = new XmlRpcCommonsTransportFactory(
				client) {
			@Override
			public XmlRpcTransport getTransport() {
				return new XmlRpcCommonsTransport(this) {
					@Override
					protected void initHttpHeaders(XmlRpcRequest pRequest)
							throws XmlRpcClientException {
						super.initHttpHeaders(pRequest);
						// add custom header, with UnityID authentication
						super.setRequestHeader("X-User", username);
						super.setRequestHeader("X-Pass", password);
						super.setRequestHeader("X-APIVERSION", "2");
					}
				};
			}
		};
		client.setTransportFactory(xmlRpcTransportFactory);
	}

	private Object xmlRPCcall(String op_name, Object[] params) {
		Object result = null;

		try {
			result = client.execute(op_name, params);

			if (result instanceof HashMap) {
				// do nothing
				return (HashMap) result;
			} else {
				Object[] result_array = (Object[]) result;
				for (int i = 0; i < result_array.length; i++)
					System.out.println(result_array[i]);
				return result_array;
			}

		} catch (XmlRpcException e) {
			e.printStackTrace();
		}

		return result;
	}

	// private HashMap parseResult(Object[] result) {
	// HashMap<String, Object> resultHash = new HashMap<String, Object>();
	//
	// for(Object each_obj : result) {
	// Object[] each_obj_array = (Object[])each_obj;
	// resultHash.put((String)each_obj_array[0], each_obj_array[1]);
	// }
	//
	// return resultHash;
	// }

	private void getImageID() {
		String[] params = null;
		xmlRPCcall("XMLRPCgetImages", params);
	}

	private boolean makeReservation() {
		String[] params = new String[3];
		// VCL2.2.1 SandBox image id 2422
		/*
		 * {id=2813, name=centos_tunnel_main_campus} {id=1913,
		 * name=centos_tunnel_mcnc}
		 */

		params[0] = Integer.toString(image_id);
		params[1] = startTime;
		params[2] = duration; // Resever for one hour

		HashMap result = (HashMap) xmlRPCcall("XMLRPCaddRequest", params);

		boolean success_in_resv = false;
		if (result.get("status").equals("success")) {
			int request_id = Integer.parseInt((String) result.get("requestid"));
			this.activeRequests.add(request_id);
			System.out
					.println("Succeed in making the reservation, request id is: "
							+ request_id);

			// Check whether the reservation is ready
			while (true) {
				HashMap status = getRequestStatus(request_id);

				if (status.get("status").equals("ready")) {
					System.out.println("The reservation is ready!");
					success_in_resv = true;
					break;
				} else if (status.get("status").equals("loading")) {
					int remain_time = (Integer) status.get("time");
					try {
						System.out
								.println("The reservation is still loading with "
										+ remain_time + " minutes remained...");
						// Wait for half of the remaining time and then check
						// again.
						if (remain_time <= 1) {
							TimeUnit.SECONDS.sleep(30);
						} else {
							TimeUnit.MINUTES.sleep((int) (remain_time / 2));
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.err.println("Fail to make a reservation.");
					break;
				}
			}
		} else {
			System.err.println("Fail to make a reservation.");
		}

		return success_in_resv;
	}

	private HashMap getRequestStatus(int request_id) {
		Object[] params = new Object[1];
		params[0] = request_id;
		return (HashMap) xmlRPCcall("XMLRPCgetRequestStatus", params);
	}

	private void cancelReservation() {
		if (!this.activeRequests.isEmpty()) {
			int request_id = this.activeRequests.get(0);
			Object[] params = new Object[1];
			params[0] = request_id;
			HashMap result = (HashMap) xmlRPCcall("XMLRPCendRequest", params);

			if (result.get("status").equals("success")) {
				System.out
						.println("End reservation, request id: " + request_id);
				this.activeRequests.remove(0);
			} else {
				System.err.println("Fail to end reservation, request id: "
						+ request_id);
			}
		}
	}

	private String[] getConnectData() {
		InetAddress addr;
		String ipAddr;

		try {
			addr = InetAddress.getLocalHost();
			// Get IP Address
			ipAddr = addr.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		Object[] params = { this.activeRequests.get(0), ipAddr };
		// Object[] params = { 1744326, ipAddr };

		HashMap result = (HashMap) xmlRPCcall("XMLRPCgetRequestConnectData",
				params);

		String[] conn_data = null;
		if (result.get("status").equals("ready")) {
			conn_data = new String[3];
			conn_data[0] = (String) result.get("serverIP");
			conn_data[1] = (String) result.get("user");
			conn_data[2] = (String) result.get("password");

			System.out.println("Reserved IP: " + conn_data[0]);
			System.out.println("Username: " + conn_data[1]);
			if (conn_data[2].equals("")) {
				conn_data[2] = this.password;
				System.out.println("Password: (use your campus password)");
			} else {
				System.out.println("Password: " + conn_data[2]);
			}
		} else if (result.get("status").equals("notready")) {
			System.err.println("The reservation is not ready.");
		} else {
			System.err.println("Fail to get the connect data.");
		}

		return conn_data;
	}

	private void termLaunch(String[] conn_data) {
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
			ssh_command = "osascript";
			break;
		case Android:

			break;

		default:
			break;
		}
		System.out.println(ssh_command);

		// Using string array is due to the requirement of the argument accepted
		// by rt.exec.
		Runtime rt = Runtime.getRuntime();
		try {
			System.out.println("Now launch the terminal");
			rt.exec(commands);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void start() {
		// TestOBA oba = new TestOBA(args[0], args[1], platform);

		// oba.getImageID();
		if (makeReservation()) {
			String[] conn_data = getConnectData();

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
			termLaunch(conn_data);
			// oba.cancelReservation();
		}
	}

}