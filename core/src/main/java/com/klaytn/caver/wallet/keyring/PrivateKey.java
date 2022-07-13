/*
 * Copyright 2020 The caver-java Authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klaytn.caver.wallet.keyring;

import com.klaytn.caver.utils.BytesUtils;
import com.klaytn.caver.utils.Utils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * Represents a PrivateKey class that includes private key string
 */
public class PrivateKey {
    static final int LEN_UNCOMPRESSED_PUBLIC_KEY_STRING = 128;

    /**
     * Private key string
     */
    private String privateKey;

    /**
     * Creates a PrivateKey instance
     * @param privateKey The private key string.
     */
    public PrivateKey(String privateKey) {
        if(!Utils.isValidPrivateKey(privateKey)) {
            throw new IllegalArgumentException("Invalid private key.");
        }
        this.privateKey = Numeric.prependHexPrefix(privateKey);
    }

    /**
     * Create a random PrivateKey instance.<p>
     * <pre>{@code
     * PrivateKey privateKey = PrivateKey.generate();
     * }</pre>
     *
     * @return PrivateKey
     */
    public static PrivateKey generate() {
        return PrivateKey.generate(null);
    }

    /**
     * Create a PrivateKey instance with entropy.<p>
     * <pre>{@code
     * String entropy = "entropy";
     * PrivateKey privateKey = PrivateKey.generate(entropy);
     * }</pre>
     *
     * @param entropy The entropy string
     * @return PrivateKey
     */
    public static PrivateKey generate(String entropy) {
        byte[] random = Utils.generateRandomBytes(32);

        byte[] entropyArr;
        if(entropy == null || entropy.isEmpty()) {
            entropyArr = Utils.generateRandomBytes(32);
        } else {
            entropyArr = Numeric.hexStringToByteArray(entropy);
        }

        byte[] innerHex = Hash.sha3(BytesUtils.concat(random, entropyArr));
        byte[] middleHex = BytesUtils.concat(BytesUtils.concat(Utils.generateRandomBytes(32), innerHex), Utils.generateRandomBytes(32));

        String outerHex = Numeric.toHexString(Hash.sha3(middleHex));

        return new PrivateKey(outerHex);
    }

    /**
     * Signs transactionHash with key and returns signature.<p>
     * <pre>Example :
     * {@code
     * int chainId = 1;
     * String hash = "0x{hash}";
     * PrivateKey prvKey = new PrivateKey("{privateKeyString}");
     *
     * SignatureData sign = prvKey.sign(hash, chainId);
     * }
     * </pre>
     * @param sigHash The has of transactionHash
     * @param chainId The chainId or network
     * @return SignatureData
     */
    public SignatureData sign(String sigHash, int chainId) {
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        Sign.SignatureData signatureData = Sign.signMessage(Numeric.hexStringToByteArray(sigHash), keyPair, false);

        SignatureData signData = new SignatureData(signatureData.getV(), signatureData.getR(), signatureData.getS());
        signData.makeEIP155Signature(chainId);

        return signData;
    }

    /**
     * Signs with hashed data and returns signature data.<p>
     * It returns a signature which has v as a parity of the y value(0 for even, 1 for odd) of secp256k1 signature.
     * <pre>Example :
     * {@code
     * String hash = "0x{hash}";
     * PrivateKey prvKey = new PrivateKey("{privateKeyString}");
     *
     * SignatureData sign = prvKey.ecsign(hash);
     * }
     * </pre>
     * @param sigHash The hash to sign
     * @return SignatureData
     */
    public SignatureData ecsign(String sigHash) {
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        Sign.SignatureData signatureData = Sign.signMessage(Numeric.hexStringToByteArray(sigHash), keyPair, false);

        // Sign.signMessage() always add to 27 at V value. so it need to substract 27 from V value.
        byte[] v = new byte[] {(byte)(signatureData.getV()[0] - 27)};

        SignatureData signData = new SignatureData(v, signatureData.getR(), signatureData.getS());
        return signData;
    }

    /**
     * Signs hashed data with key and returns signature.<p>
     * <pre>Example :
     * {@code
     * String msgHash = "0x{hash}";
     * PrivateKey prvKey = new PrivateKey("{privateKeyString}");
     *
     * SignatureData sign = prvKey.signMessage(msgHash);
     * }
     * </pre>
     * @param messageHash The hash of data to sign
     * @return SignatureData
     */
    public SignatureData signMessage(String messageHash) {
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        Sign.SignatureData signatureData = Sign.signMessage(Numeric.hexStringToByteArray(messageHash), keyPair, false);

        SignatureData signData = new SignatureData(signatureData.getV(), signatureData.getR(), signatureData.getS());
        return signData;
    }

    /**
     * Returns public key string.<p>
     * <pre>Example :
     * {@code
     * PrivateKey prvKey = new PrivateKey("{privateKeyString}");
     *
     * String publicKey = prvKey.getPublicKey(true);
     * }
     * </pre>
     * @param compressed If true, it returns compressed format
     * @return String
     */
    public String getPublicKey(boolean compressed) {
        BigInteger publicKey = Sign.publicKeyFromPrivate(Numeric.toBigInt(privateKey));

        if(compressed) {
            return Utils.compressPublicKey(Numeric.toHexStringWithPrefixZeroPadded(publicKey, LEN_UNCOMPRESSED_PUBLIC_KEY_STRING));
        }

        return Numeric.toHexStringWithPrefixZeroPadded(publicKey, LEN_UNCOMPRESSED_PUBLIC_KEY_STRING);
    }

    /**
     * Returns derived address from private key string.
     * <pre>Example :
     * {@code
     * PrivateKey prvKey = new PrivateKey("{privateKeyString}");
     * String address = prvKey.getDerivedAddress();
     * }
     * </pre>
     * @return String
     */
    public String getDerivedAddress() {
        BigInteger publicKey = Sign.publicKeyFromPrivate(Numeric.toBigInt(privateKey));
        return Numeric.prependHexPrefix(Keys.getAddress(publicKey));
    }

    /**
     * Getter function of private key string
     * @return String
     */
    public String getPrivateKey() {
        return privateKey;
    }
}
