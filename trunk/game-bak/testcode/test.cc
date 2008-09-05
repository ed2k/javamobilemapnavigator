#include <windows.h> 
#include <stdio.h>
#include <wingdi.h>

#define MYMSG(str) MessageBox(NULL,str, "MsgBox waring", MB_OK); 
#define STR(s) 
// Global variable 
 
HINSTANCE hinst; 
 
// Function prototypes. 
 
int WINAPI WinMain(HINSTANCE, HINSTANCE, LPSTR, int); 
ATOM InitApplication(HINSTANCE); 
BOOL InitInstance(HINSTANCE, int); 
LRESULT CALLBACK MainWndProc(HWND , UINT, WPARAM, LPARAM);
BOOL CALLBACK DlgWndProc(HWND hwnd, UINT Msg, WPARAM wParam, LPARAM lParam);

BOOL CALLBACK DlgWndProc(HWND hwnd, UINT Msg, WPARAM wParam, LPARAM lParam)
{
   switch(Msg)
   {
   case WM_DESTROY:
    {
      PostQuitMessage(NULL);
      return 0;
      //break;
    }
   case WM_PAINT:
    {
      PAINTSTRUCT ps;
      RECT rect;
      HDC hdc=BeginPaint(hwnd, &ps);
      GetClientRect(hwnd, &rect);
	DrawText(hdc,"great",-1, &rect, DT_SINGLELINE | DT_CENTER | DT_VCENTER);
	EndPaint(hwnd, &ps);
      break;
    }
    default:
    {
      DefWindowProc(hwnd, Msg, wParam, lParam);
     }
   } //endof switch

   return 0L;   
} 

LRESULT CALLBACK MainWndProc(HWND hwnd, UINT Msg, WPARAM wParam, LPARAM lParam)
{
   if (WM_DESTROY==Msg)
   {
      PostQuitMessage(0);
      return 0;
      //break;
   }
    return DefWindowProc(hwnd, Msg, wParam, lParam);
} 
 
// Application entry point. 
 
int WINAPI WinMain(HINSTANCE hinstance, HINSTANCE hPrevInstance, 
    LPSTR lpCmdLine, int nCmdShow) 
{ 

    MSG msg; 
    ATOM hApp = TRUE;
    hApp = InitApplication(hinstance);
    if (!hApp) 
    {
        MYMSG("if (!InitApplication(hinstance) fail");
            return FALSE; 
    }

     if (!InitInstance(hinstance, nCmdShow)) 
    {
       MYMSG("initInstance failed");
        return FALSE; 
    }
  
 
    while (GetMessage(&msg, (HWND) NULL, 0, 0) != 0 && GetMessage(&msg, (HWND) NULL, 0, 0) != -1) 
    { 
        TranslateMessage(&msg); 
        DispatchMessage(&msg); 
    } 
    if(!UnregisterClass((const char*)hApp, hinstance))
    {
        MYMSG("Unregister class failed");
    }
    return msg.wParam; 
    //    UNREFERENCED_PARAMETER(lpCmdLine); 
} 
 
ATOM InitApplication(HINSTANCE hinstance) 
{ 
    WNDCLASSEX wcx; 
 
    // Fill in the window class structure with parameters 
    // that describe the main window. 
 
    wcx.cbSize = sizeof(wcx);          // size of structure 
    wcx.style = 0;                    // redraw if size changes 
    wcx.lpfnWndProc = MainWndProc;     // points to window procedure 
    wcx.cbClsExtra = 0;                // no extra class memory 
    wcx.cbWndExtra = DLGWINDOWEXTRA;                // extra memory for dialog box 
    wcx.hInstance = hinstance;         // handle to instance 
    wcx.hIcon = LoadIcon(NULL, IDI_APPLICATION);              // predefined app. icon 
    wcx.hCursor = LoadCursor(NULL,   IDC_ARROW);                    // predefined arrow 
    wcx.hbrBackground = (HBRUSH )COLOR_WINDOW +1;
    wcx.lpszMenuName = NULL ;    // name of menu resource 
    wcx.lpszClassName = "DLGCLASS";  // name of dialog class 
    wcx.hIconSm = LoadIcon(NULL, IDI_APPLICATION); 
    // Register the window class. 
 
    return RegisterClassEx(&wcx); 
} 
 
BOOL InitInstance(HINSTANCE hinstance, int nCmdShow) 
{ 
    HWND hwnd; 
 
    // Save the application-instance handle. 
 
    hinst = hinstance; 
    DLGTEMPLATE dlgTpl={WS_POPUP |WS_CAPTION|WS_SYSMENU
    ,0,0,10,10,100,100};

    // Create the main window. 
    hwnd = CreateDialogIndirectParam(hinstance,&dlgTpl,0,DlgWndProc,0);
    if (!hwnd) 
    {
        MYMSG("create dialog failed");
        return FALSE; 
    }
 
    // Show the window and send a WM_PAINT message to the window 
    // procedure. 
    HINSTANCE hLib = LoadLibrary("GDI32.DLL");
    if(!hLib)
    { MYMSG("hLib is NULL");
       return FALSE;
       }
    FARPROC WINAPI f= GetProcAddress(hLib, "CreateRectRgn");
    if(!f){
     MYMSG("fail getProcAddress");
     }
    typedef HRGN (WINAPI *FPTYPE)(int,int,int,int);
    FPTYPE fp=(FPTYPE)f;
    HRGN hrgn= (*fp)(0,10,200,205);
    FreeLibrary(hLib);
    //RECT rect={10,10,30,30};
   

    ShowWindow(hwnd, nCmdShow); 

    UpdateWindow(hwnd); 

    SetWindowRgn(hwnd, hrgn,TRUE);
    return TRUE; 
 
} 
