package cn.ucai.superwechat.bean;

import java.io.Serializable;

/**
 * Created by clawpo on 16/2/10.
 */
public class UserBean implements Serializable {

    /**
     * id : 1001
     * result : ok
     * userName : zhangsan
     * nick : 张三
     * password : 1234
     * avatar : user_avatar/zhangsan.jpg
     * groups : 2011
     * header : z
     * latitude : 8.34
     * longitude : 12.2
     * unreadMsgCount : 4
     */

    private int id;
    private String result;
    private String userName;
    private String nick;
    private String password;
    private String avatar;
    private String groups;
    private String header;
    private double latitude;
    private double longitude;
    private int unreadMsgCount;

    public void setId(int id) {
        this.id = id;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.unreadMsgCount = unreadMsgCount;
    }

    public int getId() {
        return id;
    }

    public String getResult() {
        return result;
    }

    public String getUserName() {
        return userName;
    }

    public String getNick() {
        return nick;
    }

    public String getPassword() {
        return password;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getGroups() {
        return groups;
    }

    public String getHeader() {
        return header;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getUnreadMsgCount() {
        return unreadMsgCount;
    }

    public UserBean() {
    }

    public UserBean(String userName) {
        this.userName = userName;
    }

    public UserBean(String userName, String nick, String password) {
        this.userName = userName;
        this.nick = nick;
        this.password = password;
    }

    public UserBean(int id, String result, String userName, String nick, String password, String avatar, double latitude, double longitude, int unreadMsgCount) {
        this.id = id;
        this.result = result;
        this.userName = userName;
        this.nick = nick;
        this.password = password;
        this.avatar = avatar;
        this.latitude = latitude;
        this.longitude = longitude;
        this.unreadMsgCount = unreadMsgCount;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "result='" + result + '\'' +
                ", id=" + id +
                ", userName='" + userName + '\'' +
                ", nick='" + nick + '\'' +
                ", password='" + password + '\'' +
                ", avatar='" + avatar + '\'' +
                ", groups='" + groups + '\'' +
                ", header='" + header + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", unreadMsgCount=" + unreadMsgCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserBean userBean = (UserBean) o;

        return userName != null ? userName.equals(userBean.userName) : userBean.userName == null;

    }

    @Override
    public int hashCode() {
        return userName != null ? userName.hashCode() : 0;
    }
}
