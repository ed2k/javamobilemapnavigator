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
    DLGTEMPLATE dlgTpl={WS_POPUPWINDOW
    ,NULL,0,10,10,100,100};

    // Create the main window. 
    hwnd = CreateDialogIndirectParam(hinstance,&dlgTpl,NULL,NULL,NULL);
    if (!hwnd) 
    {
        MYMSG("create dialog failed");
        return FALSE; 
    }
 
    // Show the window and send a WM_PAINT message to the window 
    // procedure. 
 
    ShowWindow(hwnd, nCmdShow); 

    UpdateWindow(hwnd); 
    return TRUE; 
 
} 
