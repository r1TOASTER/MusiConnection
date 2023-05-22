package com.example.musiconnection;

// Encryption class holds the encrypt and decrypt functions to use when sending / receiving data in / from a socket.
class Encryption {
    // Returns the encrypted string that was generated using the plainText and the shift number.
    public static String encrypt(String plainText, int shift) {
        StringBuilder encryptedText = new StringBuilder();
        for (int i = 0; i < plainText.length(); i++) {
            char c = plainText.charAt(i);
            if (Character.isAlphabetic(c)) {
                if (Character.isLowerCase(c)) {
                    c = (char) ((c - 'a' + shift) % 26 + 'a');
                } else {
                    c = (char) ((c - 'A' + shift) % 26 + 'A');
                }
            }
            encryptedText.append(c);
        }
        return encryptedText.toString();
    }

    // Returns the decrypted string that was generated using the plainText and the shift number.
    public static String decrypt(String cipherText, int shift) {
        StringBuilder decryptedText = new StringBuilder();
        for (int i = 0; i < cipherText.length(); i++) {
            char c = cipherText.charAt(i);
            if (Character.isAlphabetic(c)) {
                if (Character.isLowerCase(c)) {
                    c = (char) ((c - 'a' - shift + 26) % 26 + 'a');
                } else {
                    c = (char) ((c - 'A' - shift + 26) % 26 + 'A');
                }
            }
            decryptedText.append(c);
        }
        return decryptedText.toString();
    }
}
