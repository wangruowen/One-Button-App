import java.util.ArrayList;

public class OBADBManager {
	private static OBADBManager instance = null;
	private static PasswordManager pwdManager = new PasswordManager();

	protected OBADBManager() {
		// Exists only to defeat instantiation.
	}

	// singleton pattern
	public static OBADBManager getInstance() {
		if (instance == null) {
			instance = new OBADBManager();
		}
		return instance;
	}

	/**
	 * Write the password of user into the SQLite database return error if
	 * having any error
	 * 
	 * @param username
	 * @param password
	 * @return if false, write down the error message in the errMsg variable
	 */
	public boolean storePasswd(String username, String password) {
		// TODO Auto-generated method stub
		PasswordManager.storeData(username, password);
		return true;

	}

	public void clearLoginTable() {
		PasswordManager.deleteTable();
	}

	/**
	 * Look at the SQlite database and get the saved password and username if
	 * exist
	 * 
	 * @return <username, password> if exist null if not
	 */
	public String[] getRememberPasswd() {
		try {
			String[] userInfos = new String[2];
			PasswordManager.getData();
			userInfos[0] = pwdManager.getUserName();
			userInfos[1] = pwdManager.getPwd();
			return userInfos;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public ArrayList<OBAEntry> getStoredOBAEntries(String username) {
		return ImageManager.getData(username);
	}

	public void storeOBAEntry(String username, OBAEntry newObaEntry) {
		ImageManager.storeData(username, newObaEntry.getImageID(),
				newObaEntry.getImageName(), newObaEntry.getImageDesc(),
				newObaEntry.getLoginMode());
	}
}
