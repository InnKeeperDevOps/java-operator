package run.innkeeper.utilities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

public class HashGenerator {
    public static String getJobName(String namespace, String name) {
        return UUID.nameUUIDFromBytes((name+'_'+namespace).getBytes()).toString();
    }
}
