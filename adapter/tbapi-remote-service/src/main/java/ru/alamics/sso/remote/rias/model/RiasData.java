package ru.alamics.sso.remote.rias.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
public class RiasData {

    private Integer status;

    private RiasResult result;

    private RiasMessage messages;

    public Integer getStatus() {
        return this.status;
    }

    public RiasResult getResult() {
        return this.result;
    }

    public RiasMessage getMessages() {
        return this.messages;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setResult(RiasResult result) {
        this.result = result;
    }

    public void setMessages(RiasMessage messages) {
        this.messages = messages;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RiasData)) return false;
        final RiasData other = (RiasData) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$status = this.getStatus();
        final Object other$status = other.getStatus();
        if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
        final Object this$result = this.getResult();
        final Object other$result = other.getResult();
        if (this$result == null ? other$result != null : !this$result.equals(other$result)) return false;
        final Object this$messages = this.getMessages();
        final Object other$messages = other.getMessages();
        if (this$messages == null ? other$messages != null : !this$messages.equals(other$messages)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RiasData;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $status = this.getStatus();
        result = result * PRIME + ($status == null ? 43 : $status.hashCode());
        final Object $result = this.getResult();
        result = result * PRIME + ($result == null ? 43 : $result.hashCode());
        final Object $messages = this.getMessages();
        result = result * PRIME + ($messages == null ? 43 : $messages.hashCode());
        return result;
    }

    public String toString() {
        return "RiasData(status=" + this.getStatus() + ", result=" + this.getResult() + ", messages=" + this.getMessages() + ")";
    }
}
