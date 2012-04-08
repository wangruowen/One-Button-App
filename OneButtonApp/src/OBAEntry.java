public class OBAEntry {
	
	public void getImageInfo(String userName)
	{
		image image_list[]=imageManager.getData(userName);
	}

	public int getImageID(int index) {
		if(image_list.length!=0)
		return image_list[index].imgID;
		else
		return null;
	}

	public String getImageName(int index) {
		if(image_list.length!=0)
		return image_list[index].imgName;
		else
		return null;
	}

	public String getImageDesc(int index) {
		if(image_list.length!=0)
		return image_list[index].imgDesc;
		else
		return null;
	}
}
