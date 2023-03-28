package com.example.musiconnection;

class Encryption {
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
