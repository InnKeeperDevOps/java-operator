package run.innkeeper.utilities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

public class HashGenerator {
    public static String getBuildName(String namespace, String name){
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest((name+'_'+namespace).getBytes());
            return new BigInteger(1, hash).toString(16);
        }catch (Exception e){
            e.printStackTrace();
        }
        return name;
    }

    public static String getJobName(String namespace, String name) {
        return UUID.nameUUIDFromBytes((name+'_'+namespace).getBytes()).toString();
    }
}
