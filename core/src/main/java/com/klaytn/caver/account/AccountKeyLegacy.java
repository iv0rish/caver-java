package com.klaytn.caver.account;

import com.klaytn.caver.tx.account.AccountKey;
import org.web3j.utils.Numeric;

import java.util.Arrays;

/**
 * AccountKeyLegacy represents a key of legacy account types. If an account has AccountKeyLegacy,
 * the tx validation process is done like below (as Ethereum did):
 * <ul>
 * <li> Get the public key from ecrecover(txhash, txsig) </li>
 * <li> Get the address of the public key </li>
 * <li> The address is the sender </li>
 * </ul>
 */
public class AccountKeyLegacy implements IAccountKey{

    private static final byte[] RLP = new byte[]{(byte) 0x01, (byte) 0xc0};
    private static final byte TYPE = (byte)0x01;


    public AccountKeyLegacy() {

    }


    /**
     * Decodes a RLP-encoded AccountKeyLegacy string
     * @param rlpEncodedKey A RLP-encoded AccountKeyLegacy string
     * @return AccountKeyLegacy
     */
    public static AccountKeyLegacy decode(String rlpEncodedKey) {
        return decode(Numeric.hexStringToByteArray(rlpEncodedKey));
    }

    /**
     * Decodes a RLP-encoded AccountKeyLegacy byte array
     * @param rlpEncodedKey RLP-encoded AccountKeyLegacy byte array
     * @return AccountKeyLegacy
     */
    public static AccountKeyLegacy decode(byte[] rlpEncodedKey) {
        if(!Arrays.equals(RLP, rlpEncodedKey)) {
            throw new RuntimeException("Invalid RLP-encoded account key String");
        }

        return new AccountKeyLegacy();
    }

    /**
     * Encodes a AccountKeyLegacy Object by RLP-encoding method.
     * @return RLP-encoded AccountKeyLegacy string
     */

    @Override
    public String getRLPEncoding() {
        return Numeric.toHexString(RLP);
    }

    public static byte getType() {
        return TYPE;
    }
}
