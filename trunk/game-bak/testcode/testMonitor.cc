
#include <stdio.h>
#include <iostream>
#include <string>
#include <list>
#include <windows.h> 
using namespace std;

#define MYMSG(str) MessageBox(NULL,str, "MsgBox waring", MB_OK); 
// Global variable 
 
HINSTANCE hinst; 
POINT HitPoint={100,100};
UINT hTimer;
list<string> strList;
 

// Function prototypes. 
list<string>::iterator find(list<string>::iterator b, list<string>::iterator e, const string& s)
{
	for(list<string>::iterator i=b;i!=e;i++)if(*i == s)return i;
	return e;
};

 
int WINAPI WinMain(HINSTANCE, HINSTANCE, LPSTR, int); 
ATOM InitApplication(HINSTANCE); 
BOOL InitInstance(HINSTANCE, int); 
LRESULT CALLBACK MainWndProc(HWND , UINT, WPARAM, LPARAM);

LRESULT CALLBACK MainWndProc(HWND hwnd, UINT Msg, WPARAM wParam, LPARAM lParam)
{
   switch(Msg)
   {
   case WM_DESTROY:
   	{
			KillTimer(hwnd,hTimer);
      	PostQuitMessage(0);
       	break;
      }
	case WM_TIMER:
		{
			GetCursorPos(&HitPoint);
			//::mouse_event(MOUSEEVENTF_MOVE | MOUSEEVENTF_LEFTDOWN,5,5,0,0);	
			//::mouse_event(MOUSEEVENTF_LEFTUP,5,5,0,0);	
			
			HWND h=WindowFromPoint(HitPoint);
			HWND child=ChildWindowFromPoint(h,HitPoint);
			if(child!=NULL)h=child;
			
			int n=GetWindowTextLength(h); 
			
			//keybd_event('L',0,0,0);
			//keybd_event('S',0,0,0);
			//keybd_event(0xd,0,0,0);
			//keybd_event(0xa,0,0,0);

			char str[100];
			//GetWindowText(h,str,100);	string s(str);
			SendMessage(h,WM_GETTEXT,(WPARAM)100,(LPARAM)str); string s(str);
			GetClassName(h,str,100); string className(str);
			//cout << "wt:" << s << endl;
			s = className + ":"+s;
			if(find(strList.begin(),strList.end(),s)==strList.end())
			{
				strList.push_back(s);
				cout<<"add "<<strList.size()<<":"<<strList.back()<<endl;
			}

			//cout<<s<<endl;

		}

   default:
   	return DefWindowProc(hwnd, Msg, wParam, lParam);
  	}
   return 0;

}; 
 
// Application entry point. 
 
int WINAPI WinMain(HINSTANCE hinstance, HINSTANCE hPrevInstance, 
    LPSTR lpCmdLine, int nCmdShow) 
{ 
       WNDCLASSEX  wcx;

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
    wcx.style = CS_HREDRAW | CS_VREDRAW;     // redraw if size changes 
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
     hwnd = CreateWindowEx(   
        0 ,                // style 
        "MainWClass",        // name of window class 
        NULL,            // title-bar string 
        WS_OVERLAPPEDWINDOW, // top-level window 
        200,       // default horizontal position 
        300,       // default vertical position 
        300,       // default width 
        300,       // default height 
         NULL,         // no owner window 
         NULL,        // use class menu 
        hinstance,           // handle to application instance 
         NULL);      // no window-creation data 
 
    if (!hwnd) 
        return FALSE; 
 
    // Show the window and send a WM_PAINT message to the window 
    // procedure. 
 
    ShowWindow(hwnd, nCmdShow); 

    UpdateWindow(hwnd); 
	 hTimer=SetTimer(hwnd,0xff,1000,0);
    return TRUE; 
 
} 
