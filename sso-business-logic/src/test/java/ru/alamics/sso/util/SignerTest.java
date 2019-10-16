package ru.alamics.sso.util;

import com.openshift.internal.util.Assert;
import org.junit.jupiter.api.Test;

class SignerTest {

    @Test
    void signString() {
        var forSign = "TESTMYCOMPANY";
        Assert.notNull(Signer.signString(forSign));
    }
}