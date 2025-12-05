package edu.escuelaing.dinochomp_backend.utils.dto.encrypted;

// DTO simple para recibir los datos cifrados
public class EncryptedPayload {
    public String iv;
    public String ciphertext;
    // Constructor vacío necesario para Jackson
    public EncryptedPayload() {} 
}
