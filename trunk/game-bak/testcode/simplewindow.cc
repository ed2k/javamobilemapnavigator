#include <windows.h> 
#include <stdio.h>

#define MYMSG(str) MessageBox(NULL,str, "MsgBox waring", MB_OK); 
// Global variable 
 
HINSTANCE hinst; 
 
// Function prototypes. 
 
int WINAPI WinMain(HINSTANCE, HINSTANCE, LPSTR, int); 
ATOM InitApplication(HINSTANCE); 
BOOL InitInstance(HINSTANCE, int); 
LRESULT CALLBACK MainWndProc(HWND , UINT, WPARAM, LPARAM);

LRESULT CALLBACK MainWndProc(HWND hwnd, UINT Msg, WPARAM wParam, LPARAM lParam)
{
   if(WM_DESTROY==Msg)
   {
      PostQuitMessage(NULL);
      return 0;
      //break;
   }
    return DefWindowProc(hwnd, Msg, wParam, lParam);
}; 
 
// Application entry point. 
 
int WINAPI WinMain(HINSTANCE hinstance, HINSTANCE hPrevInstance, 
    LPSTR lpCmdLine, int nCmdShow) 
{ 
       WNDCLASSEX  wcx;
        BOOL result = GetClassInfoEx(hinstance, "BUTTON", &wcx);
        if (!result) MYMSG("retrieve class failed");
        char buffer[300];
        sprintf(buffer, "%x %x %x %x",     
        wcx.cbSize,    wcx.style,   wcx.hbrBackground);
    //MYMSG(buffer);
//    return FALSE; 


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
    wcx.cbWndExtra = 0;                // no extra window memory 
    wcx.hInstance = hinstance;         // handle to instance 
    wcx.hIcon = LoadIcon(NULL, IDI_APPLICATION);              // predefined app. icon 
    wcx.hCursor = LoadCursor(NULL,   IDC_ARROW);                    // predefined arrow 
    wcx.hbrBackground = (HBRUSH )COLOR_WINDOW +1;
    wcx.lpszMenuName = NULL ;    // name of menu resource 
    wcx.lpszClassName = "MainWClass";  // name of window class 
    wcx.hIconSm = LoadIcon(NULL, IDI_APPLICATION); 
    // Register the window class. 
 
    return RegisterClassEx(&wcx); 
} 
 
BOOL InitInstance(HINSTANCE hinstance, int nCmdShow) 
{ 
    HWND hwnd; 
 
    // Save the application-instance handle. 
 
    hinst = hinstance; 
 
    // Create the main window. 
 hwnd = CreateDialogParam(hinstance,"dig",NULL,NULL,NULL);
  /*  hwnd = CreateWindowEx(   
        0,                // style 
        "MainWClass",        // name of window class 
        NULL,            // title-bar string 
        WS_DLGFRAME, // top-level window 
        CW_USEDEFAULT,       // default horizontal position 
        CW_USEDEFAULT,       // default vertical position 
        CW_USEDEFAULT,       // default width 
        CW_USEDEFAULT,       // default height 
         NULL,         // no owner window 
         NULL,        // use class menu 
        hinstance,           // handle to application instance 
         NULL);      // no window-creation data 
 */
    if (!hwnd) 
        return FALSE; 
 
    // Show the window and send a WM_PAINT message to the window 
    // procedure. 
 
    ShowWindow(hwnd, nCmdShow); 
    //AnimateWindow(hwnd,1,AW_SLIDE);

    UpdateWindow(hwnd); 
    return TRUE; 
 
} 
