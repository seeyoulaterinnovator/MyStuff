package ru.alamics.sso.registration.phone;

import lombok.Builder;
import lombok.Data;

import java.net.URI;
import java.nio.charset.Charset;

@Data
@Builder
public class SmsConfig {

    public class ReportsConfig {
        private ReportsConfig() {
        }

        public static final byte DELIVERED_TO_PHONE = 0b1;
        public static final byte NON_DELIVERED_TO_PHONE = 0b1 << 1;
        public static final byte QUEUED_ON_SMSC = 0b1 << 2;
        public static final byte DELIVERED_TO_SMSC = 0b1 << 3;
        public static final byte NON_DELIVERED_TO_SMSC = 0b1 << 4;

    }

    public enum Encoding {
        BITS7(0), BITS8(1), UCS2(2);

        private final int encodingNum;

        Encoding(int encodingAsInt) {
            encodingNum = encodingAsInt;
        }

        public int getPriorityAsInt() {
            return encodingNum;
        }
    }

    public enum Priority {
        LOWEST(0), LOW(1), MEDIUM(2), HIGH(3);

        private final int priorityNum;

        Priority(int priorityAsInt) {
            priorityNum = priorityAsInt;
        }

        public int getPriorityAsInt() {
            return priorityNum;
        }
    }

    private URI url;

    private String smsCenterName;

    private String username;

    private String password;

    private String senderName;

    private Integer timeout;

    private byte reportsMask;

    private Encoding encoding;

    private Charset charset;

    private Priority priority;

    private String reportBackUrl;

}
