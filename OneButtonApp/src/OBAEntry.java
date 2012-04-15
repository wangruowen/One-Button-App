public class OBAEntry {
	private int image_id;
	private String image_name;
	private String image_desc;
	private int login_mode;

	public OBAEntry(int image_id, String image_name, String image_desc,
			int login_mode) {
		this.image_id = image_id;
		this.image_name = image_name;
		this.image_desc = image_desc;
		this.login_mode = login_mode;
	}

	public int getImageID() {
		// TODO Auto-generated method stub
		return this.image_id;
	}

	public String getImageName() {
		// TODO Auto-generated method stub
		return this.image_name;
	}

	public String getImageDesc() {
		// TODO Auto-generated method stub
		return this.image_desc;
	}

	public int getLoginMode() {
		return this.login_mode;
	}

	public void setLoginMode(int login_mode) {
		this.login_mode = login_mode;
	}
}
