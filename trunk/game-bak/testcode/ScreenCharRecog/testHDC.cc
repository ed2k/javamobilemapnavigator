#include <windows.h> 
#include <stdio.h>

#include "charactermap.h"

#define MYMSG(str) MessageBox(NULL,str, "MsgBox waring", MB_OK); 
// Global variable 
 
HINSTANCE hinst; 
POINT HitPoint={100,100};
UINT hTimer;
 
// Function prototypes. 
 
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
			
			POINT p; p.x=100,p.y=100;
			HWND h=WindowFromPoint(p);
			//keybd_event('L',0,0,0);
			//keybd_event('S',0,0,0);
			//keybd_event(0xd,0,0,0);
			//keybd_event(0xa,0,0,0);


			//if(!AttachConsole(394))MYMSG("attach console failed");

			DWORD r;
			//if(!WriteConsole(GetStdHandle(STD_INPUT_HANDLE),"tttaaabbb",7,&r,0))MYMSG("write console failed");
			//if(GetStdHandle(STD_INPUT_HANDLE)==INVALID_HANDLE_VALUE)	MYMSG("get stdout fail");
			//printf("to standard output\n");

			InvalidateRect(hwnd,NULL,TRUE);
		}
	case WM_LBUTTONDOWN:
     	{
			//SetCapture(hwnd);
			//::mouse_event(MOUSEEVENTF_MOVE | MOUSEEVENTF_LEFTDOWN,5,5,0,0);	
			break;
		}
	case WM_LBUTTONUP:
     	{
			//ReleaseCapture();
			break;
		}
	case WM_MOUSEMOVE:
		{
			//InvalidateRect(hwnd,NULL,TRUE);
			break;
		}
 	case WM_PAINT:
		{
			PAINTSTRUCT ps;
			HDC hdc = BeginPaint(hwnd, &ps);
	
   	 	HWND shwnd= GetDesktopWindow();
    		HDC sdc= GetWindowDC( shwnd);
			HDC memDC =  CreateCompatibleDC(sdc);
 
      	//BitBlt(hdc,0,0,300,100,sdc,HitPoint.x,HitPoint.y,SRCCOPY);

	   	//BitBlt(hdc,0,0,300,100,memDC,0,0,SRCCOPY);

			BYTE map[50][150];
			for(int x=0;x<150;++x)
				for(int y=0;y<50;++y)
				{
  					COLORREF c=GetPixel(sdc,x-4+HitPoint.x,y-8+HitPoint.y);
					BYTE grey = (GetRValue(c) + GetGValue(c) + GetBValue(c))/3;
//					grey = (grey>0x7f)?0xff:0;
					map[y][x]=(grey>0x7f)?0xff:0;
				  // SetPixel(hdc,x,y,RGB(grey,grey,grey));					
				}	



			CharacterMap c= CharacterMap((byte *)map,150,50);
			const byte* target = c.GetMap();

// 			for(int x=0;x<c.GetWidth();++x)
// 			{
//				for(int y=0;y<c.GetHeight();++y)
 			for(int x=0;x<32;++x)
 			{
				for(int y=0;y<32;++y)

				{
					BYTE grey = *(target+y*32+x);
					for(int i=4*x;i<4*(x+1);++i)for(int j=100+4*y;j<100+4*(y+1);++j)
						SetPixel(hdc,i,j,RGB(grey,grey,grey));					
				}	
			}		   


			RECT rect;
			char str[300];
			int result = c.GetResult();
			sprintf(str,"x=%d,y=%d,result=%d",HitPoint.x,HitPoint.y,result);
			c.ToString(str);
			GetClientRect(hwnd,&rect);
			DrawText(hdc,str,-1, &rect, DT_SINGLELINE | DT_CENTER | DT_VCENTER);
			printf("%s\n",str);



    		DeleteDC(memDC);
    	 	ReleaseDC(shwnd,sdc);


			EndPaint(hwnd, &ps);

			break;
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
    wcx.hbrBackground = 0;//(HBRUSH )COLOR_WINDOW +1;
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
        500,       // default width 
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
