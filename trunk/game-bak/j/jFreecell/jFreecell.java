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

public class jMine extends Frame {

    public void paint(Graphics g) {
    }

    //StampCanvas StampCanvas = new StampCanvas();
    MineCanvas mineCanvas=new MineCanvas();
    Panel palette = new Panel(new FlowLayout());

    jMine() {
        //super("MouseMotionAdapter Example");
        // Initialize palette with stamps.
	try{
	    setLayout(new BorderLayout());
	    palette.add( new KingButton(getToolkit().getImage("mFace.gif")),BorderLayout.CENTER);	    	   
	    palette.add(new LED(3,getToolkit().getImage("led.gif")),BorderLayout.WEST);
	    palette.add(new LED(3,getToolkit().getImage("led.gif")),BorderLayout.EAST);	 
	   
	}catch(Exception e){}



        // Layout components
        add(palette, BorderLayout.NORTH);
        add(mineCanvas, BorderLayout.CENTER);
        setSize(500, 300);

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
        new jMine();
   }
}//end of class stamps

class KingButton extends Component{
    int width, height;
    int status = 0;
    Image image;
  
    KingButton(Image image) {

        MediaTracker tracker = new MediaTracker(this);

        this.image = image;
        try {
            tracker.addImage(image, 0);
            tracker.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        width = image.getWidth(null) /4;
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
	    status=2; //select the 3rd picture
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
    int width, height;
    int status = 0;
    Image imgFace,imgLED,imgButton;
    
    int xnum=10, ynum=10;
    int xGrid=16, yGrid=16;
    Mine mm;
    // Double buffer variables
    Image bbuf;
    Graphics bbufG;
    MineCanvas() {
	xnum=ynum=10;
	mm =new Mine(xnum,ynum);
        MediaTracker tracker = new MediaTracker(this);

        imgFace = getToolkit().getImage("mFace.gif");
	imgLED = getToolkit().getImage("led.gif");
	imgButton =getToolkit().getImage("mButton.gif");
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
	    int sw=xGrid,sh=yGrid, sx=mm.grids[i][j].status*sw;
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
	mm.mark(i,j);
    }
    public void openGrid(int x,int y){
	int i=x/xGrid;
	int j=y/yGrid;
	mm.openMine(i,j);
    }

    class MouseEventHandler extends MouseAdapter {
	public void mouseClicked(MouseEvent evt){
	 if((evt.getModifiers() & InputEvent.BUTTON3_MASK)
	    == InputEvent.BUTTON3_MASK){
	     markGrid(evt.getX(),evt.getY());
	     repaint();
	 }
	 if((evt.getModifiers() & InputEvent.BUTTON1_MASK)
	    == InputEvent.BUTTON1_MASK){
	     openGrid(evt.getX(),evt.getY());    
	     repaint();
	 }
	}
    }
}//end of mineCanvas

class LED extends Component{
    int width, height;
    Image image;
    int counter=0, digits=3;
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
    int width, height;
    GridMine [][] grids=null;
    int[][] neighbor8={{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
    Mine(int x,int y){
	width=x;height=y;
	//randomize

	grids=new GridMine[x][y];
	for(int i=0;i<x;i++)for(int j=0;j<y;j++){
	    grids[i][j]=new GridMine();
	    grids[i][j].status= MineStatus.cover;
	    grids[i][j].nMines = 0;
	}
	int n=0;
	while(n < 8){
	    int i=(int)(Math.random()*width);
	    int j=(int)(Math.random()*height);
	    if(! grids[i][j].isMine()){
		grids[i][j].setMines();
		n++;
		incMine(i,j);//increase neighbor's mines count
	    }
	}
    }
    public void incMine(int x, int y){
	for(int k=0;k<8;k++){
	    try{
		int i=x+neighbor8[k][0];
		int j=y+neighbor8[k][1];
		if(! grids[i][j].isMine())grids[i][j].nMines++;
	    }catch(ArrayIndexOutOfBoundsException e){}
	}	
    }
    public void openMine(int x,int y){
	try{
	    GridMine g=grids[x][y];
	    if(g.isOpened() || g.isMarked())return;
	    g.status=grids[x][y].nMines;
	    if(g.nMines == 0){
		for(int k=0;k<8;k++){
		    int i=x+neighbor8[k][0];
		    int j=y+neighbor8[k][1];
		    openMine(i,j);
		}	
	    }
	}catch(ArrayIndexOutOfBoundsException e){}
    }
    public void mark(int x, int y){
	if(x<0 || x>= width || y<0 || y>=height)return;
	if(grids[x][y].status==MineStatus.cover)
	    grids[x][y].status = MineStatus.mark;
	else if(grids[x][y].status==MineStatus.mark)
		grids[x][y].status = MineStatus.cover;
    }
}//end of Mine

class GridMine{
    int status; //
    int nMines;
    GridMine(){
    }
    public void setMines(){nMines= MineStatus.mine;}
    public void setNeighborMines(int i){nMines=i;}
    public boolean isMine(){
	if(nMines == MineStatus.mine)return true;
	else return false;
    }
    public boolean isOpened(){
	if(status < 10)return true;
	else return false;
    }
    public boolean isMarked(){
	if(status == MineStatus.mark)return true;
	else return false;
    }
}

class MineStatus{
    static int cover=10;
    static int mark=11;
    static int mine =12;
    static int notmine =13;
    static int explode =14;
}
