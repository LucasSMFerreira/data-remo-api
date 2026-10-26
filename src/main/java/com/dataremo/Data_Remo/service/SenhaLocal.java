package com.dataremo.Data_Remo.service;
import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class SenhaLocal {
  private static byte[] deriva(String senha, byte[] salt) throws Exception {
    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(senha.toCharArray(),salt,120000,256)).getEncoded();
  }
  public static String hash(String senha) {
    try {byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);return Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(deriva(senha,salt));}catch(Exception e){throw new IllegalStateException(e);}
  }
  public static boolean confere(String senha,String hash) {
    try {String[] partes=hash.split(":");return MessageDigest.isEqual(deriva(senha,Base64.getDecoder().decode(partes[0])),Base64.getDecoder().decode(partes[1]));}catch(Exception e){return false;}
  }
}
