#include <windows.h> 
#include <stdio.h>

#include <list>
#include <algorithm>
#include <stack>

using namespace std;

typedef unsigned long word32;

#define MYMSG(str) MessageBox(NULL,str, "MsgBox waring", MB_OK); 

class Point
{ 
public:
	int x; int y;
	Point():x(0),y(0){};
	Point(int px, int py):x(px),y(py){};
	void operator =(const Point& p){x=p.x;y=p.y;};
	void Up(int d=1){y-=d;};
	void Left(int d=1){x-=d;};
	void Down(int d=1){y+=d;};
	void Right(int d=1){x+=d;};
	Point Up(int d=1) const{return Point(x,y-d);};
	Point Left(int d=1) const{return Point(x-d,y);};
	Point Down(int d=1) const{return Point(x,y+d);};
	Point Right(int d=1) const{return Point(x+d,y);};
	Point Move(Point dir, int d=1) const{ 
		//printf("point.move %d,%d %d\n",dir.x,dir.y,d);
		//printf("point.move return %d,%d\n",x+d*dir.x,y+d*dir.y);
		return Point(x+d*dir.x,y+d*dir.y);};
	bool operator ==(const Point& p)const {return (x==p.x&&y==p.y);};
	bool operator !=(const Point& p)const {return (x!=p.x || y!=p.y);};
};

class FillPoints {
public:
	BYTE Color;
	int Score;
	list<Point> Candidates;// empty place
	list<Point> Blocks; // unwanted places
	FillPoints(){};
	FillPoints(FillPoints& f){*this=f;};
	const FillPoints& operator =(FillPoints& f){
		Color=f.Color;Score=f.Score;
		for(list<Point>::iterator i=f.Candidates.begin();i!=f.Candidates.end();++i)Candidates.push_back(*i);		
		for(list<Point>::iterator i=f.Blocks.begin();i!=f.Blocks.end();++i)Blocks.push_back(*i);
		return *this;		
	}
};



// Global variable 
 
HINSTANCE hinst; 
POINT HitPoint={100,100};
//list<word32> m;
list<word32> Pattern; // should have 9 
BYTE Map[9][9];
int Width=9; int Height=9;
bool AIRunning = true;

int Evaluate5(const BYTE* const seq, FillPoints* fp);

bool IsValid(int x,int y) {
	return (x>=0 && x<Width && y>=0 && y<Height);
}
bool IsValid(Point p) {return IsValid(p.x,p.y);}

bool IsInLine(Point p1,Point p2)
{
	if(p1.x==p2.x || p1.y==p2.y){return true;}
	else if((p1.x>p2.x && p1.y>p2.y) || (p1.x<p2.x && p1.y<p2.y)){
		return (p1.x-p2.x)==(p1.y-p2.y);
	} 
	return (p1.x-p2.x)==(p2.y-p1.y);
}
Point GetLineDirection(Point p1,Point p2)
{
	int x,y;
	if(p1.x==p2.x && p1.y==p2.y){x=0; y=0;}
	else if(p1.y==p2.y){ x=1;y=0;} 
	else if(p1.x==p2.x){ x=0;y=1;}
	else if((p1.x>p2.x && p1.y>p2.y) || (p1.x<p2.x && p1.y<p2.y)){
		x=1;y=1;
	} else {x=1;y=-1;}
	return Point(x,y);
}
Point GetLineDirectionByColor(const Point p,BYTE color)
{
	Point p2=p.Up();if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	p2=p.Left();if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	p2=p.Right();if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	p2=p.Down();if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	p2.x=p.x+1;p2.y=p.y+1;if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	p2.x=p.x-1;p2.y=p.y-1;if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	p2.x=p.x+1;p2.y=p.y-1;if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	p2.x=p.x-1;p2.y=p.y+1;if(IsValid(p2) && color==Map[p2.y][p2.x])return GetLineDirection(p,p2);
	return Point(0,0);
}

bool HasOtherAvailable(const Point src, BYTE c, const Point dir)
{
	printf("src %d,%d color %d dir %d,%d ",src.x,src.y,c,dir.x,dir.y);
	if(src.x <0 || src.x>=Width || src.y<0 || src.y>=Height) return false;
	
	BYTE m[9][9] ;
	for(int y=0;y<9;++y)for(int x=0;x<9;++x)m[y][x]=Map[y][x];
	std::stack<Point> s;
	
	m[src.y][src.x]=9;
	if(dir.x==0 && dir.y==0){
		s.push(src.Up()); 
		s.push(src.Down()); s.push(src.Left()); s.push(src.Right());
	} else if(dir.y==0){
		s.push(src.Up()); s.push(src.Down());
		Point p=src.Left(); if(IsValid(p))m[p.y][p.x]=9;
		p=src.Right(); if(IsValid(p))m[p.y][p.x]=9;
	} else if(dir.x==0){
		s.push(src.Left()); s.push(src.Right());
		Point p=src.Up(); if(IsValid(p))m[p.y][p.x]=9;
		p=src.Down(); if(IsValid(p))m[p.y][p.x]=9;
	} else {
		Point p; //p.x=src.x+dir.x;p.y=src.y-dir.y;	s.push(p);
		//p.x=src.x-dir.x;p.y=src.y+dir.y;	s.push(p);
		s.push(src.Up());s.push(src.Down()); s.push(src.Left()); s.push(src.Right());
		p.x=src.x+dir.x;p.y=src.y+dir.y; if(IsValid(p))m[p.y][p.x]=9;
		p.x=src.x-dir.x;p.y=src.y-dir.y; if(IsValid(p))m[p.y][p.x]=9;
	}	

	while(!s.empty())
	{
	const Point pt=s.top();s.pop();
	//printf("pt %d,%d\n",pt.x,pt.y);
	if(!IsValid(pt)) continue;
	const byte grey = m[pt.y][pt.x];
	if((c == grey  || 1 == grey) && (!IsInLine(src,pt)||dir!=GetLineDirection(src,pt))){
		printf("true\n");return true;
	}
   if(0!=grey)continue;
	
	m[pt.y][pt.x] = 9;

	s.push(pt.Up());
	s.push(pt.Left());
	s.push(pt.Down());
	s.push(pt.Right());
	}
	printf("false\n");
	return false;
}



bool IsNotEmpty(int x,int y) {return !IsValid(x,y) || Map[y][x]!=0;}

//
int EvaluatePosition(const Point pt, FillPoints* fp)
{
	int max=-1; int sum=0; 
	int emptysize=-1;
	list<Point> dir; dir.push_back(Point(1,0));
	dir.push_back(Point(0,1));dir.push_back(Point(1,1));dir.push_back(Point(1,-1));
	for(list<Point>::iterator idir=dir.begin();idir!=dir.end();++idir)
		for(int i=-4;i<=0;i++)if(IsValid(pt.Move(*idir,i) )&& IsValid(pt.Move(*idir,i+4)) )
		{  //printf("i %d, begin\n",i);
			BYTE c[5];
			for(int j=0;j<5;j++){ 
				Point tmp= pt.Move(*idir,i+j);c[j]=Map[tmp.y][tmp.x];
			}
			FillPoints candidate;
			int x=Evaluate5(c,&candidate);
			if(max<x || (max==x && candidate.Blocks.size()>emptysize)){
				max=x; emptysize=candidate.Blocks.size();
				if(NULL!=fp){
					fp->Candidates.clear(); fp->Blocks.clear(); 
					fp->Color=candidate.Color;fp->Score=candidate.Score;
					for(list<Point>::iterator iter=candidate.Candidates.begin();
						iter!=candidate.Candidates.end();
						iter++){
						fp->Candidates.push_back(pt.Move(*idir,i+(*iter).y));
						//printf("dir %d,%d \n",(*idir).x,(*idir).y);
					}
					for(list<Point>::iterator iter=candidate.Blocks.begin();
						iter!=candidate.Blocks.end();
						iter++){
						fp->Blocks.push_back(pt.Move(*idir,i+(*iter).y));
					}
				} // end of if NULL!=p
			}
			sum+=x;
		}
/*	
	for(int i=-4;i<=0;i++)if(IsValid(pt.x+i,pt.y+i) && IsValid(pt.x+i+4,pt.y+i+4))
		{
			BYTE c[5];for(int j=0;j<5;j++)c[j]=Map[pt.y+i+j][pt.x+i+j];
			FillPoints candidate;
			int x=Evaluate5(c,&candidate);
			if(max<x|| (max==x && candidate.Blocks.size()>emptysize)){
				max=x; emptysize=candidate.Blocks.size();
				if(NULL!=fp){
					fp->Candidates.clear(); fp->Blocks.clear(); int emptysize=-1;
					fp->Color=candidate.Color;fp->Score=candidate.Score;
					for(list<Point>::iterator iter=candidate.Candidates.begin();
						iter!=candidate.Candidates.end();
						iter++){
						fp->Candidates.push_back(Point(pt.x+i+(*iter).y,pt.y+i+(*iter).y));
					}
					for(list<Point>::iterator iter=candidate.Blocks.begin();
						iter!=candidate.Blocks.end();
						iter++){
						fp->Blocks.push_back(Point(pt.x+i+(*iter).y,pt.y+i+(*iter).y));
					}
				} // end of if NULL!=p
			}
			sum+=x;
		}
	for(int i=-4;i<=0;i++)if(IsValid(pt.x+i,pt.y-i) && IsValid(pt.x+i+4,pt.y-i-4))
		{
			BYTE c[5];for(int j=0;j<5;j++)c[j]=Map[pt.y-i-j][pt.x+i+j];
			FillPoints candidate;
			int x=Evaluate5(c,&candidate);
			if(max<x|| (max==x && candidate.Blocks.size()>emptysize)){
				max=x; emptysize=candidate.Blocks.size();
				if(NULL!=fp){
					fp->Candidates.clear(); fp->Blocks.clear();
					fp->Color=candidate.Color;fp->Score=candidate.Score;
					for(list<Point>::iterator iter=candidate.Candidates.begin();
						iter!=candidate.Candidates.end();
						iter++){
						fp->Candidates.push_back(Point(pt.x+i+(*iter).y,pt.y-i-(*iter).y));
					}
					for(list<Point>::iterator iter=candidate.Blocks.begin();
						iter!=candidate.Blocks.end();
						iter++){
						fp->Blocks.push_back(Point(pt.x+i+(*iter).y,pt.y-i-(*iter).y));
					}
				} // end of if NULL!=p
			}
			sum+=x;
		}
	for(int i=-4;i<=0;i++)if(IsValid(pt.x,pt.y+i) && IsValid(pt.x,pt.y+i+4))
		{
			BYTE c[5];for(int j=0;j<5;j++)c[j]=Map[pt.y+i+j][pt.x];
			FillPoints candidate;
			int x=Evaluate5(c,&candidate);
			if(max<x|| (max==x && candidate.Blocks.size()>emptysize)){
				max=x; emptysize=candidate.Blocks.size();
				if(NULL!=fp){
					fp->Candidates.clear(); fp->Blocks.clear();
					fp->Color=candidate.Color;fp->Score=candidate.Score;
					for(list<Point>::iterator iter=candidate.Candidates.begin();
						iter!=candidate.Candidates.end();
						iter++){
						fp->Candidates.push_back(Point(pt.x,pt.y+i+(*iter).y));
					}
					for(list<Point>::iterator iter=candidate.Blocks.begin();
						iter!=candidate.Blocks.end();
						iter++){
						fp->Blocks.push_back(Point(pt.x,pt.y+i+(*iter).y));
					}
				} // end of if NULL!=p
			}
			sum+=x;
		}
*/
	return max;
}

// Function prototypes. 
// given 5 sequencial positons each of which with a color, ex 01226
// evaluate its value, 
// if it is 0, means could be any other color, 
// fixme only 4 is acurate
// return value and correponding color
int Evaluate5(const BYTE* const seq, FillPoints* fp)
{
	//for(int i=0;i<5;i++)printf("%d ",*(seq+i));
	//printf("evaluate5 \n");
	// get the most freqent color other than 0(void) and 1(wildcard)
	BYTE c[10]; for(int i=0;i<9;++i)c[i]=0;
	for(int i=0;i<5;++i)if((*(seq+i))<9)c[*(seq+i)]++;
	int max=0; int color=0;
	for(int i=2;i<9;++i)
	{
		if(c[i]>max){max=c[i];color=i;}
	}
	if(0==max){
		if(NULL!=fp){fp->Color=2;fp->Score=0;fp->Candidates.clear();fp->Blocks.clear();}
		return 0;
	}// don't consider all wildcard case, pick any color 2-8
	

	for(int i=0;i<5;++i)
		if(1==*(seq+i))max++;
		else if(1!=*(seq+i) && 0!=*(seq+i) && *(seq+i)!=color ) {
			if(NULL!=fp) fp->Blocks.push_back(Point(0,i));
			max--;
		} else if(0==*(seq+i)){
			if( NULL!=fp) 	fp->Candidates.push_back(Point(0,i));
		}

	if(NULL!=fp){ fp->Color = color;fp->Score=max;}

	return max;
} 
void RealMove(const Point& src,const Point& dst)
{
//	PONIT p(src.x,src.y);
	int len=42; 
	int delta=21;
	int x,y;
			RECT r;
			GetWindowRect(GetForegroundWindow(),&r);

	x=r.left+delta+len*src.x; y=r.top+73+delta+len*src.y;
	mouse_event(MOUSEEVENTF_ABSOLUTE|MOUSEEVENTF_MOVE,0,0,0,0);	
	mouse_event(MOUSEEVENTF_MOVE|MOUSEEVENTF_LEFTDOWN,x,y,0,0);	
	mouse_event(MOUSEEVENTF_LEFTUP,x,y,0,0);

	x=r.left+delta+len*dst.x; y=r.top+73+delta+len*dst.y;
	mouse_event(MOUSEEVENTF_ABSOLUTE|MOUSEEVENTF_MOVE,0,0,0,0);	
	mouse_event(MOUSEEVENTF_MOVE|MOUSEEVENTF_LEFTDOWN,x,y,0,0);
	mouse_event(MOUSEEVENTF_LEFTUP,x,y,0,0);
		
	printf("move from (%d,%d) to (%d,%d)\n",src.x,src.y,dst.x,dst.y);
	
}
bool IsMovable(const Point& src,const Point& dst)
{
	if(src.x <0 || src.x>=Width || src.y<0 || src.y>=Height ||
		dst.x<0  || dst.x>=Width || dst.y<0 || dst.y>=Height) return false;
	if(0!=Map[dst.y][dst.x] || 0==Map[src.y][src.x])return false;

	BYTE m[9][9] ;
	for(int y=0;y<9;++y)for(int x=0;x<9;++x)m[y][x]=Map[y][x];
	std::stack<Point> s;
	s.push(src);
	m[src.y][src.x]=0;

	while(!s.empty())
	{
	const Point pt=s.top();s.pop();
	//printf("pt %d,%d\n",pt.x,pt.y);
	if(pt.x<0 || pt.x>=Width || pt.y<0 || pt.y>=Height) continue;
	const byte grey = m[pt.y][pt.x];
	if(0 != grey )continue;
   if(pt.x==dst.x && pt.y==dst.y)return true;
	
	m[pt.y][pt.x] = 1;

	s.push(pt.Up());
	s.push(pt.Left());
	s.push(pt.Down());
	s.push(pt.Right());
	}
	return false;
}

bool FindBall(BYTE color, Point& p)
{
	for(int y=0;y<9;++y)for(int x=0;x<9;++x)
		if(color==Map[y][x]){p.x=x;p.y=y;return true;}
	return false;
}
bool MatchPattern()
{
   	 	HWND shwnd= GetDesktopWindow();
			shwnd = GetForegroundWindow();
    		HDC sdc= GetWindowDC( shwnd);
			list<word32>& m=Pattern;
 
			int len = 42;
			for(int y=0;y<9;++y)
			{	for(int x=0;x<9;++x)
				{
					COLORREF c=GetPixel(sdc,x*len+25+3,y*len+73+15);
							
					list<word32>::iterator iter=m.begin(); int i=0;
					for(;iter!=m.end() && *iter != (word32)c;iter++,i++);
						
					//printf("%1x ",i);
					//printf("%6x ",(word32)c);
					if(i>=9) return false;
					Map[y][x]=i;//distance(iter,m.begin());
				}
				//printf("\n");	
			}

	return true;
}
 
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
      	PostQuitMessage(0);
       	break;
      }
	case WM_TIMER:
		{
			if(!AIRunning)break;
			printf("----------------------------\n");
			if(!MatchPattern())break;
			
			// find the best place to move
			int max=-10000; Point selectedsrc,selecteddst;

			for(BYTE y=0;y<9;y++){
				for(int x=0;x<9;++x){
					FillPoints c;int olddst=EvaluatePosition(Point(x,y),&c);
					//printf("%d,%d ",olddst,c.Color);
					if(0!=Map[y][x])continue;
					
					int min=10000; int moveMax=-1000;
					Point dst=Point(x,y);
					for(BYTE j=0;j<9;j++)for(BYTE i=0;i<9;++i)
					{	
						if(i==x && j==y) continue;
						Point src=Point(i,j);
						if(!IsMovable(src,dst))continue;
						FillPoints srcTmp,dstTmp;
						int oldsrc=EvaluatePosition(src,NULL);

						if(c.Color==Map[j][i] || 1==Map[j][i] ) {
							
							Map[y][x]=Map[j][i];Map[j][i]=0;

							int newdst=EvaluatePosition(dst,&dstTmp);
							int newsrc=EvaluatePosition(src,&srcTmp);
							if(olddst==3 && newdst==4 && !HasOtherAvailable(
								(dstTmp.Candidates.front()),dstTmp.Color,GetLineDirection(dstTmp.Candidates.front(),dst))
								) {
							} else if(oldsrc<=3 && newsrc==4 && !HasOtherAvailable(
								src,srcTmp.Color,GetLineDirectionByColor(src,srcTmp.Color))
								) {
							} else if(newdst<=4 && oldsrc==4) {
							} else if(newdst> 4 ){
								max=newdst;min=oldsrc;moveMax=newdst;selectedsrc=src;selecteddst=dst;
							} else if(newdst>max && newdst>oldsrc  ){
								max=newdst;min=oldsrc;moveMax=newdst;selectedsrc=src;selecteddst=dst;
							} else if(newsrc>max){
								max=newsrc;min=oldsrc;moveMax=newdst;selectedsrc=src;selecteddst=dst;
							} else if(newdst==max && newsrc>moveMax && newsrc>=oldsrc) {
								max=newdst;min=oldsrc;moveMax=newdst;selectedsrc=src;selecteddst=dst;
							} else if(newsrc==max && newdst>moveMax) {
								max=newsrc;moveMax=newdst;selectedsrc=src;selecteddst=dst;
							} 

							Map[j][i]=Map[y][x];Map[y][x]=0;
						}// end of if c==map j,i
					}// end of for j,i

				}// end of for x
				//printf("\n");
			}// end of for y

			if(max>0){RealMove(selectedsrc,selecteddst);}
			printf("max = %d\n",max);
			break;
		}
	case WM_LBUTTONDOWN:
     	{
			AIRunning=false;
			//SetCapture(hwnd);
			break;
		}
	case WM_LBUTTONUP:
     	{
			//ReleaseCapture();
			break;
		}
	case WM_MOUSEMOVE:
		{
			//HitPoint.x = (LOWORD(lParam)-10)<<1;
			//HitPoint.y = (HIWORD(lParam)-10)<<1;
			int x = (LOWORD(lParam));
			if (x>=0x8000)x=(x-0xffff-1) ; 
			int y = (HIWORD(lParam));
			if (y>=0x8000)y=y-0xffff - 1;
			RECT rect;
			GetWindowRect(hwnd,&rect);
			x+=(rect.left);
			y+=(rect.top+10); // 
         
			HitPoint.x=x;HitPoint.y=y;
			InvalidateRect(hwnd,NULL,TRUE);

			break;
		}
 	case WM_PAINT:
		{
			PAINTSTRUCT ps;
			HDC hdc = BeginPaint(hwnd, &ps);
	
   	 	HWND shwnd= GetDesktopWindow();
			shwnd = GetForegroundWindow();
    		HDC sdc= GetWindowDC( shwnd);
			HDC memDC =  CreateCompatibleDC(sdc);
 
      	BitBlt(hdc,0,0,42*9,42*9,sdc,0,73,SRCCOPY);

	   	//BitBlt(hdc,0,0,100,100,memDC,0,0,SRCCOPY);




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
        400,       // default horizontal position 
        300,       // default vertical position 
        400,       // default width 
        400,       // default height 
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
	 SetTimer(hwnd,0,1500,0);	
			list<word32>& m=Pattern;
			word32 p[9]={0xc0c0c0,0,0xffffff,
				0xff,0xff00,0xff0000,0xffff,0xff00ff,0xffff00};
			for(int i=0;i<9;i++)m.push_back(p[i]);		
    return TRUE; 
 
} 
