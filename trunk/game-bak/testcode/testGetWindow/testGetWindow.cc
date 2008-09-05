#include <windows.h> 
#include <stdio.h>
#include <wingdi.h>
//#include "dllwrapper.h"
class DLLClass
{
   public:
    DLLClass(const char * dllName);
    HINSTANCE GetHandle();
    ~DLLClass();
   private:
    HINSTANCE HLib;
};

static DLLClass MHOOKDLL("testGetWindow.DLL");
DLLClass::DLLClass(const char * dllName)
 :HLib(NULL)

{
   HLib = LoadLibrary(dllName);
}

DLLClass::~DLLClass()
{
   if(HLib)
   { 
      FreeLibrary(HLib);
   }
}

HINSTANCE DLLClass::GetHandle()
{
   return HLib;
}

//--------------------MHOOK.DLL API wrapper ------------------
HWND  InstallHook(HWND hwnd)
{
    HINSTANCE hLib = MHOOKDLL.GetHandle();
    if(!hLib)
    { 
       return NULL;
    }
    FARPROC WINAPI f= GetProcAddress(hLib, "InstallHook");
    if(!f){
       return NULL;
    }
    typedef HWND (*FPTYPE)(HWND);
    FPTYPE fp=(FPTYPE)f;
    return (*fp)(hwnd);
}

void UninstallHook()
{
    HINSTANCE hLib = MHOOKDLL.GetHandle();
    if(!hLib)
    { 
       return;
    }
    FARPROC WINAPI f= GetProcAddress(hLib, "UninstallHook");
    if(!f){
       return;
    }
    typedef void (*FPTYPE)();
    FPTYPE fp=(FPTYPE)f;
    return (*fp)();

}


#define MYMSG(str) MessageBox(NULL,str, "MsgBox waring", MB_OK); 
#define STR(s) 

#define IDD_MAINDLG                      101
#define IDC_CLASSNAME                    1000
#define IDC_HANDLE                       1001
#define IDC_WNDPROC                      1002
#define IDC_HOOK                         1004
#define IDC_EXIT                         1005
#define IDC_MYTRACE			     1006
#define IDC_OWNER         1011
#define IDC_PARENT         1012
#define IDC_CHILD         1013
#define IDC_FIRST         1014
#define IDC_PREV         1015
#define IDC_NEXT         1016
#define IDC_LAST         1017

#define WM_MOUSEHOOK  WM_USER+6


// Global variable 
int cntMessages = 0;
BOOL HookFlag = FALSE;
HHOOK hHook = NULL;
HINSTANCE hInstance=NULL;
HWND hEdit = NULL;
HWND hLineEdit[3];


// Function prototypes. 
int WINAPI WinMain(HINSTANCE, HINSTANCE, LPSTR, int); 
BOOL CALLBACK DlgWndProc(HWND hwnd, UINT Msg, WPARAM wParam, LPARAM lParam);
void MessageToString (UINT msg, char * output);
void UpdateDialogItem(HWND hdlg, UINT item, const char * input);


void UpdateDialogItem(HWND hdlg, UINT item, const char* input)
{
    static char buffer[128];
    GetDlgItemText(hdlg,item,buffer,128);
    if(lstrcmpi(buffer,input)!=0) SetDlgItemText(hdlg,item,input);   
}
// output 
void MessageToString (UINT msg, char * output)
{
// 0 -15                        0      1           2            3         4           5          6          7          8            9   a        b           c          d          e               f
static const char* strmap[]={"NULL", "1:CREATE", "2:DESTROY", "3:MOVE", "4:UNKNOWN", "5:SIZE", "6:ACTIVE", "SETFPCUS","KILLFOCUS","??","ENABLE","SETREDRAW","SETTEXT","GETTEXT","GETTEXTLENGTH","PAINT"
};

//{"0:","1:", "2:","3:","4:","5:","6:","7:","8:","9:","A:","B:","C:","D:","E:","F:", }

    if((0<= msg) && (msg <= 15) )
    {
       sprintf(output,"%lx:WM_%s",msg,strmap[msg]);
    }
    else if(15< msg < 0x100)
    {
       sprintf(output,"%lx:WM_??",msg);
    }
    else
    {
        sprintf(output,"%lx:??",msg);
    }
}

BOOL CALLBACK DlgWndProc(HWND hwnd, UINT Msg, WPARAM wParam, LPARAM lParam)
{
   switch(Msg)
   {
   case WM_DESTROY:
    {
      if(HookFlag)UninstallHook ();
      EndDialog(hwnd,0);
            //break;
    }
   case WM_INITDIALOG:
    {
	 hEdit=GetDlgItem( hwnd, IDC_MYTRACE);
       hLineEdit[0] = GetDlgItem( hwnd, IDC_CLASSNAME);
       hLineEdit[1] = GetDlgItem( hwnd, IDC_HANDLE);
       hLineEdit[2] = GetDlgItem( hwnd, IDC_WNDPROC);
       

       break;
    }
   case WM_PAINT:
    {
      //SetDlgItemText(hwnd,IDC_MYTRACE,"testing \r\n aaar\n\r\n\r\n");
      break;
    }
   case WM_COMMAND:
    {
      int bn = HIWORD(wParam);
      int id = LOWORD(wParam);
      if((IDC_HOOK==id)  && (bn==BN_CLICKED))
      {
         if(!HookFlag)
         {
            HWND h=InstallHook(hwnd);
            if(h)
            {
               HookFlag = TRUE;
               SetDlgItemText(hwnd,IDC_HOOK,"UnHook");
               cntMessages = 0;
            }
         }
         else
         {
            UninstallHook();
            SetDlgItemText(hwnd,IDC_HOOK,"Hook");
            HookFlag = FALSE;
         }
      }
      break;      
    }
    case WM_MOUSEHOOK:
    {
       CWPSTRUCT cwp;
       //cwp.lParam = lParam;
       //cwp.wParam = wParam;
       //cwp.message = GetHookedMessage();
       cwp.hwnd = (HWND)wParam; //GetHookedHWnd(); 
/*
       if( (hEdit != cwp.hwnd) && (cwp.message !=WM_SETTEXT) && (cwp.message !=WM_GETTEXT)
          && (cwp.hwnd != hLineEdit[0]) && (cwp.hwnd != hLineEdit[1]) && (cwp.hwnd!=hLineEdit[2])
          && (cwp.message != EM_REPLACESEL) &&(cntMessages <10000) 
        )
        */
        if(TRUE)
        {
           static char str[200];
           static char buffer[200];
 
           GetDlgItemText(hwnd,IDC_HANDLE,buffer,128);
	     wsprintf(str,"%lx",cwp.hwnd);
	     if(lstrcmpi(str, buffer)!=0) SetDlgItemText(hwnd,IDC_HANDLE,str);

	     GetDlgItemText(hwnd,IDC_CLASSNAME,buffer,128);
	     GetClassName(cwp.hwnd,str,128);
	     if(lstrcmpi(buffer,str)!=0) SetDlgItemText(hwnd,IDC_CLASSNAME,str);
	     
           GetDlgItemText(hwnd,IDC_WNDPROC,buffer,128);	     
	     wsprintf(str,"%lx",GetClassLong(cwp.hwnd,GCL_WNDPROC));
	     if(lstrcmpi(buffer,str)!=0) SetDlgItemText(hwnd,IDC_WNDPROC,str);

// GW_HWNDNEXT 2 GW_HWNDPREV 3 GW_CHILD 5 GW_HWNDFIRST 0 GW_HWNDLAST 1 GW_OWNER 4
   const UINT GWMAP[6]={IDC_FIRST,IDC_LAST,IDC_NEXT,IDC_PREV,IDC_OWNER,IDC_CHILD};

           wsprintf(str,"%x",GetParent(cwp.hwnd));
           UpdateDialogItem(hwnd, IDC_PARENT ,str);
       
           for(int i=0;i<6;i++)
           {
              wsprintf(str,"%x",GetWindow(cwp.hwnd,i));
              UpdateDialogItem(hwnd, GWMAP[i] ,str);
           }

           HWND a=cwp.hwnd;
           //static char str[128];
           //static char buffer[128];
           sprintf(str,"hwnd:%x ",cwp.hwnd);
           do{
            a=GetParent(a);
            sprintf(buffer,"-> %x ",a);
            strcat(str,buffer);
           }while(a!=NULL);
            UpdateDialogItem(hwnd, IDC_MYTRACE, str);
           
           //if (cntMessages <100)
           if(FALSE)
           {
             HWND parent, child,first,last,next,prev,owner;
             parent = GetParent(cwp.hwnd);
             child = GetWindow(cwp.hwnd,GW_CHILD);
             first = GetWindow(cwp.hwnd,GW_HWNDFIRST);
             last = GetWindow(cwp.hwnd,GW_HWNDLAST);
             next = GetWindow(cwp.hwnd,GW_HWNDNEXT);
             prev = GetWindow(cwp.hwnd,GW_HWNDPREV);
             owner = GetWindow(cwp.hwnd,GW_OWNER);
             char tmp[100];tmp[20]='\0';
             MessageToString(cwp.message, tmp);
	       sprintf(str,"hwnd:%lx m:%s pc:%x,%x fl:%x,%x np:%x,%x o:%x wl:%lx,%lx \r\n", 
              cwp.hwnd, tmp, 
              parent,child, first,last, next,prev, owner,
              wParam,lParam);	
             SendMessage(hEdit,EM_REPLACESEL ,0, (LPARAM)str);
           }
	     cntMessages++;
       }  
       break;
    }
    default:
    {
      DefWindowProc(hwnd, Msg, wParam, lParam);
     }
   } //endof switch

   return 0L;   
} 

// Application entry point. 


int WINAPI WinMain(HINSTANCE hinstance, HINSTANCE hPrevInstance, 
    LPSTR lpCmdLine, int nCmdShow) 
{ 
    //HINSTANCE hinst=GetModuleHandle(NULL);
    hInstance = hinstance;
    DialogBoxParam(hinstance,MAKEINTRESOURCE(IDD_MAINDLG),NULL, DlgWndProc,0);
    ExitProcess(0);
} 
 
