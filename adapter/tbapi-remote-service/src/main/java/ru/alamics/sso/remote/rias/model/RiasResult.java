package ru.alamics.sso.remote.rias.model;

import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;

@ToString
public class RiasResult {

    private Integer checkProfileData;

    @XmlElement(name = "check_profile_data")
    public Integer getCheckProfileData() {
        return this.checkProfileData;
    }

    public void setCheckProfileData(Integer checkProfileData) {
        this.checkProfileData = checkProfileData;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RiasResult)) return false;
        final RiasResult other = (RiasResult) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$checkProfileData = this.getCheckProfileData();
        final Object other$checkProfileData = other.getCheckProfileData();
        if (this$checkProfileData == null ? other$checkProfileData != null : !this$checkProfileData.equals(other$checkProfileData))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RiasResult;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $checkProfileData = this.getCheckProfileData();
        result = result * PRIME + ($checkProfileData == null ? 43 : $checkProfileData.hashCode());
        return result;
    }

}
