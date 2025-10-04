package com.longjunwang.moni.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encrypt {
    /**
     * 使用 SHA-256 加密字符串
     * @param input 原始字符串
     * @return 64位十六进制哈希
     */
    public static String encrypt(String input) {
        try {
            // 1️⃣ 创建 SHA-256 摘要器
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 2️⃣ 执行加密
            byte[] hash = digest.digest(input.getBytes());

            // 3️⃣ 转为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b & 0xff));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(Encrypt.encrypt("今天是个很有消费258.96").length());
    }
}
