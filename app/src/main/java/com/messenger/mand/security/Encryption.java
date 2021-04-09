package com.messenger.mand.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Encryption {
    private final String TAG = Encryption.class.toString();

    private final Cipher cipher;


    public Encryption() throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }

    public final KeyPair generateRSAKeyPairWithAlias(String alias, int keySize) throws Exception
    {
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, 100);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

        kpg.initialize(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                .setKeySize(keySize)
                .setCertificateSubject(new X500Principal("CN=test"))
                .setCertificateSerialNumber(BigInteger.ONE)
                .setUserAuthenticationRequired(false)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .setCertificateNotBefore(notBefore.getTime())
                .setCertificateNotAfter(notAfter.getTime())
                .setRandomizedEncryptionRequired(true)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build());

        return kpg.generateKeyPair();
    }

    public final PublicKey getPublicKeyFromAlias(String alias, @NotNull KeyStore keyStore)
    {
        KeyStore.PrivateKeyEntry privateKeyEntry = null;
        try {
            privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        assert privateKeyEntry != null;
        return privateKeyEntry.getCertificate().getPublicKey();
    }

    public final PrivateKey getPrivateKeyFromAlias(String alias, @NotNull KeyStore keyStore)
    {
        KeyStore.PrivateKeyEntry privateKeyEntry = null;
        try {
            privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        assert privateKeyEntry != null;
        return privateKeyEntry.getPrivateKey();
    }

    @NotNull
    @Contract("_, _, _ -> new")
    public final String encryptData(@NotNull String data, PublicKey publicKey, String path) throws
            InvalidKeyException, IOException
    {
        this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        CipherOutputStream cipherOutputStream = new CipherOutputStream(
                new FileOutputStream(path), this.cipher);
        cipherOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        cipherOutputStream.close();

        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @NotNull
    @Contract("_, _ -> new")
    public final String decryptData(PrivateKey privateKey, String path) throws
            InvalidKeyException, IOException
    {
        this.cipher.init(Cipher.DECRYPT_MODE, privateKey);

        CipherInputStream cipherInputStream = new CipherInputStream(new FileInputStream(path), this.cipher);
        byte [] roundTrippedBytes = new byte[1000];

        int index = 0;
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1)
        {
            roundTrippedBytes[index] = (byte)nextByte;
            index++;
        }

        return new String(roundTrippedBytes, 0, index, StandardCharsets.UTF_8);
    }


    public final String encryptText(@NotNull String msg, PublicKey key) throws IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);

        return Base64.encodeBase64String(cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8)));
    }

    @NotNull
    @Contract("_, _ -> new")
    public final String decryptText(String msg, PrivateKey key) throws InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);

        return new String(cipher.doFinal(Base64.decodeBase64(msg)), StandardCharsets.UTF_8);
    }


    public final KeyPair generateRSAKeyPair() throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        int keyLength = 1024;
        keyPairGenerator.initialize(keyLength, secureRandom);

        return keyPairGenerator.generateKeyPair();
    }

}
