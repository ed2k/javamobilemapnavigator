import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*; 
import java.text.DateFormat;


/*
3 after action statuses for each grid {covered, opened, marked}
4 grid statuses for each grid {opened: # of mines 0-8, open error, mine, marked}
2 types of action
 dig(open) : left click
 mark(unmark: flip flop) : right click

 */

public class j5orMore extends Frame {

    public void paint(Graphics g) {
    }

    //StampCanvas StampCanvas = new StampCanvas();
    MineCanvas mineCanvas;
    Panel palette = new Panel(new FlowLayout());

    j5orMore() {
        //super("MouseMotionAdapter Example");
        // Initialize palette with stamps.
	try{
	    setLayout(new BorderLayout());

	    palette.add(new LED(5,getToolkit().getImage("led.gif")),BorderLayout.WEST);

	    palette.add( new FaceButton(getToolkit().getImage("mFace.gif")),BorderLayout.CENTER);
	    LED score=new  LED(5,getToolkit().getImage("led.gif"));
	    mineCanvas=new MineCanvas(score);
	    palette.add(score,BorderLayout.EAST);	 
	   
	}catch(Exception e){}



        // Layout components
        add(palette, BorderLayout.NORTH);
        add(mineCanvas, BorderLayout.CENTER);
        setSize(400, 500);

        show();

    }



    class MouseEventHandler extends MouseAdapter {
        public void mousePressed(MouseEvent evt) {

            System.out.print("stamp "+evt.getX()+","+evt.getY());//for test
	    System.out.println();//for click test
	    //StampCanvas.setStamp(stamp.image);
	    //StampCanvas.setClickPoint(new Point(evt.getX(),evt.getY()));
        }
    }

    public static void main(String args[]) {
        new j5orMore();
   }
}//end of class stamps

class FaceButton extends Component{
    int width, height;
    int status = 0;
    Image image;
  
    FaceButton(Image image) {

        MediaTracker tracker = new MediaTracker(this);

        this.image = image;
        try {
            tracker.addImage(image, 0);
            tracker.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        width = image.getWidth(null) /5;
        height = image.getHeight(null);
	addMouseListener(new MouseEventHandler());
	addMouseMotionListener(new MouseMotionEventHandler());
    }



    public void paint(Graphics g) {
	int sx=status*width;
        g.drawImage(image,0,0,width,height,sx,0,sx+width,height , this);
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    class MouseEventHandler extends MouseAdapter {
        public void mousePressed(MouseEvent evt) {
            //ImageButton stamp = (ImageButton)evt.getSource();
            //System.out.print("button "+evt.getX()+","+evt.getY());//for test	    
	    status=1; //select the 3rd picture
	    repaint();
        }
        public void mouseReleased(MouseEvent evt) {
            //ImageButton stamp = (ImageButton)evt.getSource();
            //System.out.print("button "+evt.getX()+","+evt.getY());//for test	    
	    status=0; //back to normal
	    repaint();
        }
    }
    class MouseMotionEventHandler extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent evt) {
	    int x = evt.getX();
	    int y = evt.getY();
	    if(x > width/2){
		status=3;
	    }else status=0;
	    repaint();            
        }
    }

}//end of kingbutton
class MineCanvas extends Canvas{
    LED score;

    int width, height;
    int status = 0;

    Image imgFace,imgLED,imgButton;
    
    int xnum=10, ynum=10;
    int xGrid=16, yGrid=16;
    Mine mm;
    // Double buffer variables
    Image bbuf;
    Graphics bbufG;
    MineCanvas(LED led) {
	score = led;
	xnum=ynum=9;
	xGrid=yGrid=42;
	mm =new Mine(xnum,ynum);
        MediaTracker tracker = new MediaTracker(this);

        imgFace = getToolkit().getImage("mFace.gif");
	imgLED = getToolkit().getImage("led.gif");
	imgButton =getToolkit().getImage("5orMoreBalls.gif");
        try {
            tracker.addImage(imgFace, 0);
            tracker.addImage(imgLED, 1);
            tracker.addImage(imgButton, 2);
            tracker.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        width = 300;
        height = 300;//image.getHeight(null);
	addMouseListener(new MouseEventHandler());
	//addMouseMotionListener(new MouseMotionEventHandler());
    }

    public void paint(Graphics g) {
        update(g);
    }

    public void update(Graphics g) {
	score.setCounter(mm.getScore());
        int w = getSize().width;
        int h = getSize().height;

        // Create the double buffer if necessary.
        // Also make it bigger if necessary.
        if (bbuf == null 
              || bbuf.getWidth(null) < w
              || bbuf.getHeight(null) < h) {
            bbuf = createImage(w, h);
            if (bbufG != null) {
                bbufG.dispose();
            }
            bbufG = bbuf.getGraphics();

        }

        // Clear the background.
        bbufG.setColor(Color.lightGray);
        bbufG.fillRect(0, 0, w, h);

 
	for(int i=0;i<xnum;i++)for(int j=0;j<ynum;j++){
	    int sw=xGrid,sh=yGrid, sx=mm.grids[i][j].getStatus()*sw;
	    int dx=i*sw;
	    int dy=j*sh;
	    bbufG.drawImage(imgButton,dx,dy,dx+sw,dy+sh,sx,0,sx+sw,sh,this);
	}

        // Draw the off-screen image to the display.
        g.drawImage(bbuf, 0, 0, this);
		
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    public void markGrid(int x,int y){
	int i=x/xGrid;
	int j=y/yGrid;
	//	mm.mark(i,j);
    }
    public void openGrid(int x,int y){
	mm.setPos(x/xGrid,y/yGrid);	
    }

    class MouseEventHandler extends MouseAdapter {
	public void mouseClicked(MouseEvent evt){
	 if((evt.getModifiers() & InputEvent.BUTTON1_MASK)
	    == InputEvent.BUTTON1_MASK){
	     openGrid(evt.getX(),evt.getY());    
	     repaint();
	 }
	}
    }
}//end of mineCanvas

class LED extends Component{
    private int width, height;
    private Image image;
    private int counter=0, digits=3;
    public void setCounter(int c){
	counter = c;
	repaint();
    }
    LED(int digits, Image image) {
	this.digits=digits;
        MediaTracker tracker = new MediaTracker(this);

        this.image = image;
        try {
            tracker.addImage(image, 0);
            tracker.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        width = image.getWidth(null) /10;
        height = image.getHeight(null);
    }



    public void paint(Graphics g) {
	for(int i=0;i<digits;i++){
	    int sx=getDigit(i)*width;
	    int dx=i*width;
	    g.drawImage(image,dx,0,dx+width,height,sx,0,sx+width,height,this);
	}
    }

    private int getDigit(int i){
	double cnt=counter;
	int r=(int)Math.floor(cnt/Math.pow(10,digits-i-1)) ;
	return r%10;
    }

    public Dimension getPreferredSize() {
        return new Dimension(width*digits, height);
    } 



}//end of LED

class Mine{  
    private int score=0;
    private int unitScore=3;
    private int stepScore=1;

    boolean selected=false;
    int selx=-1,sely=-1;
    int[][] set3=new int[3][2];

    int width, height;
    GridMine [][] grids=null;
    //up,down, left,right, upperleft,downright, upperright,downleft
    int[][] neighbor8={{0,-1},{0,1},{-1,0},{1,0},{-1,-1},{1,1},{1,-1},{-1,1}};
    public int getScore(){return score;}
    Mine(int x,int y){
	width=x;height=y;
	//randomize

	grids=new GridMine[x][y];
	for(int i=0;i<x;i++)for(int j=0;j<y;j++){
	    grids[i][j]=new GridMine();
	    grids[i][j].setEmpty();
	}
	for(int i=0;i<x;i++)for(int j=0;j<y;j++){
	    constructNeighbor(i,j);
	}

	setMines(3);
    }
    public void constructNeighbor(int x, int y){
	//called by mine(int,int)
	GridMine g=grids[x][y];
	for(int k=0;k<8;k++){
	    int i=x+neighbor8[k][0];
	    int j=y+neighbor8[k][1];
	    try{
		g.nb8[k]=grids[i][j];
	    }catch(ArrayIndexOutOfBoundsException e){
		g.nb8[k]=null;
	    }
	}
    }
    int nMaxBalls=7;
    public void setMines(int nMax){
	int n=0;
	while(n < nMax){
	    int i=(int)(Math.random()*width);
	    int j=(int)(Math.random()*height);
	    GridMine g=grids[i][j];
	    if( g.isEmpty()){
		g.setStatus( (int)(Math.random()*nMaxBalls)+1);
		set3[n][0]=i;
		set3[n][1]=j;
		n++;		
	    }
	}
    }
    public void setPos(int x, int y){
	GridMine g=grids[x][y];
	if((g.isEmpty() || g.isSelected()) && !selected) return;//select an illeal grid
	else if(selected && g.isEmpty()){
	    if(!move(selx,sely,x,y))return;
	    if(!check5orMore(x,y)){
		setMines(3);
		for(int i=0;i<3;i++)check5orMore(set3[i][0],set3[i][1]);
	    }
	}else if(selected && !g.isSelected()){
	    grids[selx][sely].deSelect();
	    g.setSelect(); //change selected grid
	    selx=x;sely=y;
	}else if(!selected && !g.isSelected()){
	    selected = true;
	    g.setSelect();
	    selx=x;sely=y;
	}
    }
    public boolean findPath(GridMine src,GridMine dst){
	boolean b=false;
	if(src == dst)return true;
	for(int k=0;k<4;k++){
	    GridMine g=src.nb8[k];
	    if(g!=null && !g.mark && g.isEmpty()){
		g.mark=true;
		b=findPath(g,dst);
		if(b)return true;
	    }
	}
	return b;
    }

    public boolean move(int sx,int sy, int dx,int dy){	
	for(int i=0;i<width;i++)for(int j=0;j<height;j++)
	    grids[i][j].mark = false;
	grids[sx][sy].mark=true;
	if(! findPath(grids[sx][sy],grids[dx][dy]))return false;
	selected = false;
	grids[sx][sy].deSelect();
	grids[dx][dy].setStatus(grids[sx][sy].getStatus());
	grids[sx][sy].setEmpty();
	return true;
    }
    public boolean check5orMore(int x, int y){
	int c;
	try{
	    c=grids[x][y].getStatus();
	}catch(ArrayIndexOutOfBoundsException e){return false;}
	for(int k=0;k<8;k+=2){//4 directions
	    GridMine g=grids[x][y];
	    int n=1;
	    while(g.nb8[k]!=null && c==g.nb8[k].getStatus()){
		n++;
		g=g.nb8[k];
	    }
	    g=grids[x][y];
	    while(g.nb8[k+1]!=null && c==g.nb8[k+1].getStatus()){
		n++;
		g=g.nb8[k+1];
	    }
	    if(n < 5)continue;
	    //begin 5 or More
	    score+=5*Math.pow(unitScore,n-4);
	    g=grids[x][y];
	    while(g.nb8[k]!=null && c==g.nb8[k].getStatus()){		
		g=g.nb8[k];
		g.setEmpty();
	    }
	    g=grids[x][y];
	    while(g.nb8[k+1]!=null && c==g.nb8[k+1].getStatus()){ 
		g=g.nb8[k+1];
		g.setEmpty();
	    }
	    grids[x][y].setEmpty();
	    return true;
	}
	return false;
    }
}//end of Mine

class GridMine{
    static int UL=4;
    static int UP=0;
    static int UR=6;
    static int LT=2;
    static int RT=3;
    static int DL=7;
    static int DN=1;
    static int DR=5;

    static int select = 9;
    private int status; //
    GridMine[] nb8={null,null,null,null,
		    null,null,null,null}; 
    boolean mark=false;
    public void setEmpty(){status = 0;}
    public boolean isEmpty(){if(status == 0)return true;else return false;}
    public int getStatus(){return status;}
    public void setStatus(int s){status=s;}
    public boolean isSelected(){
	if(status > select)return true;
	else return false;
    }
    public void setSelect(){
	if(status <= 0)return;
	status+=select;
    }
    public void deSelect(){
	if(status <= select)return;
	status-=select;
    }
    GridMine(){
    }


}

