package cn.ucai.superwechat.bean;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by clawpo on 16/2/10.
 */
public class GroupBean implements Serializable {

    /**
     * id : 1010
     * name : Android项目群
     * avatar :  group_avatar/android.jpg
     * intro : 私人讨论小组
     * owner : 1001
     * isPublic : false
     * isExame : false
     * groupId : 2011
     * modifiedTime : 1010
     * members : 1001,1002,1003
     */

    private int id;
    private String name;
    private String avatar;
    private String intro;
    private String owner;
    @JsonProperty("isPublic")
    private boolean isPublic;
    @JsonProperty("isExame")
    private boolean isExame;
    private String groupId;
    private int modifiedTime;
    private String members;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setIsExame(boolean isExame) {
        this.isExame = isExame;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setModifiedTime(int modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getIntro() {
        return intro;
    }

    public String getOwner() {
        return owner;
    }

    @JsonIgnore
    public boolean isPublic() {
        return isPublic;
    }

    @JsonIgnore
    public boolean isExame() {
        return isExame;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getModifiedTime() {
        return modifiedTime;
    }

    public String getMembers() {
        return members;
    }

    public GroupBean() {
    }
	public GroupBean(String groupId, String name, String intro, String owner,
			boolean isPublic, boolean isExame,String members) {
		super();
		this.groupId = groupId;
		this.name = name;
		this.intro = intro;
		this.owner = owner;
		this.isPublic = isPublic;
		this.isExame = isExame;
		this.members=members;
	}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupBean)) return false;

        GroupBean groupBean = (GroupBean) o;

        return getName() != null ? getName().equals(groupBean.getName()) : groupBean.getName() == null;

    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GroupBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", intro='" + intro + '\'' +
                ", owner='" + owner + '\'' +
                ", isPublic=" + isPublic +
                ", isExame=" + isExame +
                ", groupId='" + groupId + '\'' +
                ", modifiedTime=" + modifiedTime +
                ", members='" + members + '\'' +
                '}';
    }
}
