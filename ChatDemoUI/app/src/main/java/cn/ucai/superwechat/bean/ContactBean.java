package cn.ucai.superwechat.bean;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by clawpo on 16/2/10.
 */
public class ContactBean implements Serializable {

    /**
     * result : ok
     * myuid : 1001
     * cuid : 1002
     * isGetMyLocation : false
     * isShowMyLocation : false
     */

    private String result;
    private int myuid;
    private int cuid;
    @JsonProperty("isGetMyLocation")
    private boolean 	isGetMyLocation;
    @JsonProperty("isShowMyLocation")
    private boolean 	isShowMyLocation;

    public void setResult(String result) {
        this.result = result;
    }

    public void setMyuid(int myuid) {
        this.myuid = myuid;
    }

    public void setCuid(int cuid) {
        this.cuid = cuid;
    }

    public void setIsGetMyLocation(boolean isGetMyLocation) {
        this.isGetMyLocation = isGetMyLocation;
    }

    public void setIsShowMyLocation(boolean isShowMyLocation) {
        this.isShowMyLocation = isShowMyLocation;
    }

    public String getResult() {
        return result;
    }

    public int getMyuid() {
        return myuid;
    }

    public int getCuid() {
        return cuid;
    }

    @JsonIgnore
    public boolean isGetMyLocation() {
        return isGetMyLocation;
    }

    @JsonIgnore
    public boolean isShowMyLocation() {
        return isShowMyLocation;
    }

    public ContactBean() {
    }

    public ContactBean(String result, int myuid, int cuid, boolean isGetMyLocation, boolean isShowMyLocation) {
        this.result = result;
        this.myuid = myuid;
        this.cuid = cuid;
        this.isGetMyLocation = isGetMyLocation;
        this.isShowMyLocation = isShowMyLocation;
    }

    @Override
    public String toString() {
        return "ContactBean{" +
                "result='" + result + '\'' +
                ", myuid=" + myuid +
                ", cuid=" + cuid +
                ", isGetMyLocation=" + isGetMyLocation +
                ", isShowMyLocation=" + isShowMyLocation +
                '}';
    }
}
