using System.Xml;
using System.Net.Sockets;
using System.Security.Cryptography;

namespace tevsn
{
    public class Tevsn
    {
        //create tcp client attributes and methods to connect to socket, send and receive socket
        public TcpClient client;
        public NetworkStream stream;
        public void Connect(string server, int port)
        {
            client = new TcpClient();
            client.Connect(server, port);
            stream = client.GetStream();
        }

        //create a method to send data to the socket
        public void Send(string data)
        {
            byte[] bytes = System.Text.Encoding.ASCII.GetBytes(data);
            stream.Write(bytes, 0, bytes.Length);
            stream.Flush();
        }

        //create a method to receive data from the socket
        public string Receive()
        {
            byte[] bytes = new byte[client.ReceiveBufferSize];
            int bytesRead = stream.Read(bytes, 0, client.ReceiveBufferSize);
            return System.Text.Encoding.ASCII.GetString(bytes, 0, bytesRead);
        }

        //create a method to close the socket connection
        public void Close()
        {
            stream.Close();
            client.Close();
        }

        //create aes cbc mode
public static byte[] aes_cbc_encrypt(byte[] data, byte[] key)
        {
            AesCryptoServiceProvider aes = new AesCryptoServiceProvider();
            aes.Mode = CipherMode.CBC;
            aes.Key = key;
            aes.GenerateIV();
            ICryptoTransform encryptor = aes.CreateEncryptor(aes.Key, aes.IV);
            MemoryStream ms = new MemoryStream();
            CryptoStream cs = new CryptoStream(ms, encryptor, CryptoStreamMode.Write);
            cs.Write(data, 0, data.Length);
            cs.FlushFinalBlock();
            byte[] encrypted = ms.ToArray();
            ms.Close();
            cs.Close();
            return encrypted;
        }

        //create aes cbc decrypt method

    }
}
