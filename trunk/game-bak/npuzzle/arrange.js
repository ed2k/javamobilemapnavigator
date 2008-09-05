var gsize, ghrow, ghcol, gtime, gmoves, gintervalid=-1, gshuffling;

function toggleHelp()
{
   if (butHelp.value == "Hide Help")
      {
         help.style.display = "none";
         butHelp.value = "Show Help";
      }
   else
      {
         help.style.display = "";
         butHelp.value = "Hide Help";
      }  
}

//random number between low and hi
function r(low,hi)
{
   return Math.floor((hi-low)*Math.random()+low); 
}

//random number between 1 and hi
function r1(hi)
{
   return Math.floor((hi-1)*Math.random()+1); 
}

//random number between 0 and hi
function r0(hi)
{
   return Math.floor((hi)*Math.random()); 
}

function startGame()
{
   shuffle();
   updateBoard();
   gtime = 0;
   gmoves = 0;
   clearInterval(gintervalid);
   tickTime();
   gintervalid = setInterval("tickTime()",1000);
}

function stopGame()
{
   if (gintervalid==-1) return;
   clearInterval(gintervalid);
   fldStatus.innerHTML = "";
   gintervalid=-1;
}

function tickTime()
{
   showStatus();
   gtime++;
}

function checkWin()
{
   var i, j, s;
  
   if (gintervalid==-1) return; //game not started!
  
   if (!isHole(gsize-1,gsize-1)) return;
  
   for (i=0;i<gsize;i++)for (j=0;j<gsize;j++){
            if (!(i==gsize-1 && j==gsize-1)){ //ignore last block (ideally a hole)
                  if (getValue(i,j)!=(i*gsize+j+1).toString()) return;
               }
         }
   stopGame();

   s = "<table cellpadding=4>";
   s += "<tr><td align=center class=capt3>!! CONGRATS !!</td></tr>";
   s += "<tr class=capt4><td align=center>You have done it in " + gtime + " secs ";
   s += "with " + gmoves + " moves!</td></tr>";
   s += "<tr><td align=center class=capt4>Your speed is " + Math.round(1000*gmoves/gtime)/1000 + " moves/sec</td></tr>";
   s += "</table>";
   fldStatus.innerHTML = s;
   //  shuffle();
}

function showStatus()
{
   fldStatus.innerHTML = "Time:&nbsp;" + gtime + " secs&nbsp;&nbsp;&nbsp;Moves:&nbsp;" + gmoves
      }

function updateBoard(){
  for (var i=0; i<grid.width; i++) for (var j=0; j<grid.height; j++){
	var b = ID("a_" + i + "_" + j);
	b.innerHTML = grid.m[i][j].label;
  }
}
function showTable()
{
   var i, j, s;
  
   stopGame();
   s = "<table border=3 cellpadding=0 cellspacing=0 bgcolor='#666655'><tr><td class=bigcell>";
   s = s + "<table border=0 cellpadding=0 cellspacing=0>";
   for (i=0; i<grid.width; i++)
      {
         s = s + "<tr>";    
         for (j=0; j<grid.height; j++)
            {
               s = s + "<td id=a_" + i + "_" + j + " onclick='move(this)' class=cell>" + "</td>";
            }
         s = s + "</tr>";        
      }
   s = s + "</table>";
   s = s + "</td></tr></table>";      
   return s;
}

function getCell(row, col)
{
   //return eval("a_" + row + "_" + col);
   return document.getElementById("a_" + row + "_" + col);
}

function setValue(row,col,val)
{
   var v = getCell(row, col);
   v.innerHTML = val;
   v.className = "cell";
}

function getValue(row,col)
{
   //  alert(row + "," + col);

   var v = getCell(row, col);
   return v.innerHTML;
}

function setHole(row,col)
{ 
   var v = getCell(row, col);
   v.innerHTML = "";
   v.className = "hole";
   ghrow = row;
   ghcol = col;
}

function getRow(obj)
{
   var a = obj.id.split("_");
   return a[1];
}

function getCol(obj)
{
   var a = obj.id.split("_");
   return a[2];
}

function isHole(row, col)
{
   return (row==ghrow && col==ghcol) ? true : false;
}

function getHoleInRow(row)
{
   var i;
  
   return (row==ghrow) ? ghcol : -1;
}

function getHoleInCol(col)
{
   var i;

   return (col==ghcol) ? ghrow : -1;
}

function shiftHoleRow(src,dest,row)
{
   var i;

   //conversion to integer needed in some cases!
   src = parseInt(src);
   dest = parseInt(dest);

   if (src < dest)
      {
         for (i=src;i<dest;i++)
            {
               setValue(row,i,getValue(row,i+1));
               setHole(row,i+1);
            }
      }
   if (dest < src)
      {
         for (i=src;i>dest;i--)
            {
               setValue(row,i,getValue(row,i-1));
               setHole(row,i-1);
            }
      }
}

function shiftHoleCol(src,dest,col)
{
   var i;
  
   //conversion to integer needed in some cases!
   src = parseInt(src);
   dest = parseInt(dest);
    
   if (src < dest)
      {//alert("src=" + src +" dest=" + dest + " col=" + col);
         for (i=src;i<dest;i++)
            {//alert(parseInt(i)+1);
               setValue(i,col,getValue(i+1,col));
               setHole(i+1,col);
            }
      }
   if (dest < src)
      {
         for (i=src;i>dest;i--)
            {
               setValue(i,col,getValue(i-1,col));
               setHole(i-1,col);
            }
      }
}

function move(obj)
{
   var r, c, hr, hc;

   if (gintervalid==-1 && !gshuffling) 
      {
         alert('Please press the "Start Game" button to start.')
            return;
      }
   r = getRow(obj);
   c = getCol(obj);
   if (isHole(r,c)) return;
  
   hc = getHoleInRow(r);
   if (hc != -1)
      {
         shiftHoleRow(hc,c,r);
         gmoves++;
         checkWin();
         return;
      }
  
   hr = getHoleInCol(c);

   if (hr != -1)
      {
         shiftHoleCol(hr,r,c);
         gmoves++;
         checkWin();
         return;
      }
}

function shuffle()
{
   var t,i,j;

   for (i=0;i<100;i++) {
     j = r0(4);
     if (j==0) {
        grid.moveBlank(-1,0);
     } else if (j==1){
        grid.moveBlank(1,0);
     } else if (j==2) {
        grid.moveBlank(0,-1);
     } else {
        grid.moveBlank(0,1);
     }
   }
}

var grid;
function ID(s){return document.getElementById(s);}
function loadBoard(size)
{
   grid = new Grid(size);

   gsize = size;
  
   ID('board').innerHTML = showTable(gsize);
   setHole(gsize-1,gsize-1);
   updateBoard();
   //shuffle();
}

function Grid(size){
     this.width = size;
     this.height = size;
     this.rand = 2;
     this.m = new Array(this.width);
     for(var i=0;i<this.width;i++){
        this.m[i] = Array(this.height) 
        for(var j=0;j<this.height;j++){
	this.m[i][j] = new Cell(i+j*this.height+1,i,j);
     } }
     // ok to lost one cell of memory    	
     this.blank = new Cell('',this.width-1,this.height-1);
     this.m[this.width-1][this.height-1] = this.blank;
}

Grid.prototype.moveBlank = function Grid_moveBlank(dx,dy){
   var blank = this.blank
   var x = blank.x + dx;
   var y = blank.y + dy;
   if (x <0 || x >= this.width || y < 0 || y>= this.height) return;

   // swap 
   this.set(this.m[x][y], blank.x, blank.y);
   this.set(this.m[blank.x][blank.y], x, y);
   // remember which is blank to save search time
   this.set(this.blank, x, y);
}

Grid.prototype.set = function Grid_set(cell, x, y){
   this.m[x][y]=cell;
   cell.setLocation(x,y);
}

function Cell(str, x, y) {
	this.label = str;
	this.ix = x;	// initial location in grid coordinates
	this.iy = y;
	this.x = x; 	// current location in grid coordinates
   	this.y = y;
}

Cell.prototype.setLocation = function Cell_setLocation(x,y){
	this.x = x;
	this.y = y;
}

Cell.prototype.isHome = function Cell_isHome(x,y) {
	 return ((x == this.ix) && (y == this.iy));
}
