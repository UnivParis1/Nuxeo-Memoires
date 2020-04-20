package fr.up1.memoire;

public class MemDocInfo {

	private String parentPath;
	private String type;
	private String name;
	private String title;
	private String description;

	public MemDocInfo(String parentPath, String type, String name, String title, String description) {
		setParentPath(parentPath);
		this.type = type;
		this.name = name;
		this.title = title;
		this.description = description;
	}
	public String getParentPath() {
		return parentPath;
	}
	public void setParentPath(String parentPath) {
		if("/".equals(parentPath) || !parentPath.endsWith("/") ) {
			this.parentPath = parentPath;
		}else{
			this.parentPath = parentPath.substring(0, parentPath.length()-1);
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		if(!title.isEmpty()) {
            return title;
        } else {
            return name;
        }
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPath(){
		if("/".equals(parentPath)){
			return parentPath+name;
		}else{
			return parentPath+"/"+name;
		}
	}
	@Override
    public String toString(){
		return getType()+" : " +getName()+" “"+getTitle()+"”";
	}
}
