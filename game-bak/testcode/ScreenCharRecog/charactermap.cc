#include "charactermap.h"
#include <stack>

void CharacterMap::MeasureLines()
{
	// scan from left to right, count number of lines
	bool previsline=false;
	for(word16 x=0;x<Width;++x)
	{
	  	if(!IsVLine(x,TARGET))
		{  
			previsline = false;	
		}
		else
		{
			if(!previsline)
			{
				VLines.push_back(x);
				previsline=true;
			}
		}
	}

	previsline=false;
	for(word16 y=0;y<Height;++y)
	{
	  	if(!IsHLine(y,TARGET))
		{  
			previsline = false;	
		}
		else
		{
			if(!previsline)
			{
				HLines.push_back(y);
				previsline=true;
			}
		}
	}
}

// use color to mark identified circles 
bool CharacterMap::MeasureCircleShape(const Point p, byte color)
{
	Point start;
	while(FindPoint(start,Background))
	{
		std::stack<Point> s;
		s.push(start);
		word16 left,right,top,bottom;
		left=right=start.x;top=bottom=start.y;

		while(!s.empty())
		{
			const Point pt=s.top();s.pop();
			if(pt.x<0 || pt.x>=Width || pt.y<0 || pt.y>=Height) continue;
			const byte grey = Map[pt.y][pt.x];
			if(color == grey || TARGET == grey )continue;	

			word16 x=pt.x;
			word16 y=pt.y;
			if(x<left)left=x;
			if(x>right)right=x;
			if(y<top)top=y;
			if(y>bottom)bottom=y;
			Map[pt.y][pt.x] = color;

			s.push(pt.Up());
			s.push(pt.Left());
			s.push(pt.Down());
			s.push(pt.Right());
		}	
		Circles.push_back(Rect(left,top,right,bottom));
	}
	Result = Circles.size();
	return false;
}
// if the given line is fill with the given color
bool CharacterMap::IsVLine(int x, byte color)
{
	if(x<0 || x>=Width) return false;
	int sum = 0;
	for(word16 y=0;y<Height;++y)
	{
		if(color==Map[y][x])
		{
			sum++;
		}
	}	
	if((sum+1)>=Height)return true;		
	return false;
}

// if the given line is fill with the given color
bool CharacterMap::IsHLine(int y, byte color)
{
	if(y<0 || y>=Height) return false;
	int sum = 0;
	for(word16 x=0;x<Width;++x)
	{
		if(color==Map[y][x])
		{
			sum++;
		}
	}	
	if((sum)>=Width)return true;		
	return false;
}

// p [out] contains the point found
bool CharacterMap::FindPoint(Point & p, byte color)
{
	for(word16 x=0;x<Width;++x)for(word16 y=0;y<Height;++y)
		if(color== Map[y][x]) {p.x=x;p.y=y;return true;}
	return false;
}

void CharacterMap::FloodFill(const Point& p,byte color)
{
	std::stack<Point> s;
	s.push(p);

	while(!s.empty())
	{
	const Point pt=s.top();s.pop();
	if(pt.x<0 || pt.x>=Width || pt.y<0 || pt.y>=Height) continue;
	const byte grey = Map[pt.y][pt.x];
	if(color == grey || TARGET == grey )continue;

	
	Map[pt.y][pt.x] = color;

	s.push(pt.Up());
	s.push(pt.Left());
	s.push(pt.Down());
	s.push(pt.Right());
	}
}

void CharacterMap::FlushNonCenteredLines()
{
	// find a line pattern, 
	// sum of piexel values in line percpective
	// 4 < threshhold
	// 114 <-- these are lines contain characters.
	// 113 <--|	
	// 3 < threshhold
	int threshhold = 1;	
	word16 y=0;

	// dot start until meet the first empty line
	int sum=0; for(word16 x=0;x<MAX_WIDTH;++x)sum+=Map[y][x];
	while (y<MAX_HEIGHT && sum >=threshhold)
	{
		for(word16 x=0;x<MAX_WIDTH;++x)Map[y][x]=Background; // flush this line
      y++;
 		sum=0; for(word16 x=0;x<MAX_WIDTH;++x)sum+=Map[y][x];
	}	

	// walk until find the line pattern	
	while (y<MAX_HEIGHT && sum<threshhold)
	{
      y++;
 		sum=0; for(word16 x=0;x<MAX_WIDTH;++x)sum+=Map[y][x];
	}	

	// walk until the empty line
	while (y<MAX_HEIGHT && sum >=threshhold)
	{
      y++;
 		sum=0; for(word16 x=0;x<MAX_WIDTH;++x)sum+=Map[y][x];
	}	

	// flush the rest lines
	while (y<MAX_HEIGHT)
	{
		for(word16 x=0;x<MAX_WIDTH;++x)Map[y][x]=Background; // flush this line
      y++;
	}	

}


void CharacterMap::FlushBackground()
{
	for(word16 x=0;x<Width;++x)for(word16 y=0;y<Height;++y)
		if(TARGET!=Map[y][x])Map[y][x]=Background;
}

// assume input is a black-white map (either 0 or 0xff) for each pixel
// the x-y coordinator layout is c-style, i.e map[y][x]
// first WIDHT*HEIGHT is our point of interest
CharacterMap::CharacterMap(
	const byte* src, word16 width, word16 height) :
	Width(0),Height(0),VSeperator(VSEPERATOR),HSeperator(HSEPERATOR),
	Background(BACKGROUND),Result(0)
{
	if((NULL==src) || (width<MAX_WIDTH) 
		|| (height<MAX_HEIGHT) ) return;
   
	Circles.empty();
	// find background color
	int background = 0;
 	for(word16 x=0;x<width;++x)
 	{
		for(word16 y=0;y<height;++y)
		{
			background+=*(src+y*width+x);
		}	
	}
 	background = background/(width*height) > 0x7f?0xff:0;

	// copy interest area
	for(word16 x=0;x<MAX_WIDTH;++x)for(word16 y=0;y<MAX_HEIGHT;++y)
		{
			Map[y][x]= *(src+y*width+x)==background?Background:TARGET;
		}
	

	FlushNonCenteredLines();

	// vertically seperate character
	for(word16 x=0;x<MAX_WIDTH;++x)
	{
		bool b=true; //if this line is all background
		for(word16 y=0;y<MAX_HEIGHT;++y)
		{
			if(Background!=Map[y][x])
			{
				b=false;
				break;
			}
		}
		// mark vertical seperator	
		if(b)for(word16 y=0;y<MAX_HEIGHT;++y)Map[y][x]=VSeperator;
	}

	// horizontally seperate character
	for(int y=0;y<MAX_HEIGHT;++y)
 	{			
  		bool bStart=false; // whether leave v-seperator 
		word16 xStart=0;
		word16 xEnd=0;

	 	for(word16 x=1;x<MAX_WIDTH;++x)
		{	
			if( !bStart && Background == Map[y][x])
			{
				bStart=true;
				xStart=x;
			}
			else if(VSeperator==Map[y][x] && bStart)
			{
				bStart=false;
				xEnd = x;
				// mark horizontal seperator	
				for(word16 i=xStart;i<xEnd;++i)Map[y][i]=HSeperator;					
			}
			else if(VSeperator!=Map[y][x] && bStart && Background != Map[y][x])
			{	
			  bStart=false;
			}
		}//end of for x
	}// end of for y

	
	// find a character between 2 VSpererators and 2 HSeperators

	word16 x,y,i,j;
	x=y=i=j=0;

	// do not start unit meet VSep
	byte grey=Map[y][x];
	while	(x<MAX_WIDTH && grey!=VSeperator)
	{			
		x++;
		grey = Map[y][x];					
	}

	// go across continuous VSep
	while	(x<MAX_WIDTH && grey==VSeperator)
	{			
		x++;
		grey = Map[y][x];					
	}
	i=x; // i is character start x pos

	while	( y<MAX_HEIGHT && (grey==HSeperator))
	{			
		y++;
		grey = Map[y][x];
	}
	j=y; // j is charecter start y pos

	while	(x<MAX_WIDTH && grey!=VSeperator)
	{			
		x++;
		grey = Map[y][x];	
	}
	Width=x-i;

	while	(y<MAX_HEIGHT && grey!=HSeperator && i<MAX_WIDTH)
	{			
		y++;
		grey = Map[y][i];					
	}
	Height=y-j;

	// mov found character
	for(word16 x=0;x<Width;++x)for(word16 y=0;y<Height;++y)
		Map[y][x]=Map[j+y][i+x];


	FlushBackground();


	byte fillcolor=200;
	for(word16 x=0;x<Width;++x)FloodFill(Point(x,0),fillcolor);
   for(word16 x=0;x<Width;++x)FloodFill(Point(x,Height-1),fillcolor);
	for(int y=1;y<(Height-1);++y)FloodFill(Point(0,y),fillcolor);
	for(int y=1;y<(Height-1);++y)FloodFill(Point(Width-1,y),fillcolor);

	Point p;
   MeasureCircleShape(p,100);
	MeasureLines();
}

void CharacterMap::ToString(char* str)
{
   int sum =0;
	//for(int x=0;x<Width;++x)for(int y=0;y<Height;++y)	
	//	if(TARGET==Map[y][x])sum++;

	int x1,x2,x3,x4,y1,y2;
	x1=x2=x3=x4=y1=y2=0;

	int remainder = Height & 0x1;
	if( 1 == remainder)y1=y2=(Height>>1); // Height = 3, 0-1, 1-2
	else {y2=(Height>>1);y1=y2-1;  } // Height=4, 0-1, 2-3

	remainder = Width % 3;
	//(Width-1-x4)==(x3-x2)=(x1-0)
	if(0==remainder){x1=(Width/3)-1;x2=x1+1;x3=x2+x1;x4=x3+1;}  // Width=3 0-0,1-1,2-2
	else if(1==remainder){x1=(Width/3);x2=x1;x3=x2+x1;x4=x3;}//Width=4 0-1,1-2,2-3
	else {x1=(Width/3)+1;x2=x1-1;x3=x2+x1;x4=x3-1;} //Width=5 0-1,1-2,3-3

	

	int upleft=0;
	for(int x=0;x<=x1;++x)for(int y=0;y<=y1;++y)	
		if(TARGET==Map[y][x])upleft++;
	
	int upmiddle=0;
	for(int x=x2;x<=x3;++x)for(int y=0;y<=y1;++y)	
		if(TARGET==Map[y][x])upmiddle++;

	int upright=0;
	for(int x=x4;x<Width;++x)for(int y=0;y<=y1;++y)
		if(TARGET==Map[y][x])upright++;

	int downleft=0;
	for(int x=0;x<=x1;++x)for(int y=Height/2;y<Height;++y)	
		if(TARGET==Map[y][x])downleft++;

	int downmiddle=0;
	for(int x=x2;x<=x3;++x)for(int y=Height/2;y<Height;++y)	
		if(TARGET==Map[y][x])downmiddle++;

	int downright=0;
	for(int x=x4;x<Width;++x)for(int y=Height/2;y<Height;++y)	
		if(TARGET==Map[y][x])downright++;

	Spwt.Weight[0][0]=upleft;
	Spwt.Weight[0][1]=upmiddle;
	Spwt.Weight[0][2]=upright;
	Spwt.Weight[1][0]=downleft;
	Spwt.Weight[1][1]=downmiddle;
	Spwt.Weight[1][2]=downright;

	Spwt.Normalize();

	int numOfCircles = Circles.size();
	int numOfVLines = VLines.size();
	int numOfHLines = HLines.size();


	char result='-';

	if(0==numOfCircles)
	{
		if(0==numOfVLines)
		{
			if(0==numOfHLines)
			{
				if(Spwt.IsSShape())result='s';// do not diff. Ss
				else if(Spwt.IsVShape())result='V';// fixme VvWwY
				else if(Spwt.IsCShape())result='C';// do not diff. Cc
				else if(Spwt.IsGShape())result='G';
				else if(Spwt.IsFShape())
				{ //yr
					if(Height>=(2*Width))result='y';
					else result='r';
				}
				else
				{	// fixme Xxzt
					result='x';
				}
			}
			else if(1==numOfHLines);
			else if(2==numOfHLines && Spwt.IsZShape())result='Z';
			// fix me z is similar to x in current characteristic
		}
		else if(1==numOfVLines)
		{
			// fixme, Iijl
			if(0==numOfHLines)
			{
				if(Spwt.IsJShape())result='J';
				else if(Spwt.IsLShape())result='h'; // fixme or k
				else if(Spwt.IsCShape())result='K';
			}
			else if(1==numOfHLines)
			{
				if(Spwt.IsTShape())result='T';
				else if(Spwt.IsLShape())result='L';
				else if(Spwt.IsFShape())result='F';
				else if(Height >= (2*Width))result='f';				
			}
			else if(2==numOfHLines)result='E';
		}
		else if(2==numOfVLines)
		{
			if(0==numOfHLines)
			{ // UuMNn
				if(Spwt.IsUShape() && !Spwt.IsMShape())result='U'; // do not diff. U u	
				else if(Spwt.IsMShape() && Spwt.IsUShape())result='N';
				else if(Spwt.IsMShape() && Spwt.IsCShape())result='n';
				else if(Spwt.IsMShape() && !Spwt.IsCShape())result='M';
			}
			else if(1==numOfHLines) result='H';
		}
		else if(3==numOfVLines) result='m';
	}
	else if(1==numOfCircles)
	{
		if(0==numOfVLines)
		{
			if(Spwt.IsAShape())result='A';
			else if(Spwt.IsGShape())result='Q';
			else if(Spwt.Is7Shape())result='g';
			else // only e a o is left
			{
				Rect r=Circles.front();
				if(r.GetWidth() > r.GetHeight())result='e';
				else if(Spwt.IsMinEqMax())result='o';
				else result ='a';
			}
		} 
		else if(1==numOfVLines)
		{ //DPRbdpq
			if(Spwt.IsLShape())result='b';
			else if(Spwt.IsJShape())result='d';
			else if(Spwt.Is7Shape())result='q';	
			else if(Spwt.IsFShape())result='p'; // don't diff P and p
			else //only D and R is left
			{
				Rect r=Circles.front();
				if(r.GetHeight()>(Height/2))result='D';				
				else result='R';
			}
		}
	}	
	else if(2==numOfCircles)
	{
		result='B';	
	}


	sprintf(str,"C%dVHL%d%dSW(%d,%d,%d)(%d,%d,%d),Z%dS%dV%dC%dF%dG%dT%dL%dJ%dU%dM%dA%d7%d %c",
		numOfCircles,numOfVLines,numOfHLines,
		Spwt.Weight[0][0],Spwt.Weight[0][1],Spwt.Weight[0][2],
		Spwt.Weight[1][0],Spwt.Weight[1][1],Spwt.Weight[1][2],
		Spwt.IsZShape(),Spwt.IsSShape(),Spwt.IsVShape(),Spwt.IsCShape(),Spwt.IsFShape(),Spwt.IsGShape(),
		Spwt.IsTShape(),Spwt.IsLShape(),Spwt.IsJShape(),Spwt.IsUShape(),Spwt.IsMShape(),Spwt.IsAShape(),Spwt.Is7Shape(),
  	   result	
		);
}
