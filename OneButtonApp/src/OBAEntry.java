public class OBAEntry {
	
	public int getImageInfo(String userName)
	{
		image image_list[]=imageManager.getData(userName);
		return image_list.length;
	}

	public int getImageID(int index) {
		return image_list[index].imgID;
	}

	public String getImageName(int index) {
		return image_list[index].imgName;
	}

	public String getImageDesc(int index) {
		return image_list[index].imgDesc;
	}
}
