using System.Security.Cryptography;
using System.Text;

namespace cevsn.encrn
{
    public class Blowfish
    {
        private String pass;
        private Blowfish bf = Blowfish.Create();

        public Blowfish(String pass) 
        {
            this.pass = pass;
        }

        public String Encrypt(String toEnc)
        {
            toEnc = Convert.ToBase64String(Encoding.UTF8.GetBytes(toEnc));

            byte[] b64Bytes = System.Text.Encoding.UTF8.GetBytes(toEnc);

            byte[] encr = bf.EncryptBytes(b64Bytes);

            return Convert.ToBase64String(encrn);
        }

        public String Decrypt(String toDec) 
        {
            byte[] b64Bytes = = Convert.FromBase64String(toDec);

            byte[] decrn = bf.DecryptBytes(b64Bytes);

            return Encoding.UTF8.GetString(Convert.FromBase64String(decrn));
        }

        public void init()
        {
            bf.Mode = CipherMode.CBC;
            bf.Padding = PaddingMode.PKCS7;
            bf.key = this.key.GetBytes();
        }


    }
}