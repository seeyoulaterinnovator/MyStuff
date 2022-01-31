package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.MsgConfig;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public interface MessageService {

    default MultivaluedMap<String, Object> getConfigForQuery(MsgConfig smsConfig) {

        MultivaluedHashMap<String, Object> map = new MultivaluedHashMap<>();
        map.add("smsc", smsConfig.getMsgCenterName());
        map.add("username", smsConfig.getUsername());
        map.add("password", smsConfig.getPassword());
        map.add("from", smsConfig.getSenderName());
        map.add("validity", smsConfig.getTimeout());
        map.add("priority", smsConfig.getPriority().getPriorityAsInt());
        map.add("dlr-mask", smsConfig.getReportsMask());
        map.add("coding", smsConfig.getEncoding().getPriorityAsInt());
        map.add("charset", smsConfig.getCharset());

        return map;
    }
}
