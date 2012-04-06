import java.util.HashMap;


public class OBADBManager {
	private static OBADBManager instance = null;
	protected OBADBManager() {
		// Exists only to defeat instantiation.
	}
	
	// singleton pattern
	public static OBADBManager getInstance() {
		if(instance == null) {
			instance = new OBADBManager();
		}
		return instance;
	}

	/**
	 * Write the password of user into the SQLite database
	 * return error if having any error
	 * @param username
	 * @param password
	 * @return if false, write down the error message in the errMsg variable
	 */
	public boolean storePasswd(String username, String password) {
		// TODO Auto-generated method stub
		return false;
		
	}
	/**
	 * Look at the SQlite database and get the saved password and username if exist
	 * @return <username, password> if exist
	 * 			null if not
	 */
	public HashMap<String, String> getRememberPasswd() {
		return null;
	}
}
