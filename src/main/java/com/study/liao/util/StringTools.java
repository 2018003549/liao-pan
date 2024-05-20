package com.study.liao.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;

public class StringTools {
    /**
     * 生成随机数
     * @param count 随机数位数
     * @return
     */
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }
    public static boolean isEmpty(String str) {

        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }
    public static String encodeByMd5(String orignString){
        return isEmpty(orignString)?null: DigestUtils.md2Hex(orignString);
    }
}
