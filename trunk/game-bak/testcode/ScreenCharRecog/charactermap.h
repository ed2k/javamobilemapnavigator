#ifndef __CHARACTER_MAP__
#define __CHARACTER_MAP__


#include "stddef.h" // #define NULL 0
//bool true false define in c++

#include <list>


typedef unsigned char byte;
typedef unsigned int word16;

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
};

class Rect
{
public:
	int lx,rx,ty,by;//left right top bottom
	Rect():lx(0),rx(0),ty(0),by(0){};
	Rect(int xl,int yt,int xr, int yb):lx(xl),rx(xr),ty(yt),by(yb){};
	int GetWidth(){return (rx-lx);}
	int GetHeight(){return (by-ty);}
};

class SpaceWeight
{
public:
	enum {
		UP=0,
		DOWN=1,
		WIDTH=3,
		HEIGHT=2,
		LEFT=0,
		MIDDLE=1,
		RIGHT=2
	};
	int Weight[2][3];
	void Normalize(){
		int sum=0;for(int x=0;x<WIDTH;++x)for(int y=0;y<HEIGHT;++y)sum+=Weight[y][x];
		if(sum==0)sum=1;
		for(int x=0;x<WIDTH;++x)for(int y=0;y<HEIGHT;++y)
			Weight[y][x]=Weight[y][x]*100/sum;
	};	
	bool IsMinEqMax()
	{
		std::list<int> a;
		for(int x=0;x<WIDTH;++x)for(int y=0;y<HEIGHT;++y)a.push_back(Weight[y][x]);
		a.sort();
		return (a.back() == a.front());
	}		
	bool IsZShape()
	{
		int map[6]={-1,-1,+1,
						+1,-1,-1};		
		return MatchShape(map);
	};
	bool IsSShape(){
		int map[6]={+1,-1,-1,
						-1,-1,+1};		
		return MatchShape(map);
	};
	bool IsVShape(){
		int map[6]={+1,-1,+1,
						-1,+1,-1};		
		return MatchShape(map);
	};
	bool IsCShape(){
		int map[6]={+1,-1,-1,
						+1,-1,-1};		
		return MatchShape(map);
	};
	bool IsFShape(){
		int map[6]={+1,+1,+1,
						+1,+1,-1};		
		return MatchShape(map);
	};
	bool IsGShape(){
		int map[6]={-1,-1,-1,
						-1,-1,+1};		
		return MatchShape(map);
	};
	bool IsTShape(){
		int map[6]={+1,+1,+1,
						-1,+1,-1};		
		return MatchShape(map);
	};
	bool IsLShape(){
		int map[6]={+1,-1,-1,
						+1,+1,+1};		
		return MatchShape(map);
	};
	bool IsJShape(){
		int map[6]={-1,+1,+1,
						+1,+1,+1};		
		return MatchShape(map);
	};
	bool IsUShape(){
		int map[6]={+1,-1,+1,
						+1,+1,+1};		
		return MatchShape(map);
	};
	bool IsMShape(){
		int map[6]={+1,+1,+1,
						+1,-1,+1};		
		return MatchShape(map);
	};
	bool IsAShape(){
		int map[6]={-1,+1,-1,
						+1,-1,+1};		
		return MatchShape(map);
	};
	bool Is7Shape(){
		int map[6]={+1,+1,+1,
						-1,+1,+1};		
		return MatchShape(map);
	};
private:
	bool MatchShape(const int* const map)
	{
		std::list<int> a;
		std::list<int> b;
		for(int i=0;i<(WIDTH*HEIGHT);++i)
		{
			int x=(i%WIDTH);int y=(i/WIDTH);
			int p=Weight[y][x];
			if(1==*(map+i))a.push_back(p);
			else b.push_back(p);
		}
		a.sort();b.sort();
		if(b.back() <= a.front())return true;
		else return false;
	};
};

//typedef vector<byte> 
  
class CharacterMap
{
public:
	// use 32 as it is easy to manipulate a word32
	enum { 
		MAX_WIDTH =32, 
		MAX_HEIGHT=32,
		VSEPERATOR=100,
		HSEPERATOR=200,
		TARGET=0xff,
		BACKGROUND=0
	};	
private:
	int Width; 
	int Height;
	byte VSeperator;
	byte HSeperator;
	byte Background;
	byte Map[MAX_HEIGHT][MAX_HEIGHT];
	std::list<Rect> Circles;
	std::list<int> VLines;
	std::list<int> HLines;
	SpaceWeight Spwt;
	word16 Result;
public:
	CharacterMap(const byte* src, word16 width, word16 height);
	~CharacterMap(){};
	const byte* GetMap(){return (byte*)Map;};
	int GetWidth(){return Width;};
	int GetHeight(){return Height;};
	word16 GetResult(){return Result;};
	void ToString(char * str);
	
private:
	// mark all non target pixel as background
	void FlushBackground();
	// mark all non centered the lines as background
	void FlushNonCenteredLines();
	//
	void FloodFill(const Point& p,byte color);
	// return a point has identified color
	bool FindPoint(Point & p, byte color);
	bool IsVLine(int x, byte color);
	bool IsHLine(int y, byte color);
	bool MeasureCircleShape(const Point p, byte color);
	void MeasureLines();
};

/*
C number of circles, U up  D down
F focus U up, Down, M middle, L left, R right

  C  ,F,      VL,HL
A 1U, 121,222  
B 2 ,        ,1
C -   321,321
D 1   211,211,1
E -   221,221,1, 2
F -   221,220,1, 1
G -   321,334,- 
H -   212,212,2 
I -  ,121,121,1
J -  ,012,112,
K -   211,211,1
L -  ,210,211,1
M -  ,222,212,2     
N -  ,       ,2
O 1
P 1U, 111,110
Q 1 , 111,122,1
R 1U         ,1
S -   221,122
T -   121,010,1, 1
U -   101,111,2
V -   212,121,
W -   212,222
X -   
Y -   111,010
Z -   112,211, ,2 
a 1d, 
b 1d, 100,222,1
c -   321,321
d 1d, 112,222,1
e 1u*, special ratio of circle 
f -   132,131,1
g 1u, 222,122
h -   100,212,1
i -   120,121,1 with a dot
j -   122,122,1 with a dot
k -   100,211,1
l -   120,121,1
m -          ,3
n -          ,2
o 1 
p 1u, 111,110,1
q 1u, 111,011,1
r -   222,110
s -   211,112
t -   
u -   101,111
v -   212,121
w -   212,222
x -   212,212
y -   212,221
z -   122,221
0
1
2
3
4
5
6
7
8
9





*/

#endif //__CHARACTER_MAP__
