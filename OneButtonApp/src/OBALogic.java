import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcCommonsTransport;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransport;

/**
 * The OBALogic is the connector with VCL server
 * It will be responsible to sending and receiving the request from client to VCL server
 * @author Minh Tuan PHAM
 *
 */
public class OBALogic {

	private String username;

	private String password;

	private XmlRpcClient client;
	
	private String errMsg;

	public OBALogic(final String username,
			final String password) {
		this.username = username;
		this.password = password;

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
			System.err.println("XML RPC Call Error!");
			// e.printStackTrace();
		}

		return result;
	}

	/**
	 * This method try to send a reservation request to the VCL server
	 * If success: return the requestid
	 * If failed: return the value -1 and an error message message in the variable errMsg of class
	 * @param imageid
	 * @param start
	 * @param length
	 * @return requestid or -1.
	 */
	public int sendRequestReservation(int imageid, int start, int length) {
		errMsg = "Error Message";
		return -1;
	}
	
	/**
	 * This method checks request status
	 * 
	 * @param request_id
	 * @return	<"error", errormsg> : errormsg may be request error or failed request
	 * 			<"ready">
	 * 			<"loading",estimated time>
	 * 			<"timeout"> : request timeout because of no connection in the limit time
	 * 			<"future">
	 */
	public HashMap<String, String> getRequestStatus(int requestID) {
		/*Object[] params = new Object[1];
		params[0] = requestID;
		return (HashMap) xmlRPCcall("XMLRPCgetRequestStatus", params);*/
		return null;
	}
	
	/**
	 * This method send a request canceling an reservation
	 * This method may be quite tricky, because it need to differentiate between a requesting reservation and a successfully requested reservation
	 * in both case, it should cancel the reservation
	 * 
	 * @return	If success: true
	 * 			If failed: return false and an error message message in the variable errMsg of class
	 */
	public boolean requestCancelReservation(int reservationID) {
		/*
		if (this.active_req_id > 0) {
		 
			int request_id = this.active_req_id;
			Object[] params = new Object[1];
			params[0] = request_id;
			HashMap result = (HashMap) xmlRPCcall("XMLRPCendRequest", params);

			if (result.get("status").equals("success")) {
				System.out
						.println("End reservation, request id: " + request_id);
				this.active_req_id = -1;
			} else {
				System.err.println("Fail to end reservation, request id: "
						+ request_id);
			}
		}
		*/
		return false;
	}
	
	/**
	 * This method send a request extending the reservation
	 * @param reservationID
	 * @param extendTime
	 * @return	If success: true
	 * 			If failed: return false and an error message message in the variable errMsg of class
	 */
	public boolean extendReservation(int reservationID, int extendTime) {
		return false;
	}
	

	/**
	 * Send request to the VCL server and ask for ALL the informations of the current Reservation
	 * this function need to fill all the fields each OBABean: request_id, image_name, image_id, etc.
	 * @return the ArrayList of all the OBABean or null
	 */
	public ArrayList<OBABean> getCurrentReservations() {
		return null;
	}
	
	/**
	 * This method send a request to VCL server to get the connection data for a requestID
	 * This method should be called only after the request status checking
	 * @param	requestID
	 * @param	ip address of connecting user's compute
	 * 
	 * @return 	a String[] with [0] = serverIP - address of the reserved machine
								[1] = user - user to use when connecting to the machine
								[2] = password - password to use when connecting to the machine
				null if failed
	 */
	public String[] getConnectData(int requestID, String remoteIP) {
		InetAddress addr;
		String ipAddr;
/*
		try {
			addr = InetAddress.getLocalHost();
			// Get IP Address
			ipAddr = addr.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		Object[] params = { this.active_req_id, ipAddr };
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
		*/
		return null;
	}

	/**
	 * This method send a request to the server to get all the accessible Images for this user
	 * @return HashMap <"ImageID", "ImageName">
	 */
	public HashMap<String, String> getAvailableImages() {
		/*
		String[] params = null;
		xmlRPCcall("XMLRPCgetImages", params);
		*/
		return null;
		
	}
	
	/**
	 * Check username, password
	 * @return
	 */
	public boolean loginCheck() {
		HashMap result = (HashMap) xmlRPCcall("XMLRPCtest", null);
		if (result != null && result.get("status").equals("success")) {
			return true;
		}

		return false;
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
	
	/*
	public String[] getPercentageStatus() {
		int complete_percent = -1;
		String remain_time_str = null;

		// Check whether the reservation is ready
		HashMap status = getRequestStatus(active_req_id);

		if (status.get("status").equals("ready")) {
			complete_percent = 100;
			remain_time_str = "Ready";
		} else if (status.get("status").equals("loading")) {
			remain_time_str = (String) status.get("time");
			int remain_time = Integer.parseInt(remain_time_str);

			if (initial_loading_time < 0) {
				initial_loading_time = remain_time;
			}

			complete_percent = (int) (((float) initial_loading_time - (float) remain_time)
					/ (float) initial_loading_time * 100.0);
		} else {
			System.err.println("Fail to make a reservation.");
		}

		return new String[] { Integer.toString(complete_percent),
				remain_time_str };
	}
	*/
}
