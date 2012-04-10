public class OBAEntry {
	private int image_id;
	private String image_name;
	private String image_desc;
	private int load_mode;

	public OBAEntry(int image_id, String image_name, String image_desc,
			int load_mode) {
		this.image_id = image_id;
		this.image_name = image_name;
		this.image_desc = image_desc;
		this.load_mode = load_mode;
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

	public int getLoadMode() {
		return this.load_mode;
	}
}
