package edu.escuelaing.dinochomp_backend.services;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AesCrypto {

  private static final String SECRET = "MY_SUPER_SECRET_KEY_32_BYTES_1234";

  private static SecretKey getKey(){
    byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, 0, 32, "AES");
  }

  // ===== DESCIFRAR =====
  public static String decrypt(String ivB64, String cipherB64) throws Exception {
    byte[] iv = Base64.getDecoder().decode(ivB64);
    byte[] ciphertext = Base64.getDecoder().decode(cipherB64);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    GCMParameterSpec spec = new GCMParameterSpec(128, iv); // tag 16 bytes
    cipher.init(Cipher.DECRYPT_MODE, getKey(), spec);

    byte[] plain = cipher.doFinal(ciphertext);
    return new String(plain, StandardCharsets.UTF_8);
  }

  // ===== CIFRAR (si el backend envía datos cifrados al cliente) =====
  public static Encrypted encrypt(String json) throws Exception {
    byte[] iv = new byte[12];
    new SecureRandom().nextBytes(iv);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    GCMParameterSpec spec = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.ENCRYPT_MODE, getKey(), spec);

    byte[] ct = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));

    return new Encrypted(
        Base64.getEncoder().encodeToString(iv),
        Base64.getEncoder().encodeToString(ct)
    );
  }

  public static record Encrypted(String iv, String ciphertext){}
}

