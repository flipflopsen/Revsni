

#include "pch.h"
#include <metahost.h>
#pragma comment(lib, "mscoree.lib")


void callCSharp()
{
	HRESULT hr;
	ICLRMetaHost* pMetaHost = NULL;
	ICLRRuntimeInfo* pRuntimeInfo = NULL;
	ICLRRuntimeHost* pClrRuntimeHost = NULL;

	// Bind to the CLR runtime..
	hr = CLRCreateInstance(CLSID_CLRMetaHost, IID_PPV_ARGS(&pMetaHost));
	hr = pMetaHost->GetRuntime(L"v4.0.30319", IID_PPV_ARGS(&pRuntimeInfo));
	hr = pRuntimeInfo->GetInterface(CLSID_CLRRuntimeHost,
	IID_PPV_ARGS(&pClrRuntimeHost));

	// Push the big START button shown above
	hr = pClrRuntimeHost->Start();

	// Okay, the CLR is up and running in this (previously native) process.
	// Now call a method on our managed C# class library.
	DWORD dwRet = 0;
	hr = pClrRuntimeHost->ExecuteInDefaultAppDomain(
	L"C:\\Users\\Flipflop\\Desktop\\Cevsn.dll", //<--
	L"cevsn.Cevsn", L"Cevsin", L"lul", &dwRet);

	// Optionally stop the CLR runtime (we could also leave it running)
	//hr = pClrRuntimeHost->Stop();

	// Don't forget to clean up.
	//pClrRuntimeHost->Release();
}

	BOOL APIENTRY DllMain(
	HANDLE hModule, // Handle to DLL module
	DWORD ul_reason_for_call,
	LPVOID lpReserved) // Reserved
	{
		switch (ul_reason_for_call)
		{
			case DLL_PROCESS_ATTACH:
				CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)callCSharp, NULL, 0, NULL);

				return true;
			case DLL_PROCESS_DETACH:
				break;
		};
	return true;
}

