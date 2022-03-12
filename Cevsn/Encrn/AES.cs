using System.Security.Cryptography;
using System.Text;

namespace cevsn.encrn
{
    public class AES
    {
        private byte[]? key;
        private byte[] iv;
        private string pass;
        private Aes aesCipher = Aes.Create();
        public AES(string pass, byte[] iv)
        {
            this.pass = pass;
            this.iv = iv;
            
            generateKey(pass);
            init();
        }

        public void generateKey(string pass)
        {
            byte[] salt = new byte[]{172, 137, 25, 56, 156, 100, 136, 211, 84, 67, 96, 10, 24, 111, 112, 137, 3};
            int iterations = 1024;
            var rfc2898 = new System.Security.Cryptography.Rfc2898DeriveBytes(pass, salt, iterations);

            key = rfc2898.GetBytes(16);

            String keyB64 = Convert.ToBase64String(key);

            System.Console.WriteLine("Key: " + keyB64);
        }

        public void init()
        {
            aesCipher.KeySize = 128;
            aesCipher.BlockSize = 128;
            aesCipher.Mode = CipherMode.CBC;
            aesCipher.Padding = PaddingMode.PKCS7;
            aesCipher.Key = key!;
            aesCipher.IV = iv;
        }

        public string encrypt(string data)
        {
            string toEnc = Convert.ToBase64String(Encoding.UTF8.GetBytes(data));

            byte[] b = System.Text.Encoding.UTF8.GetBytes(toEnc);
            ICryptoTransform encryptTransform = aesCipher.CreateEncryptor();
            byte[] ctext = encryptTransform.TransformFinalBlock(b, 0, b.Length);

            return Convert.ToBase64String(ctext);
        }

        public string decrypt(string enc)
        {
            Console.Write("Trying to decrypt: " + enc + "\n");

            byte[] decoded = Convert.FromBase64String(enc);
            ICryptoTransform decryptTransform = aesCipher.CreateDecryptor();
            byte[] plainText = decryptTransform.TransformFinalBlock(decoded, 0, decoded.Length);

            System.Console.WriteLine("Decrypted: " + Encoding.UTF8.GetString(Convert.FromBase64String(Encoding.UTF8.GetString(plainText))) + "\n");

            return Encoding.UTF8.GetString(Convert.FromBase64String(Encoding.UTF8.GetString(plainText)));
        }
    }
}