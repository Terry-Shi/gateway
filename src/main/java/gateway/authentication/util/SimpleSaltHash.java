package gateway.authentication.util;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.SimpleHash;

public class SimpleSaltHash {
    
    public static final String salt = "SimpleSaltHash";
    
    /**
     * 
     * @param str 将要被hash算法处理的字符串
     * @param salt
     * @return 加salt并hash后的字符串
     */
    public static String getMd5Hash(String str, String salt) {
        String result = new Md5Hash(str, salt).toString();
        return result;
    }
    
    /**
     * 获取加密后的密码，需要指定 hash迭代的次数
     *
     * @param hashAlgorithm  hash算法名称 MD2、MD5、SHA-1、SHA-256、SHA-384、SHA-512、etc。
     * @param password       需要加密的密码
     * @param salt           盐
     * @param hashIterations hash迭代的次数
     * @return 加密后的密码
     */
    public static String encryptPassword(String hashAlgorithm, String password, String salt, int hashIterations) {
        SimpleHash hash = new SimpleHash(hashAlgorithm, password, salt, hashIterations);
        return hash.toString();
    }
    
    public static void main(String[] args) {
        System.out.println(getMd5Hash("passowrd1", "mysalt"));
    }
}
