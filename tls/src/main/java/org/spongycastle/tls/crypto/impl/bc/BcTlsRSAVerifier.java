package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.Signer;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSABlindedEngine;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.signers.GenericSigner;
import org.spongycastle.crypto.signers.RSADigestSigner;
import org.spongycastle.tls.DigitallySigned;
import org.spongycastle.tls.HashAlgorithm;
import org.spongycastle.tls.SignatureAlgorithm;
import org.spongycastle.tls.SignatureAndHashAlgorithm;
import org.spongycastle.tls.TlsUtils;

/**
 * Operator supporting the verification of RSA signatures using the BC light-weight API.
 */
public class BcTlsRSAVerifier
    extends BcTlsVerifier
{
    public BcTlsRSAVerifier(BcTlsCrypto crypto, RSAKeyParameters publicKey)
    {
        super(crypto, publicKey);
    }

    public boolean verifyRawSignature(DigitallySigned signedParams, byte[] hash)
    {
        Digest nullDigest = crypto.createDigest(HashAlgorithm.none);

        SignatureAndHashAlgorithm algorithm = signedParams.getAlgorithm();
        Signer signer;
        if (algorithm != null)
        {
            if (algorithm.getSignature() != SignatureAlgorithm.rsa)
            {
                throw new IllegalStateException();
            }

            /*
             * RFC 5246 4.7. In RSA signing, the opaque vector contains the signature generated
             * using the RSASSA-PKCS1-v1_5 signature scheme defined in [PKCS1].
             */
            signer = new RSADigestSigner(nullDigest, TlsUtils.getOIDForHashAlgorithm(algorithm.getHash()));
        }
        else
        {
            /*
             * RFC 5246 4.7. Note that earlier versions of TLS used a different RSA signature scheme
             * that did not include a DigestInfo encoding.
             */
            signer = new GenericSigner(new PKCS1Encoding(new RSABlindedEngine()), nullDigest);
        }
        signer.init(false, publicKey);
        signer.update(hash, 0, hash.length);
        return signer.verifySignature(signedParams.getSignature());
    }
}
