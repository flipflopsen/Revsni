using System.Security.Cryptography;
using System.Text;
using Org.BouncyCastle.Asn1;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Security;

namespace cevsn.encrn.rsa
{
    public class RSA
    {
        public string PubKey { get;set; }
        public string? PrivKey { get;set; }

        public RSA(string key)
        {
            this.PubKey = key;
        }
        public string Decrypt(byte[] data)
        {
            RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();

            string keyBase64 = PrivKey!.Replace("\r", "").Replace("\n", "").Replace(" ", "");
            byte[] privateInfoByte;

            privateInfoByte = Convert.FromBase64String(Encoding.UTF8.GetString(Convert.FromBase64String(keyBase64))); 
            

            rsa.ImportPkcs8PrivateKey(new ReadOnlySpan<byte>(privateInfoByte), out _);

            byte[] decryptedData = rsa.Decrypt(data, RSAEncryptionPadding.Pkcs1);
            
            return Encoding.UTF8.GetString(decryptedData);
        }

        public string Encrypt(string data)
        {
            try {
                RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
            
                string keyBase64 = PubKey.Replace("\r", "").Replace("\n", "").Replace(" ", "");
                byte[] publicInfoByte = Convert.FromBase64String(Encoding.UTF8.GetString(Convert.FromBase64String(keyBase64)));

                Asn1Object pubKeyObj = Asn1Object.FromByteArray(publicInfoByte);
                AsymmetricKeyParameter pubKey = PublicKeyFactory.CreateKey(publicInfoByte);
                RSAParameters rsaParams = DotNetUtilities.ToRSAParameters((RsaKeyParameters)pubKey);

                rsa.ImportParameters(rsaParams);

                byte[] dataToEncrypt = Encoding.UTF8.GetBytes(Convert.ToBase64String(Encoding.UTF8.GetBytes(data)));
                byte[] encryptedData = rsa.Encrypt(dataToEncrypt, false);

                return Convert.ToBase64String(encryptedData);
            } catch (Exception) {
                
                return Convert.ToBase64String(Encoding.UTF8.GetBytes(Encrypt("Output was too long.. :)" + "\n")));
            }
        }
    }
}