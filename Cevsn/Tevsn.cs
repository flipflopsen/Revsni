using System;
using System.Xml;
using System.Net.Sockets;
using System.Security.Cryptography;

namespace tevsn
{
    public class Tevsn
    {
        public String IP { get;set; }
        public int Port { get;set; }


        public byte[] Key { get;set; }

        public byte[] IV { get;set; }

        public TcpClient tcpClient = new TcpClient();
        
        public Tevsn(string ip, int port, byte[] key, byte[] iv)
        {
            this.IP = ip;
            this.Port = port;
            this.Key = key;
            this.IV = iv;
        }

        //try to connect, if success, return true
        public bool Connect()
        {
            try
            {
                tcpClient.Connect(IP, Port);
                return true;
            }
            catch (Exception e)
            {   
                e.ToString();
                return false;
            }
        }

        //check if connection is alive
        public bool IsConnected()
        {
            try
            {
                if (tcpClient.Client.Poll(0, SelectMode.SelectRead))
                {
                    byte[] buff = new byte[1];
                    if (tcpClient.Client.Receive(buff, SocketFlags.Peek) == 0)
                    {
                        return false;
                    }
                }
                return true;
            }
            catch (Exception e)
            {
                e.ToString();
                return false;
            }
        }

        public void Send(String data)
        {
            NetworkStream clientStream = tcpClient.GetStream();
            string message = Encrypt(data);
            byte[] outStream = System.Text.Encoding.ASCII.GetBytes(message);
            clientStream.Write(outStream, 0, outStream.Length);
            clientStream.Flush();
        }

        public String Receive()
        {
            String message;
            NetworkStream clientStream = tcpClient.GetStream();
            byte[] inStream = new byte[tcpClient.ReceiveBufferSize];
            clientStream.Read(inStream, 0, tcpClient.ReceiveBufferSize);
            byte[] b64 = Convert.FromBase64String(System.Text.Encoding.ASCII.GetString(inStream));
            message = Decrypt(b64);
            return message;
        }

        public String Decrypt(byte[] encrypted)
        {
            string plaintext = "";

            using(Aes aesCBC = Aes.Create())
            {
                aesCBC.Key = this.Key;

                byte[] cipherText = new byte[encrypted.Length - this.IV.Length];

                Array.Copy(encrypted, IV, IV.Length);
                Array.Copy(encrypted, IV.Length, cipherText, 0, cipherText.Length);

                aesCBC.IV = this.IV;

                aesCBC.Mode = CipherMode.CBC;

                ICryptoTransform decryptor = aesCBC.CreateDecryptor(aesCBC.Key, aesCBC.IV);

                using (var msDecrypt = new MemoryStream(cipherText))
                {
                    using (var csDecrypt = new CryptoStream(msDecrypt, decryptor, CryptoStreamMode.Read))
                    {
                        using (var srDecrypt = new StreamReader(csDecrypt))
                        {
                            plaintext = srDecrypt.ReadToEnd();
                            return plaintext;
                        }
                    }
                }

            }
        }

        public String Encrypt(string message)
        {
            byte[] encrypted;
            byte[] IV = this.IV;

            using (Aes aesAlg = Aes.Create())
            {
                aesAlg.Key = this.Key;

                aesAlg.IV = IV;

                aesAlg.Mode = CipherMode.CBC;

                var encryptor = aesAlg.CreateEncryptor(aesAlg.Key, aesAlg.IV);

                using (var msEncrypt = new MemoryStream())
                {
                    using (var csEncrypt = new CryptoStream(msEncrypt, encryptor, CryptoStreamMode.Write))
                    {
                        using (var swEncrypt = new StreamWriter(csEncrypt))
                        {
                            swEncrypt.Write(message);
                        }
                        encrypted = msEncrypt.ToArray();
                    }
                }
            }
            var combinedIvCt = new byte[IV.Length + encrypted.Length];
            Array.Copy(IV, 0, combinedIvCt, 0, IV.Length);
            Array.Copy(encrypted, 0, combinedIvCt, IV.Length, encrypted.Length);

            return Convert.ToBase64String(combinedIvCt);

        }

    }
}
