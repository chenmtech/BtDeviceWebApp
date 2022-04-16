package com.cmtech.web.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

/**
 * MD5加密辅助类
 * @author gdmc
 *
 */
public class MD5Utils {

    /**
     * 生成MD5密码
     * @param info
     * @return
     */
    public static String getMD5Code(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes(StandardCharsets.UTF_8));
            byte[] encryption = md5.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : encryption) {
                if (Integer.toHexString(0xff & b).length() == 1) {
                    stringBuilder.append("0").append(Integer.toHexString(0xff & b));
                } else {
                    stringBuilder.append(Integer.toHexString(0xff & b));
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
//            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * 加盐生成MD5密码
     *
     * @param password
     * @return
     * @author daniel
     * @time 2016-6-11 下午8:45:04
     */
    public static String generate(String password) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(16);
        sb.append(r.nextInt(99999999)).append(r.nextInt(99999999));
        int len = sb.length();
        if (len < 16) {
            for (int i = 0; i < 16 - len; i++) {
                sb.append("0");
            }
        }
        String salt = sb.toString();
        password = getMD5Code(password + salt);
        if(password.length() != 32) return "";
        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = password.charAt(i / 3 * 2);
            char c = salt.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = password.charAt(i / 3 * 2 + 1);
        }
        return new String(cs);
    }

    /**
     * 校验加盐后密码是否与现有密码一致
     *
     * @param password：加盐前密码
     * @param md5：现有密码
     * @return
     * @author daniel
     * @time 2016-6-11 下午8:45:39
     */
    public static boolean verify(String password, String md5) {
        char[] cs1 = new char[32];
        char[] cs2 = new char[16];
        for (int i = 0; i < 48; i += 3) {
            cs1[i / 3 * 2] = md5.charAt(i);
            cs1[i / 3 * 2 + 1] = md5.charAt(i + 2);
            cs2[i / 3] = md5.charAt(i + 1);
        }
        String salt = new String(cs2);
        return getMD5Code(password + salt).equals(new String(cs1));
    }
}
