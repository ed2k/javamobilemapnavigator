unit Sfree10;{use for aiSearch unable draw when know how to solve 1941
add animation demo}
{用计算机解freecell纸牌游戏}
interface

uses
  SysUtils, WinTypes, WinProcs, Messages, Classes, Graphics, Controls,
  Forms, Dialogs, ExtCtrls, StdCtrls, Menus, Buttons, Gauges;
const{牌局 1941}
   T=10;J=11;Q=12;K=13;S=0;H=1;D=2;C=3;
   p1941:array [0..7,-1..18]of integer=(
    (7,9-1+13*D,6-1+13*S,4-1+13*D,1-1+13*H,5-1+13*C,4-1+13*H,K-1+13*H,0,0,0,0,0,0,0,0,0,0,0,0),
    (7,J-1+13*H,J-1+13*C,J-1+13*S,7-1+13*S,6-1+13*D,1-1+13*C,4-1+13*S,0,0,0,0,0,0,0,0,0,0,0,0),
    (7,1-1+13*D,8-1+13*D,1-1+13*S,2-1+13*H,K-1+13*C,K-1+13*D,7-1+13*H,0,0,0,0,0,0,0,0,0,0,0,0),
    (7,K-1+13*S,T-1+13*S,9-1+13*S,8-1+13*H,J-1+13*D,Q-1+13*D,3-1+13*H,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,T-1+13*C,4-1+13*C,2-1+13*S,5-1+13*D,3-1+13*S,6-1+13*H,    0,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,3-1+13*C,Q-1+13*C,5-1+13*S,Q-1+13*S,Q-1+13*H,7-1+13*D,    0,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,9-1+13*H,5-1+13*H,7-1+13*C,6-1+13*C,3-1+13*D,T-1+13*D,    0,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,2-1+13*C,8-1+13*C,8-1+13*S,T-1+13*H,2-1+13*D,9-1+13*C,    0,0,0,0,0,0,0,0,0,0,0,0,0));
   p19:array [0..7,-1..18]of integer=({牌局 19}
   (7,K-1+13*C,J-1+13*H,1-1+13*H,6-1+13*H,K-1+13*S,8-1+13*D,6-1+13*S,0,0,0,0,0,0,0,0,0,0,0,0),
    (7,8-1+13*S,6-1+13*C,7-1+13*H,K-1+13*D,5-1+13*D,K-1+13*H,9-1+13*S,0,0,0,0,0,0,0,0,0,0,0,0),
    (7,3-1+13*D,5-1+13*S,4-1+13*H,8-1+13*C,Q-1+13*C,1-1+13*S,4-1+13*D,0,0,0,0,0,0,0,0,0,0,0,0),
    (7,2-1+13*H,9-1+13*C,7-1+13*D,T-1+13*D,Q-1+13*D,3-1+13*C,6-1+13*D,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,5-1+13*C,2-1+13*D,9-1+13*D,2-1+13*S,T-1+13*C,4-1+13*C,    0,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,1-1+13*D,4-1+13*S,7-1+13*C,J-1+13*S,J-1+13*D,Q-1+13*H,    0,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,9-1+13*H,Q-1+13*S,J-1+13*C,3-1+13*H,7-1+13*S,5-1+13*H,    0,0,0,0,0,0,0,0,0,0,0,0,0),
    (6,8-1+13*H,T-1+13*H,3-1+13*S,T-1+13*S,1-1+13*C,2-1+13*C,    0,0,0,0,0,0,0,0,0,0,0,0,0));
   maxlist=8000;{for aisearch,最大扩展节点数 }
   L0:integer=1;{L0 -L4 :费用函数计算时所用到的参数权值}
   L1:integer=2;
   L2:integer=1;
   L3:integer=300;
   L4:integer=100;
   L5:integer=2000;{期望搜索后减少的费用值}
  {next=-1 to -8 :in stack3
      100 to 103 in stack 2
      60 to 63 stack1
   spade=0;
   heart=1;
   diamond=2;
   club=3;}
   {for seleced}
   none=-1100;{标识无}
   pk0=0;{标识牌}
   s1=60;s2=100;s3=-8;{标识栈号}
   aiDo=-999;{for draw,selected,标识计算机搜索状态}
   {for ai,标识移动牌方向,例如s2tos1表明牌从栈2移动到栈1}
   s2tos1=-900;s2tos3=-800;s3tos1=-700;s3tos2=-600;s3tos3=-500;s2tos2=-400;
   UndoLevel=200;{for undo最多记录步数}
   {jie mian,界面参数}
   pkx=71;pky=96;{牌的大小}
   s1x=350;s1y=5; {栈1初始位置}
   s2x=2;s2y=5;   {栈2初始位置}
   s3x=10;s3y=110;{栈3初始位置}
   gapx=8;gapy=15;{栈3中牌与牌的间隔}

type
  TPai=class(TImage) {牌类}
  private
  x,y:integer;{牌当前逻辑上所在位置}
  public
  procedure inverse;{牌反显}
end;
type
  PMv=^TMv;
  TMv=record  {走步记录结构}
   i:integer;{total steps 0-200} {已经记录步数0-(i-1)}
   m:array [0..undolevel]of integer;{maximum 200 steps,
   记录目的牌(旧版本),记录牌移动方向(新版本)}
   n:array [0..undolevel]of integer;{move numbers
   记录要移动牌(旧版本),记录牌移动数目(新版本)}
  end;
  PPaiJu=^TPaiJu;
  TPaiJu=record{牌局记录结构}
   id:integer; {牌标识,no use}
   val:longint;{该牌局下费用数}
   isOpenList:boolean;
   st1:array [0..3]of integer;{栈1}
   st2:array [0..3]of integer;{栈2}
   st3:array [0..7,-1..20]of integer;{栈3}
   mv:TMv;{从初始牌局到当前牌局移动记录}
  end;
type SList=array [0..maxlist]of ^TPaiju ;{for aisearch,节点指针}
type TidxList=array [0..maxlist]of integer;{节点索引记录}
  TFreecell = class(TForm)
    pk: TImage;{牌的bmp图片}
    imgKing: TImage;{界面中上方K的bmp图片}
    MainMenu1: TMainMenu;{主菜单}
    mGame1: TMenuItem;{Game菜单}
    mHelp1: TMenuItem;{Help菜单}
    mNewGame1: TMenuItem; {New Game 菜单}
    mAI1: TMenuItem;  {Ai菜单}
    mSave1: TMenuItem;{Save 选项}
    mUndo1: TMenuItem; {Undo 选项}
    mInput1: TMenuItem; {Input 菜单}
    mParameter1: TMenuItem;  {Paremeters... 选项}
    mPai19411: TMenuItem;
    mPai191: TMenuItem;
    mSunfree1: TMenuItem;
    mOpen1: TMenuItem;
    mReplay1: TMenuItem;  {mXXX的均表示菜单类}
    dlgOpen1: TOpenDialog;{dlgXXX:对话框类}
    dlgSave1: TSaveDialog;
    Timer1: TTimer;       {时钟}
    function aiFlyPai:integer;{飞牌:牌上到右上角栈1中}
    function aiEvaluate:longint;{对当前牌局费用进行评价}
    function CopyPai:integer; {把sys牌局与pai[].xy 相关联}
    function aiCopyList(var des:TPaiju;src:TPaiju):integer;{
     把源牌局拷贝到目的牌局}
    function aiMeet(var pos:integer):boolean;{判断当前牌局是否已经在
     生成节点(List[])之中,若是返回true,否pos中方插入位置}
    function aiSearch(var ju:TPaiju):boolean;{计算机解牌搜索程序}
    function aiUndo:integer;{undo 1 step in mv,一级Undo}
    function f(p:integer):integer;{标识与实际使用值换算}
    function g(p:integer):integer;{标识与实际使用值换算}
    function s(p:integer):integer;{标识状态判断}
    function Chained(p,q:integer):boolean;{for stack3 判断牌P与牌Q能否成链}
    function Rule(p,q:integer):boolean;{判断牌Q能否放在牌P下}
    function Join(p,q:integer):integer;{Join y to p把牌Q放在牌P下}
    function RememberStep(step,number:integer):integer;{记录牌的移动}
    function Init:integer;{设置初始牌局}
    function CountPai(stack:integer):integer;  {how many pai in one of
    stack in stack3,计算栈3的一列中有几张牌}
    function CountFreeStack(stack:integer):integer;{计算栈2中有几个空}
    function CountMaxChain(stack:integer):integer;{计算栈3的一列中有几张牌
    构成链}
    function FindLast(p:integer):integer;{返回栈3的第P列中最后一张牌}
    function FindFreeStack(stack:integer):integer;{return stacknumber
    返回栈2中的空的位置}
    function FindPai(p:integer ;var x,y:integer;ju:TPaiju):integer; {
    找牌P,返回牌P的位置参数}
    function Draw(p:integer):integer;{for jiemian,画一列牌}
    function ReDraw:integer;{accouding to sys,根据sys记录重画牌局}
    procedure pkClick(Sender: TObject);
    procedure FormMouseDown(Sender: TObject; Button: TMouseButton;
       Shift: TShiftState; X, Y: Integer);
    procedure FormCreate(Sender: TObject);
    procedure pkDblClick(Sender: TObject);
    procedure mSave1Click(Sender: TObject);
    procedure mUndo1Click(Sender: TObject);
    procedure pkMouseDown(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure mAiClick(Sender: TObject);
    procedure mDealClick(Sender: TObject);
    procedure mPai19411Click(Sender: TObject);
    procedure mPai191Click(Sender: TObject);
    procedure mParameter1Click(Sender: TObject);
    procedure mSunfree1Click(Sender: TObject);
    procedure mOpen1Click(Sender: TObject);
    procedure mReplay1Click(Sender: TObject);
    procedure FormPaint(Sender: TObject);
    procedure imgKingMouseDown(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure imgKingMouseUp(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure Timer1Timer(Sender: TObject);
    procedure mHelp1Click(Sender: TObject);
  private
    { Private declarations }
  public
    { Public declarations }
   sys:TPaiju;{系统牌局记录,是操作的默认对象}
   InitPai:TPaiju;{initial set,初始牌局}
   Pai:array [0..51] of TPai;{52张牌}
   Selected:integer;{人玩牌时的状态标识}
   {for aiSearch}
   List:^SList;{扩展节点表}
   idxList:^TidxList;{for list sort,扩展节点表,根据费用从大到小排序}
   NodeList:TList;
   idxNodeList:TList;
   iList:integer;{记录已经生成节点个数}
   NewMv:integer;{用于记录计算机解牌后的步数}
  end;{TFeecell}
var
  Freecell: TFreecell;
implementation
uses hup2,HU1;
{$R *.DFM}
function TFreecell.aiFlyPai:integer;{fly pai to stack1}
{飞牌:牌上到右上角栈1中}
{用到的类内公共变量:sys}
var i,j,p:integer;
begin{}
 aiFlyPai:=none;
 for i:=0 to 11do begin{0 to 3 in stack 2; 4 to 11 in stack3}
  {找到一张可以移动的牌}
  if i<4 then if s(sys.st2[i])<>pk0 then continue else p:=sys.st2[i]
  else if s(sys.st3[i-4][0])<>pk0 then continue else p:=FindLast(sys.st3[i-4][0]);
  {判断找到的牌}
  {是A则直接入栈1}
  if(f(p)<> 0)then begin
   {是2则判断对应栈1的槽中是否有A,有则放入2}
   if(p<>(sys.st1[g(p)]+1))then continue;
   {是3以上的牌(包括3)则不但对应槽中应该有比它小1点的牌}
   {,而且对应不同的两种花色都应该已经放入了比它小2点的牌}
   {例如:找到黑桃9,那么黑桃8,红心7和方块7都在栈1才能把黑桃9放入}
   if(f(p)<>1)then begin
    if((g(p)=0)or(g(p)=3))and
    ((f(sys.st1[1])<f(p)-2)or(f(sys.st1[2])<f(p)-2)or(sys.st1[1]=s1+1)or(sys.st1[2]=s1+2))then continue
    else if((g(p)=1)or(g(p)=2))and
    ((f(sys.st1[0])<f(p)-2)or(f(sys.st1[3])<f(p)-2)or(sys.st1[0]=s1)or(sys.st1[3]=s1+3))then continue;
   end;
  end;
  Join(s1+g(p),p);aiFlyPai:=p;
 end;{i}{1.find 'A' 2.find '2' 3. find others}
end;
function TFreecell.aiCopyList(var des:TPaiju;src:TPaiju):integer;
{把源牌局拷贝到目的牌局}
{用到的类内公共变量:无}
var i,j:integer;
begin
 des.id:=src.id;
 des.isOpenlist:=src.isOpenlist;
 des.val:=src.val;
 for i:=0to 3 do des.st1[i]:=src.st1[i];
 for i:=0to 3 do des.st2[i]:=src.st2[i];
 for i:=0 to 7 do for j:=-1 to 20 do des.st3[i][j]:=src.st3[i][j];
 des.mv.i:=src.mv.i;
 for i:=0 to src.mv.i do begin
  des.mv.m[i]:=src.mv.m[i];
  des.mv.n[i]:=src.mv.n[i];
 end;
end;
function TFreecell.CopyPai:integer;
{把sys牌局与pai[].xy 相关联}
{用到的类内公共变量:sys,pai}
var i,j,p:integer;
begin
 for i:=0 to 3 do begin
  p:=sys.st1[i];
  if s(p)<>pk0 then continue;
  for j:=13*g(p) to p do Pai[j].x:=s1+i;
 end;
 for i:=0 to 3 do begin
  p:=sys.st2[i];
  if s(p)<>pk0 then continue;
  Pai[p].x:=s2+i;
 end;
 for i:=0 to 7 do for j:=0to 20 do begin
  p:=sys.st3[i][j];
  if s(p)<>pk0 then break;
  Pai[p].x:=i;Pai[p].y:=j;
 end;
end;
function TFreecell.aiEvaluate:longint;
{对当前牌局费用进行评价}
{用到的类内公共变量:sys}
var i,j,m,n,p,q,freestacknum:integer;
   ev:longint;
begin
{ ev:=4*L4;}
 ev:=0;
 freestacknum:=CountFreeStack(s2)+CountFreeStack(s3);
 case freestacknum of{计算空栈数}
 3:inc(ev,L3);{L3:空栈数评估,越少费用越高}
 2:inc(ev,L3*2);
 1:inc(ev,L3*4);
 0:inc(ev,L3*10);
 end;
 for i:=0 to 7 do begin
  n:=CountPai(i);
  if n=0 then begin {dec(ev,L4);}continue;end;{-L4}
  if n=1 then continue;{某列没有牌或只有一张牌不计算费用}
  m:=CountMaxChain(i);
  if(n=m)and(f(sys.st3[i][0])=12){'K'}then continue;
{  inc(ev,L0*n*(41-n)div 2);L0:某列牌是K以下一顺不计算费用}
  for j:=0 to n-1 do  begin
   p:=sys.st3[i][j];{计算每列牌长度的费用以牌点计算越小越少}
   inc(ev,L0*(n-j-1)*(36-n+j)*(12-f(p))div 2);{L0:某列牌长度费用乘数}
  end;
  if n<=(freestacknum+1) then continue;{某列牌数比空栈数少不计算乱序费用}
  if(n<>m)then inc(ev,L1*n*(37-n)div 2);{计算每列牌乱序长度的费用}
  inc(ev,L2*m*(37-m)div 2); {L1:某列牌乱序长度费用乘数}
  {L2:某列牌成顺长度费用}
 end;{for i}
 aiEvaluate:=ev;
end;
function TFreecell.aiMeet(var pos:integer):boolean;
{判断当前牌局是否已经在生成节点(List[])之中,若是返回true,否pos中方插入位置}
{用到的类内公共变量:sys,iList,idxList,List}
var i,j,iH,iL,iM,x,y,oldx,oldy:integer;
   meet:boolean;
label myjump;
begin
 aiMeet:=false;
 if iList=0 then begin pos:=0;exit;end;
 {half search}
 meet:=false;
 iL:=iList-1;iH:=0;{list^[idxlist^[0]].val>list^[idxlist^[ilist-1]].val}
 for i:=0 to maxlist do begin{对半搜索}
  if iH>iL then break;
  iM:=(iL+iH)div 2;
  if List^[idxlist^[iM]]^.val>sys.val then iH:=iM+1
  else if List^[idxlist^[iM]]^.val<sys.val then iL:=iM-1
  else meet:=true;
  if meet then break;
 end;
 {judge if val equal in list}
 if not meet then begin
  if iH>iM then pos:=iH else pos:=iM;
  exit;{if meet then is iM}
 end;
 pos:=iM;
 {找出费用值相同的牌}
 for iH:=iM downto 0 do if List^[idxlist^[iH]]^.val<>sys.val then break;
 if iH<>0 then inc(iH)else iH:=0;
 for iL:=iM to iList-1 do if List^[idxlist^[iL]]^.val<>sys.val then break;
 if iL<>(iList-1) then dec(iL)else iL:=iList-1;
 meet:=false;
 {判断牌型是否重复}
 {重复包括:}
 {1:栈2之中四张牌相同(但是并非顺序相同)}
 {2:栈3之中每列牌对应相同(但是并非列与列顺序相同)}
 for iM:=iH to iL do begin{compare iL-iH+1 ju pai}
  for i:=0 to 3 do if sys.st1[i]<>List^[idxlist^[iM]]^.st1[i] then goto myjump;
  for i:=0 to 3 do begin
   FindPai(sys.st2[i],x,y,List^[idxlist^[iM]]^);
   if s(x)<>s2 then goto myjump;
  end;
  for i:=0 to 7 do begin
   if(s(sys.st3[i][0])<>pk0)then continue;{empty colume}
   FindPai(sys.st3[i][0],x,y,list^[idxlist^[iM]]^);
   if y<>0 then goto myjump;
   oldx:=x;
   for j:=1 to 18 do begin
    if s(sys.st3[i][j])<>pk0 then break;{no pai}
    FindPai(sys.st3[i][j],x,y,list^[idxlist^[iM]]^);
    if(x<>oldx)or(y<>j) then goto myjump;
   end;{j}
  end;{i}
  meet:=true;break;
myjump:
 end;
 aiMeet:=meet;
end;
function TFreecell.aiSearch(var ju:TPaiju):boolean;
{计算机解牌搜索程序}
{用到的类内公共变量:sys,iList,idxList,List}
var i,j,p,q,tmp:integer;
    r:Pmv;
    tmpju,oldju:PPaiju;
    //new method for tlist using
    newnode:PPaiju;
    newidx:^integer;
    //new tlist method
    success:boolean;
    msg:Tmsg;
    Oldev:longint;
label mexit,havesteps,step2;
begin{A算法原理请参考人工智能书}
 huprogress.show;{show progress显示进度窗}
 success:=false;
 iList:=0;{先给初值}
 getmem(tmpju,sizeof(TPaiju));getmem(oldju,sizeof(TPaiju));
 getmem(list,sizeof(SList));getmem(idxlist,sizeof(Tidxlist));
 getmem(list^[0],sizeof(TPaiju));
//new tlist method
 for i:=0 to MaxList do begin
  new(newnode);new(newidx);
  NodeList.add(newnode);idxNodeList.add(newidx);
 end;
 getmem(NodeList.List[0],sizeof(TPaiju));
//new tlist method
 oldev:=sys.val;
 aiCopyList(oldju^,sys);{保存初始牌型以便于操作完恢复}
 aiCopyList(list^[0]^,sys);
 list^[0]^.isOpenList:=true;iList:=1;getmem(list^[iList],sizeof(TPaiju));idxlist^[0]:=0;
step2:
 peekmessage(msg,0,WM_KEYDOWN,WM_KEYDOWN,PM_REMOVE+PM_NOYIELD);
 if(msg.message=WM_KEYDOWN)then goto mexit;{如果有按键则退出}
 huprogress.gauge1.progress:=(iList div 20)mod 100 ;{show progress显示扩展节点数}
 {寻找表中未扩展节点}
 for i:=iList-1 downto 0 do if list^[idxlist^[i]]^.isOpenList then goto havesteps;
 showmessage('No step!');{未找到则退出}
 goto mexit;{nothing in openlist;fail}
 {find a min value openlist^[?]}
havesteps:{找到未扩展节点}
 {设置为已经扩展了的}
 list^[idxlist^[i]]^.isOpenList:=false;{belongs to closelist,now;}
 {为了便于操作拷贝至sys之中,同时必须关联Pai[]}
 aiCopyList(sys,list^[idxlist^[i]]^); CopyPai;
 {评估是否达到要求费用值,注意默认小于500则是可行的}
 if(sys.val<500)or((sys.val+L5)<OldEv)then begin
  success:=true;goto mexit;
 end; {meetdemand exit aiSearch:=true; exit; success}
{ find next step in sys}
 Getmem(r,sizeof(TMv));{     }
 r^.i:=0;
 {搜索当前可以移动的牌,以sys之中记录的牌型为基础搜索}
 {分五种情况:}
 {1:栈2的牌移动到栈3的某一列}
 {2:栈3某一列的一些牌移动到栈3的另一列}
 {3:栈2的牌移动到栈1}
 {4:栈3的牌移动到栈1}
 {5:栈3某一列的最后一张牌移动到栈2}
 {find step and remember it}
 if CountFreeStack(s2)<>4 then begin{search step    }
  for i:=0 to 3 do begin
   q:=sys.st2[i];if(s(q)<>pk0)then continue;{jump to i loop}
   for j:=0 to 7 do begin
    p:=sys.st3[j][0];if(s(p)=pk0)then p:=FindLast(p);
    if Rule(p,q)then begin r^.m[r^.i]:=p;r^.n[r^.i]:=q;inc(r^.i);end;
   end;{j}
  end;{for i}
 end;{ 1:s2 tos3;                            }
 for i:=0 to 7 do begin
  q:=sys.st3[i][0];if(s(q)<>pk0)then continue;{jump to i circle}
  if(CountPai(i)=CountMaxChain(i))and(f(q)=12){'K'}then continue;{forbid move when
   there is a colum K-Q-J...}
  q:=FindLast(q);
  for j:=0 to 7 do begin {2:s3 to s3;  }
   if(j=i)then continue;
   p:=sys.st3[j][0];if(s(p)=pk0)then p:=FindLast(p);
   if Rule(p,q)then begin
    r^.m[r^.i]:=p;r^.n[r^.i]:=q;inc(r^.i);
   end;{if}
  end;{j}
 end;{i}
 for i:=0 to 3 do begin{ 3:s2 to s1;  }
  q:=sys.st2[i];if(s(q)<>pk0)then continue;{jump to i loop}
  p:=sys.st1[g(q)];
  if Rule(p,q)then begin
   r^.m[r^.i]:=p;r^.n[r^.i]:=q;inc(r^.i);
  end;
 end;{for j,i}
 for i:=0 to 7 do begin {4:s3 to s1;  }
  q:=sys.st3[i][0];if(s(q)<>pk0)then continue;{jump to i circle}
  q:=FindLast(q);
  p:=sys.st1[g(q)];
  if Rule(p,q)then begin
   r^.m[r^.i]:=p;r^.n[r^.i]:=q;inc(r^.i);
  end;
 end;{for i}
 {5:s3 to s2; bad need to search again}
 p:=FindFreeStack(s2);
 if(s(p)<>none)then begin
  for i:=0 to 7 do begin {}
    q:=sys.st3[i][0];if(s(q)<>pk0)then continue;{jump to i circle}
    if CountMaxChain(i)=CountPai(i)then continue;
    q:=FindLast(q);
    r^.m[r^.i]:=p;r^.n[r^.i]:=q;inc(r^.i);
  end;{for i}
 end;{end if}
 {try step respectively}
 {如果存在移动,则分别尝试移动,然后记录下移动后的牌型以及移动步法}
{do find end;}
 if r^.i<>0 then begin{;has step}
  for i:=0 to r^.i-1 do begin
   aiCopyList(tmpju^,sys);{保存初始牌型以便于操作完恢复}
   Join(r^.m[i],r^.n[i]);
   for j:=0 to 51 do if aiFlyPai=none then break;{为了加快搜索速度,飞牌情况
   不记录,直接记录飞牌后的结果}
   sys.val:=aiEvaluate;
   {如果牌型已经在表之中,则忽略之}
   if (not aiMeet(p)) then begin{sys insert to List^;}
    sys.isOpenlist:=true;
    aiCopyList(list^[iList]^,sys);
    {牌型记录按先到先记录形式存在表中,用IdxList[]按费用值由大到小排序}
    if p<iList then for j:=iList downto p+1 do idxlist^[j]:=idxlist^[j-1];
    idxlist^[p]:=iList;
    inc(iList);getmem(list^[iList],sizeof(TPaiju));
    if(iList=Maxlist)then begin
     freemem(r,sizeof(Tmv));showmessage('List is full');
     goto mexit;
    end;
   end;{if not aimeet}
   aiCopyList(sys,tmpju^);CopyPai; {aiUndo; }
  end;{for }
  {add for naked up}
  {特殊情况处理:裸上}
  {考虑实际玩牌时的一种策略,与之相应的处理}
  {将栈3某列牌的最后几张牌一并上至栈2的空之中}
  tmp:=CountFreeStack(s2);
  if(tmp>1)then begin
   for i:=0 to 7 do begin
    q:=CountPai(i);
    if q<=tmp then continue;{如果某列牌张数小于栈2空数,则忽略特殊操作}
    {如果某列牌成一顺的牌数大于栈2空数,则忽略特殊操作}
    if tmp<CountMaxChain(i) then continue;
    aiCopyList(tmpju^,sys);
    {移动牌}
    for j:=q-1 downto q-tmp do Join(FindFreeStack(s2),sys.st3[i][j]);
    for j:=0 to 51 do if aiFlyPai=none then break;
    {以下操作类似}
    sys.val:=aiEvaluate;
    if not aiMeet(p) then begin{sys insert to List^;}
     sys.isOpenlist:=true;
     aiCopyList(list^[iList]^,sys);
     if p<iList then for j:=iList downto p+1 do idxlist^[j]:=idxlist^[j-1];
     idxlist^[p]:=iList;
     inc(iList);getmem(list^[iList],sizeof(TPaiju));
     if(iList=Maxlist)then begin
      freemem(r,sizeof(Tmv));showmessage('List is full');
      goto mexit;
     end;
    end;{if not aimeet}
    aiCopyList(sys,tmpju^);CopyPai; {恢复初始牌型}
   end;{i}
  end;{if tmp<>0}
 end;{end if r^.i<>0}
 freemem(r,sizeof(Tmv));{释放记录移动步法内存}
 goto step2;
mexit:
 aiSearch:=success;
 aiCopyList(ju,sys);
 if not success then begin{如果没有得到满足要求的牌型,则返回费用值最低的牌型}
  for i:=iList-1 downto 0 do if list^[idxlist^[i]]^.isOpenList then break;
  if i<0 then i:=0;
  aiCopyList(ju,List^[idxList^[i]]^);
 end;
 aiCopyList(sys,oldju^);CopyPai;{恢复初始牌型}
 for i:=iList downto 0 do freemem(list^[i],sizeof(TPaiju));
 freemem(List,sizeof(SList));freemem(idxList,sizeof(TidxList));
 //new tlist method
 NodeList.pack;idxNodeList.pack;
 for i:=NodeList.Count-1 downto 0 do  freemem(NodeList.items[i],sizeof(TPaiju));
 for i:=idxNodeList.Count-1 downto 0 do  freemem(idxNodeList.items[i],sizeof(integer));
 NodeList.Clear;idxNodeList.clear;
 //new tlist method
 freemem(tmpju,sizeof(TPaiju));
 freemem(oldju,sizeof(TPaiju));{释放内存}
 huprogress.hide;
end;
function TFreecell.aiUndo:integer;
{undo 1 step in mv,一级Undo}
{用到的类内公共变量:sys}
var i,j,p,q,tmp,y:integer;
begin
if(sys.mv.i=0)then exit;
dec(sys.mv.i);{}
{获得移动步法中的确切由哪张牌操作的}
{p中存放目的牌,q中存放被移动的牌}
{分六种情况分别处理(多了一种人为的栈2到栈2的实际上是无用的操作)}
{注意,恢复操作与记录中的步法正好是反的}
p:=g(sys.mv.m[sys.mv.i]);q:=f(sys.mv.m[sys.mv.i]);
case s(sys.mv.m[sys.mv.i]) of
s3tos3:begin {do s3tos3}
 tmp:=FindLast(sys.st3[p][0]);{tmp=p}
 if(s(sys.st3[q][0])=s3)then begin
  j:=Pai[tmp].y;{j=py}
  for i:=sys.mv.n[sys.mv.i]-1 downto 0 do begin{i=qy}
   tmp:=sys.st3[p][j];
   sys.st3[q][i]:=tmp;
   sys.st3[p][j]:=s3+p;
   Pai[tmp].x:=q;Pai[tmp].y:=i;
   dec(j);
  end;
 end
 else begin
  j:=Pai[tmp].y;{j=py}
  tmp:=FindLast(sys.st3[q][0]);{tmp=q}
  y:=Pai[tmp].y;{y=qy}
  for i:=y+sys.mv.n[sys.mv.i] downto y+1 do begin{i=qy}
   tmp:=sys.st3[p][j];{tmp=py}
   sys.st3[q][i]:=tmp;
   sys.st3[p][j]:=s3+p;
   Pai[tmp].x:=q;Pai[tmp].y:=i;
   dec(j);
  end;{for}
 end;
 Draw(tmp);
end;{m=s3tos3}
s2tos3:begin{do s3tos2}
 p:=FindLast(sys.st3[p][0]);
 sys.st3[Pai[p].x][Pai[p].y]:=s3+Pai[p].x;
 sys.st2[q]:=p;
 Pai[p].x:=s2+q;
 Draw(p);
end;{m=s2tos3}
s2tos1:begin  {do s1 to s2}
 tmp:=sys.st1[p];
 if(f(sys.st1[p])=0){'A'}then sys.st1[p]:=s1+p
 else sys.st1[p]:=sys.st1[p]-1;
 sys.st2[q]:=tmp;
 Pai[tmp].x:=s2+q;
 Draw(tmp);
end;{m=s2tos1}
s3tos2:begin {do s2 to s3}
 tmp:=sys.st2[p];
 sys.st2[p]:=s2+p;
 if(s(sys.st3[q][0])=s3)then begin
  sys.st3[q][0]:=tmp;
  Pai[tmp].x:=q;Pai[tmp].y:=0;
 end
 else begin
  q:=FindLast(sys.st3[q][0]);
  sys.st3[Pai[q].x][Pai[q].y+1]:=tmp;
  Pai[tmp].x:=Pai[q].x;Pai[tmp].y:=Pai[q].y+1;
 end;
 Draw(tmp);
end;{m=s2tos3}
s2tos2:begin {do s2 to s2}
 tmp:=sys.st2[p];
 sys.st2[p]:=s2+p;
 sys.st2[q]:=tmp;
 Pai[tmp].x:=s2+q;
 Draw(tmp);
end;{m=s2tos2}
s3tos1:begin {do s1 to s3}
 tmp:=sys.st1[p];
 if(f(sys.st1[p])=0){'A'}then sys.st1[p]:=s1+p
 else sys.st1[p]:=sys.st1[p]-1;
 if(s(sys.st3[q][0])=s3)then begin
  sys.st3[q][0]:=tmp;
  Pai[tmp].x:=q;Pai[tmp].y:=0;
 end
 else begin
  q:=FindLast(sys.st3[q][0]);
  sys.st3[Pai[q].x][Pai[q].y+1]:=tmp;
  Pai[tmp].x:=Pai[q].x;Pai[tmp].y:=Pai[q].y+1;
 end;
 Draw(tmp);
end;{m=s3tos1}
end;{case sys.mv.m}
end;

function TFreecell.Chained(p,q:integer):boolean;{for stack3 p,q is pai}
{for stack3 判断牌P与牌q能否成链,p上q下}
{用到的类内公共变量:无}
begin
     Chained:=false;
     if f(p)<>(f(q)+1) then  exit;
     if((f(p)+f(q))mod 2)=0then begin {both even or odd}
      if((g(p)=0)or(g(p)=3))and(g(q)=1)or(g(q)=2)then exit;
      if((g(p)=2)or(g(p)=1))and(g(q)=0)or(g(q)=3)then exit;
     end
     else begin {diff even or odd}
      if((g(p)=0)or(g(p)=3))and((g(q)=0)or(g(q)=3))then  exit;
      if((g(p)=2)or(g(p)=1))and((g(q)=1)or(g(q)=2))then  exit;
     end;
     Chained:=true;
end;
function TFreecell.CountPai(stack:integer):integer;
{count pai in one of columes in stack3,计算栈3的一列中有几张牌}
{用到的类内公共变量:sys}
var i:integer;{count the total numbers of pai in  stack3}
begin{exclusive for stack3}
 if(stack<0)or(stack>7)then begin CountPai:=0;exit ;end;
 for i:=0 to 19 do begin
  if (sys.st3[stack][i]=s3+stack)then break;
 end;
 CountPai:=i;
end;
function TFreecell.CountFreeStack(stack:integer):integer;
{计算栈2中有几个空}
{用到的类内公共变量:sys}
var i,cnt:integer;
begin{}
 cnt:=0;
 case s(stack)of
 s3: begin{count stack3}
  for i:=0 to 7 do if sys.st3[i][0]<0 then inc(cnt);
 end;{stack=s3}
 s2: begin{count sys.st2}
  for i:= 0 to 3 do if sys.st2[i]>=s2 then inc(cnt);
 end;{stack=s2}
 s1: begin{count sys.st2}
  for i:= 0 to 3 do if sys.st1[i]>=s1 then inc(cnt);
 end;{stack=s1}
 end;{case stack}
 CountFreeStack:=cnt;
end;
function TFreecell.CountMaxChain(stack:integer):integer;{input stack3 slot number}
{计算栈3的一列中有几张牌构成顺}
{用到的类内公共变量:sys}
var i,num:integer;
begin{}
 if(stack<0)or(stack>7)then begin CountMaxChain:=0;exit ;end;
 num:=CountPai(stack);
 if num=1 then begin CountMaxChain:=1;exit;end;
 for i:=(num-1) downto 1 do
  if not Chained(sys.st3[stack][i-1],sys.st3[f(stack)][i])then begin
   CountMaxChain:=num-i;exit;
  end;
 CountMaxChain:=num;
end;
function TFreecell.Draw(p:integer):integer;
{for jiemian,画牌p以后的一列牌}
{用到的类内公共变量:sys,Pai}
var i,deadlock,startx,starty:integer;
begin
 if(selected=aiDo)then exit;
 if s(p)<>pk0 then begin Draw:=0;exit;end;
 i:= Pai[p].x ;
 case s(i) of
 s3: begin{stack3}
  startx:=s3x+f(i)*(gapx+pkx);starty:=s3y+Pai[p].y*gapy;
  end;
 s2:begin{sys.st2}
  startx:=s2x+f(i)*pkx;starty:=s2y;
  end;
 s1: begin {sys.st1}
  startx:=s1x+f(i)*pkx;starty:=s1y;
  end;
 pk0: begin
  startx:=s3x+Pai[p].x*(gapx+pkx);starty:=s3y+Pai[p].y*gapy;
  end;
 end;
 for deadlock:= Pai[p].y to 20 do begin {avoid deadlock}
   case s(p) of
   pk0: begin
    Pai[p].setbounds(startx,starty,pkx,pky);
    Pai[p].bringtofront;
    Pai[p].show;
    if s(Pai[p].x)<>pk0 then break;
    p:=sys.st3[Pai[p].x][Pai[p].y+1];
    starty:=starty+gapy;
   end;
   else begin    break;   end;
   end;{case p}
 end;{for}
end;
function TFreecell.f(p:integer):integer;
{标识与实际使用值换算}
{用到的类内公共变量:无}
begin
 case s(p) of
  pk0: f:=(p-pk0) mod 13;
  s3:  f:=p-s3;
  s1: f:=p -s1;
  s2:f:=p-s2;
  s2tos1:f:=(p-s2tos1)mod 10;
  s2tos3:f:=(p-s2tos3)mod 10;
  s3tos1:f:=(p-s3tos1)mod 10;
  s3tos2:f:=(p-s3tos2)mod 10;
  s3tos3:f:=(p-s3tos3)mod 10;
  s2tos2:f:=(p-s2tos2)mod 10;
 end;
end;
function TFreecell.FindFreeStack(stack:integer):integer;
{return stacknumber    返回栈2中的空的位置}
{用到的类内公共变量:sys}
var i,j:integer;
begin
 FindFreeStack:=none;
 case s(stack)of
 s3: begin{count stack3}
  for i:=0 to 7 do if sys.st3[i][0]<0 then begin FindFreeStack:=s3+i;break;end;
 end;{stack=s3}
 s2: begin{count sys.st2}
  for i:= 0 to 3 do if sys.st2[i]>=s2 then begin FindFreeStack:=s2+i;break;end;
 end;{stack=s2}
 s1: begin{count sys.st1}
  for i:= 0 to 3 do if sys.st2[i]>=s1 then begin FindFreeStack:=s1+i;break;end;
 end;{stack=s1}
 end;{case stack}
end;
function TFreecell.FindLast(p:integer):integer;
{返回栈3的第P列中最后一张牌}
{用到的类内公共变量:sys}
var i,tmp:integer;
begin
 FindLast:=none;
 case s(p)of
 s2:begin if s(sys.st2[f(p)])<>pk0 then exit else FindLast:=sys.st2[f(p)];end;
 s3:begin if s(sys.st3[f(p)][0])<>pk0 then exit else p:=sys.st3[f(p)][0];end;
 end;{case p}
 case s(Pai[p].x) of
 s1,s2:FindLast:=p;
 pk0:begin
  FindLast:=none;
  for i:=0 to 51 do begin{avoid deadlock}
  tmp:=sys.st3[Pai[p].x][Pai[p].y+1];
  if(tmp>=0)and(tmp<=52) then begin
     p:=tmp;end
  else begin
   FindLast:=p;exit;
  end;
  end;{end for}
 end;{p.x=pk0}
 end;{case p.x}
end;
function TFreecell.FindPai(p:integer;var x:integer;var y:integer;ju:TPaiju):integer;
{在ju之中找牌P,返回牌P的位置参数x,y}
{用到的类内公共变量:无}
var i,j:integer;
begin{find where p is,return sys.st3ition  in x,y}
 FindPai:=0;
 for i:=0 to 3 do begin
  if ju.st1[i]=p then begin x:=s1+i;FindPai:=-1;exit; end;
  if ju.st2[i]=p then begin x:=s2+i;FindPai:=-1;exit; end;
 end;
 for i:=0 to 7 do begin
 for j:=0 to 19 do begin
  if ju.st3[i][j]=p then begin x:=i;y:=j;FindPai:=-1 ;exit;end;
 end;{j}
 end;{i}
end;
function TFreecell.g(p:integer):integer;
{标识与实际使用值换算}
{用到的类内公共变量:无}
begin
 case s(p) of
  pk0:g:=(p-pk0) div 13;{return decimal}
  s3tos3:g:=(p-s3tos3)div 10;
  s2tos1:g:=(p-s2tos1)div 10;
  s2tos3:g:=(p-s2tos3)div 10;
  s3tos1:g:=(p-s3tos1)div 10;
  s3tos2:g:=(p-s3tos2)div 10;
  s2tos2:g:=(p-s2tos2)div 10;
 end;
end;
function TFreecell.Init:integer;
{设置初始牌局}
{用到的类内公共变量:sys,Pai[],InitPai[]}
var i,j:integer;
begin
 idxNodeList:=Tlist.create;
 NodeList:=Tlist.create;
 if(s(selected)=pk0)then Pai[selected].inverse;
 selected:=none;{freecell.cursor:=crDefault;}
 InitPai.mv.i:=0;
 for i:=0 to 3 do begin  InitPai.st1[i]:=s1+i;InitPai.st2[i]:=s2+i; end;
 for i:=4 to 7 do InitPai.st3[i][6]:=s3+i;
{get chain}
 for i:=0 to 7 do begin
 for j:=0 to 5 do begin
  Pai[InitPai.st3[i][j]].x:=i; Pai[InitPai.st3[i][j]].y:=j;
 end;{j}
 end;{i}
 for i:=0 to 3 do begin
  Pai[InitPai.st3[i][6]].x:=i; Pai[InitPai.st3[i][6]].y:=6;
 end;
 for i:=4 to 7 do InitPai.st3[i][6]:=s3+i;
 for i:=0 to 7 do begin
 for j:=7 to 20 do begin
  InitPai.st3[i][j]:=i+s3;
 end;{j}
 end;{i}
 for i:=0 to 7 do InitPai.st3[i][-1]:=i+s3;
 aiCopyList(sys,InitPai);CopyPai;
 sys.val:=aiEvaluate;
 InitPai.val:=sys.val;
 for i:=0 to 7 do begin
  Draw(sys.st3[i][0]);
 end;
 caption:=inttostr(sys.val);
end;
function TFreecell.Join(p,q:integer):integer;{supsys.st3e can be Joined}
{Join q to p把牌Q放在牌P下}
{用到的类内公共变量:sys,Pai[]}
var i,j,px,py,qx,qy,temp:integer;
   step,number:integer;{for undo}
begin
 if(s(q)<>pk0)then exit;{q一定要是牌}
 {两类复杂操作}
 if(s(p)=pk0)then begin{s3tos3 condition 1}
 {1:栈3的一列牌至栈3的另一非空列}
  temp:=f(p)-f(q);
  py:=Pai[p].y;px:=Pai[p].x;
  qx:=Pai[q].x;qy:=Pai[q].y;
  if(s(px)=pk0)and(s(qx)=pk0)then begin {special action}
   for j:=qy downto qy-temp+1 do begin
    i:=sys.st3[qx][j];
    sys.st3[px][py+temp-qy+j]:=i;
    Pai[i].x:=px;Pai[i].y:=py+temp-qy+j;
    sys.st3[qx][j]:=s3+qx;
   end;
   RememberStep(s3tos3+10*px+qx,temp);
   Draw(sys.st3[px][py+1]);exit;{end of special action1}
  end;
 end;
 if(s(p)=s3)and(s(Pai[q].x)=pk0){on s3}then begin {special action}
 {2:栈3的一列牌至栈3的另一空列}
  i:=(CountFreeStack(s2)+1)shl (CountFreeStack(s3)-1);
  j:=CountMaxChain(Pai[q].x);{s3tos3 condition 1}
  if i>j then temp:=j else temp:=i;{temp:=min(CountMaxChain(q),(m+1)*2^(N-1));}
  py:=0;px:=f(p);
  qx:=Pai[q].x;qy:=Pai[q].y;
  for j:=qy downto qy-temp+1 do begin
    i:=sys.st3[qx][j];
    sys.st3[px][py+temp-qy+j-1]:=i;
    Pai[i].x:=px;Pai[i].y:=py+temp-qy+j-1;
    sys.st3[qx][j]:=s3+qx;
  end;
  RememberStep(s3tos3+10*px+qx,temp);
  Draw(sys.st3[px][py]);exit;{end of special action1}
 end; {s3tos3,px=s3 or pk0,qx=pk0}
 {其他简单操作}
 {先记录移动步法}
 case s(Pai[q].x) of{判断牌q所在的栈}
 s2: begin {sys.st2}   {s2tos1,px=s1,qx=s2}
  if(s(p)=s3)then step:=s2tos3+f(Pai[q].x)+10*f(p)
  else if(s(p)=pk0)and(s(Pai[p].x)=pk0)then step:=s2tos3+f(Pai[q].x)+10*Pai[p].x
  else if(s(p)=s2)then step:=s2tos2+f(Pai[q].x)+10*f(p){s2tos2}
  else if(s(p)=s1)then step:=s2tos1+f(Pai[q].x)+10*f(p)
  else step:=s2tos1+f(Pai[q].x)+10*g(p); {s2tos3,px=pk0,qx=s2}
 end;{q.x=2}
 pk0: begin{stack3,simple condition}
  if(s(p)=s1)then step:=s3tos1+Pai[q].x+10*f(p){s3tos1,px=s1,qx=pk0}
  else if(s(p)=s2)then  step:=s3tos2+Pai[q].x+10*f(p) {s3tos2,px=s2,qx=pk0}
  else if(s(p)=pk0)and(s(Pai[p].x)=s1)then step:=s3tos1+Pai[q].x+10*g(p);
 end;{q.x=s3}
 end;{case q.x}
 RememberStep(step,1);
 {移动操作}
 case s(Pai[q].x) of
 s1:exit;
 s2: begin {sys.st2}
  sys.st2[f(Pai[q].x)]:=Pai[q].x;
 end;{q.x=2}
 pk0: begin{stack3,simple condition}
  if(s(p)<>pk0)or(s(Pai[p].x)<>pk0)then sys.st3[Pai[q].x][Pai[q].y]:=s3+Pai[q].x;
 end;{q.x=s3}
 end;{case q.x}
 case s(p) of
 s3:begin sys.st3[f(p)][0]:=q;Pai[q].x:=f(p);Pai[q].y:=0;end;
 pk0: begin{pai}
   if s(Pai[p].x)=s1 then begin
    sys.st1[f(Pai[p].x)]:=q;Pai[q].x:=Pai[p].x;
   end
   else begin{stack3}
    if(s(Pai[p].x)=pk0)then begin
     sys.st3[Pai[p].x][Pai[p].y+1]:=q;
     Pai[q].x:=Pai[p].x;Pai[q].y:=Pai[p].y+1;
    end;
   end;
 end;{p=s3}
 s1: begin {stack 1 ,2}
   sys.st1[f(p)]:=q; {sys.st1}
   Pai[q].x:=p;
 end;{P=S1}
 s2:begin
   sys.st2[f(p)]:=q; {sys.st2}
   Pai[q].x:=p;
 end;{p=s2}
 end;{case p}
 Draw(q);
 sys.val:=aiEvaluate;
end;
function TFreecell.s(p:integer):integer;
{标识状态判断}
{用到的类内公共变量:无}
begin
 if(p>=s3)and(p<=(s3+7))then begin s:=s3;exit;end;
 if(p>=pk0)and(p<=(pk0+51))then begin s:=pk0;exit;end;
 if(p>=s1)and(p<=(s1+3))then begin s:=s1;exit;end;
 if(p>=s2)and(p<=(s2+3))then begin s:=s2;exit;end;
 if(p>=s2tos1)and(p<=(s2tos1+99))then begin s:=s2tos1;exit;end;
 if(p>=s2tos3)and(p<=(s2tos3+99))then begin s:=s2tos3;exit;end;
 if(p>=s3tos1)and(p<=(s3tos1+99))then begin s:=s3tos1;exit;end;
 if(p>=s3tos2)and(p<=(s3tos2+99))then begin s:=s3tos2;exit;end;
 if(p>=s3tos3)and(p<=(s3tos3+99))then begin s:=s3tos3;exit;end;
 s:=none;
end;
function TFreecell.ReDraw:integer;
{accouding to sys,根据sys记录重画牌局}
{用到的类内公共变量:sys}
var i,j:integer;
begin
 for i:=0 to 3 do begin
  if s(sys.st1[i])<>pk0 then continue;
  for j:=13*g(sys.st1[i]) to sys.st1[i] do begin
   Draw(j);
  end;
 end;
 for i:=0 to 3 do begin
  if s(sys.st2[i])<>pk0 then continue;
  Draw(sys.st2[i]);
 end;
 for i:=0 to 7 do begin
  if s(sys.st3[i][0])=pk0 then Draw(sys.st3[i][0]);
 end;{i}
end;
function TFreecell.RememberStep(step,number:integer):integer;
{记录牌的移动}
{用到的类内公共变量:sys}
begin
 if sys.mv.i>=UndoLevel then begin
  showmessage('Steps overflow');exit;{for undo}
 end;
 {if s(q)<>pk0 then exit;}
 sys.mv.m[sys.mv.i]:=step;sys.mv.n[sys.mv.i]:=number;inc(sys.mv.i);
end;
function TFreecell.Rule(p,q:integer):boolean;
{判断牌q能否放在牌P下}
{用到的类内公共变量:sys}
var i,j:integer;
begin
 Rule:=false;{default Rule:=false;}
 if s(q)<>pk0 then exit;{首先q一定要是牌}
 case s(p) of{case p,判断p的属性}
 s1:begin
  if(g(q)=f(p))and(f(q)=0){"A"}then Rule:=true;
 end;{p=s1}
 s2,s3:begin{p一定是空列,因为设置如此} Rule:=true; end;{p=s1,s2,s3}
 pk0:begin{如果p是牌,判断牌所在的栈}
   case s(Pai[p].x) of {case p.x}
   s1:begin {Rule sys.st1}
    if(g(q)=g(p))and(f(q)=f(p)+1)then Rule:=true;
   end;{p.x=s1}
   s2:begin exit;   end;
   pk0:begin {Rule stack3,牌在栈3}
     case s(Pai[q].x) of{where is q,判断牌q所在的栈}
     s1 : exit;
     s2 : begin if Chained(p,q) then Rule:=true else exit;end;{q.x=s2}
     pk0:begin{in stack3,牌q在栈3,规则最复杂处}
     {1:p的牌点比q大}
      if f(p)<=f(q)then  exit;
      {2:pq相互连接的牌一定要红黑相间}
      if((f(p)+f(q))mod 2)=0then begin {both even or odd}
       if((g(p)=0)or(g(p)=3))and((g(q)=1)or(g(q)=2))then exit;
       if((g(p)=2)or(g(p)=1))and((g(q)=0)or(g(q)=3))then exit;
      end
      else begin {diff even or odd}
       if((g(p)=0)or(g(p)=3))and((g(q)=0)or(g(q)=3))then  exit;
       if((g(p)=2)or(g(p)=1))and((g(q)=1)or(g(q)=2))then  exit;
      end;
      {3:要有足够的空供移动,满足(m+1)*(2^n)>pq的点数差}
      {m:栈2空数}
      {n:栈3空数}
      if(CountMaxChain(Pai[q].x)+f(q))<f(p) then exit;
      if s(Pai[q].x)=s2 then begin Rule:=true;exit;end
      else begin
        i:=CountFreeStack(s3);
        i:=(CountFreeStack(s2)+1)shl i;
       if i>=(f(p)-f(q))then Rule:=true;
      end;
     end;{q.x=s3}
     end;{case q.x}
   end;{p.x =s3}
   end;{case p.x}
  end;{p=pk0}
  end;{case p}
end;

procedure TFreecell.pkClick(Sender: TObject);
{用到的类内公共变量:selected,Pai[]}
var i,p:integer;
begin
 if timer1.enabled then exit;
 p:=(sender as tpai).tag;
 if (selected=none)then begin{judge put or pick}
 {如果以前没有反显的牌,则这次点中的牌反显}
  if(s(Pai[p].x)<>s1)then begin
   selected:=FindLast(p);{freecell.cursor:=crDrag;}Pai[selected].inverse;
{ inverse Draw(selected);}
  end;
  exit;
 end;
 i:=FindLast(p);
 {如果以前点中的牌与这次点中的牌相同,则那张牌再次反显至正常状态}
 if selected=i then begin
  {inverse to normal }
   Pai[selected].inverse;selected:=none;{freecell.cursor:=crDefault;}
   for i:=0 to 51 do if aiFlyPai =none then break;
   exit;
 end;
 if Rule(i,selected)then begin
   Join(i,selected);
   Pai[selected].inverse;selected:=none;{freecell.cursor:=crDefault;}
   for i:=0 to 51 do if  aiFlyPai =none then break;
 end;
end;

procedure TFreecell.FormMouseDown(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
{用到的类内公共变量:selected,Pai[]}
begin
 if timer1.enabled then exit;
 if selected=none then exit;
 if(y>s3y)and(x>s3x)and(x<(s3x+(gapx+pkx)*8)) then x:=s3+(x-s3x)div(gapx+pkx){stack3}
 else if(y>s1y)and(y<(s1y+pky))and(x>s1x)and(x<(s1x+4*pkx))then x:=s1+(x-s1x)div pkx{sys.st1}
 else if(y>s2y)and(y<(s2y+pky))and(x>s2x)and(x<(s2x+4*pkx))then x:=s2+(x-s2x)div pkx{sys.st2}
 else begin
  Pai[selected].inverse;{误操作取消以前点中的牌}
  selected:=none;{freecell.cursor:=crDefault;}
  exit;{process selected}
 end;
 {如果X代表的列在栈3并且那以列有牌,则取消以前点中的牌}
 if(s(x)=s3)and(s(sys.st3[f(x)][0])<>s3)then begin
  Pai[selected].inverse;
  selected:=none;{freecell.cursor:=crDefault;}
  exit;
 end;{exclude mouse in gapx}
 if not Rule(x,selected) then exit;
 Join(x,selected);
 Pai[selected].inverse;selected:=none;{freecell.cursor:=crDefault;}
end;

procedure TFreecell.FormCreate(Sender: TObject);
{用到的类内公共变量:selected,Pai[]}
var i,j:integer;
        ttt:TPai;
begin
 for i:= 0 to 51 do begin
  Pai[i]:=TPai.create(self);
  Pai[i].parent:=self;
  {Pai[i].autosize:=true;}
  Pai[i].width:=pkx;Pai[i].height:=pky;
  Pai[i].onclick:=pk.onclick;
  Pai[i].onmousedown:=pkmousedown;
  Pai[i].ondblclick:=pkdblclick;
  Pai[i].tag:=i;
  Pai[i].canvas.copyrect(rect(0,0,pkx,pky),pk.canvas,
       rect(f(i)*pkx,g(i)*pky,(f(i)+1)*pkx,(g(i)+1)*pky));
 end;
 for i:=0 to 7 do  for j:=0 to 6 do InitPai.st3[i][j]:=i+j*8;
 selected:=none;
 Init;
end;

procedure TFreecell.pkDblClick(Sender: TObject);
{用到的类内公共变量:selected,Pai[]}
var i,p:integer;
begin
 if timer1.enabled then exit;
 if selected<>none then Pai[selected].inverse;{取消点中牌的操作}
 selected:=none;
 p:=(sender as Tpai).tag;
 {双击操作,如果栈2有空则被点中的那一列牌的最后一张牌移入}
 if(s(Pai[p].x)=pk0){instack3} and(CountFreeStack(s2)<>0)then begin
   i:=FindFreeStack(s2);
   Join(i,FindLast(p));
 end;
 for i:=0 to 51 do  if aiFlyPai =none then break;
end;

procedure TFreecell.mSave1Click(Sender: TObject);
{用到的类内公共变量:selected,Pai[],sys}
var i,j:integer;
 f:File of TPaiJu;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 if selected<>none then Pai[selected].inverse;{取消点中牌的操作}
 selected:=none;
 if dlgSave1.execute then begin
  assignfile(f,dlgSave1.filename);
  rewrite(f);
  write(f,sys);
  closefile(f);
 end;
end;

procedure TFreecell.mUndo1Click(Sender: TObject);
{用到的类内公共变量:selected,Pai[]}
begin
  if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 if selected<>none then Pai[selected].inverse;{取消点中牌的操作}
 selected:=none;
 aiUndo;
end;
procedure TFreecell.pkMouseDown(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
{鼠标右键点取}
{用到的类内公共变量:selected,Pai[]}
var p:integer;
    m:Tmsg;
begin
 if timer1.enabled then exit;
 if button<>mbRight then exit;
 if(selected<>none)then Pai[selected].inverse;{取消点中牌的操作}
 selected:=none;
 if s((sender as TPai).x)<>pk0 then exit;
 (sender as TPai).bringtofront;
 (sender as TPai).repaint;
 p:=(sender as TPai).tag;
 while (m.message<>WM_RBUTTONUP)and(m.message<>WM_MOUSEMOVE)
 	do GetMessage(m,handle,0,65535);{api}
 p:=sys.st3[Pai[p].x][Pai[p].y+1];
 if s(p)=pk0 then Draw(p);
end;

procedure TFreecell.mAiClick(Sender: TObject);
var i,j,p,q:integer;
    tmpju:^TPaiju;
    msg:Tmsg;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 if(selected<>none)then Pai[selected].inverse;{取消点中牌的操作}
 sys.val:=aiEvaluate;
 selected:=aiDo;{屏蔽界面显示}
 screen.cursor:=crHourglass;
 getmem(tmpju,sizeof(TPaiju));
 aiSearch(tmpju^);
 Newmv:=tmpju^.mv.i;{目的牌型步数存放在sys.mv之中}
 for i:=sys.mv.i to Newmv-1 do begin
  sys.mv.m[i]:=tmpju^.mv.m[i];
  sys.mv.n[i]:=tmpju^.mv.n[i];
 end;
 freemem(tmpju,sizeof(TPaiju));
 Timer1.enabled:=true;{启动时钟进行自动移动牌}
 selected:=none;screen.cursor:=crDefault;
end;

procedure TFreecell.mDealClick(Sender: TObject);
{随机发牌}
{用到的类内公共变量:InitPai[]}
var i,j,p:integer;
   tmp:array [0..51]of boolean;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 for i:=0 to 51 do tmp[i]:=false;
{ for i:=0 to 7 do  for j:=0 to 6 do InitPai[i][j]:=i+j*8;}
 randomize;
 for j:=0 to 5 do  for i:=0 to 7 do begin
  p:=random(52);
  while tmp[p] do p:=random(52);
  tmp[p]:=true;InitPai.st3[i][j]:=p;
 end;
 for i:=0 to 3 do for j:=0 to 51 do begin
  if tmp[j] then continue;
  InitPai.st3[i][6]:=j;tmp[j]:=true;
  break;
 end;
 Init;
end;

procedure TFreecell.mPai19411Click(Sender: TObject);
{用到的类内公共变量:InitPai[]}
var i,j:integer;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 for i:=0 to 7 do  for j:=0 to 6 do InitPai.st3[i][j]:=p1941[i][j];
 Init;
end;

procedure TFreecell.mPai191Click(Sender: TObject);
{用到的类内公共变量:InitPai[]}
var i,j:integer;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 for i:=0 to 7 do  for j:=0 to 6 do InitPai.st3[i][j]:=p19[i][j];
 Init;
end;

procedure TFreecell.mParameter1Click(Sender: TObject);
begin
if Timer1.enabled then Timer1.enabled:=false;
hu_input.showmodal;
end;

procedure TFreecell.mSunfree1Click(Sender: TObject);
{用到的类内公共变量:InitPai[]}
var i,j:integer;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 for i:=0 to 7 do  for j:=0 to 6 do InitPai.st3[i][j]:=i+j*8;
 Init;
end;

procedure TFreecell.mOpen1Click(Sender: TObject);
{用到的类内公共变量:selected,Pai[],sys,InitPai[]}
var  f:file of Tpaiju;  i,j:integer;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 if selected<>none then Pai[selected].inverse;{取消点中牌的操作}
 selected:=none;
 if dlgOpen1.execute then begin
   assignfile(f,dlgOpen1.filename);
   reset(f);
   read(f,InitPai);
   closefile(f);
   aiCopyList(sys,InitPai); CopyPai;
   ReDraw;
 end;
end;

procedure TFreecell.mReplay1Click(Sender: TObject);
{用到的类内公共变量:sys,InitPai[]}
var pseudo:tobject;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
  aiCopyList(sys,InitPai); CopyPai;
  ReDraw;
end;

procedure TFreecell.FormPaint(Sender: TObject);
{画栈1栈2的界面}
{用到的类内公共变量:无}
var i,j,x,y:integer;
begin
 for j:=0 to 3 do begin
  canvas.moveto(s1x+j*pkx,s1y+pky-1);
  canvas.pen.color:=clblack;
  canvas.lineto(s1x+j*pkx,s1y);
  canvas.lineto(s1x+(j+1)*pkx-1,s1y);
  canvas.pen.color:=cllime ;
  canvas.lineto(s1x+(j+1)*pkx-1,s1y+pky-1);
  canvas.lineto(s1x+j*pkx,s1y+pky-1);
 end;
 for j:=0 to 3 do begin
  canvas.moveto(s2x+j*pkx,s2y+pky-1);
  canvas.pen.color:=clblack;
  canvas.lineto(s2x+j*pkx,s2y);
  canvas.lineto(s2x+(j+1)*pkx-1,s2y);
  canvas.pen.color:=cllime ;
  canvas.lineto(s2x+(j+1)*pkx-1,s2y+pky-1);
  canvas.lineto(s2x+j*pkx,s2y+pky-1);
 end;
end;

procedure TFreecell.imgKingMouseDown(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
{用到的类内公共变量:无}
var src,des:Trect;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
{显示按下状态按钮}
 des:=rect(0,0,38,38);src:=rect(38*2,0,38*3,38);
 imgKing.canvas.copyrect(des,imgKing.canvas,src);
end;

procedure TFreecell.imgKingMouseUp(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
{用到的类内公共变量:无}
var src,des:Trect;
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 {显示弹起状态按钮}
 des:=rect(0,0,38,38);src:=rect(38,0,38*2,38);
 imgKing.canvas.copyrect(des,imgKing.canvas,src);
end;

procedure TFreecell.Timer1Timer(Sender: TObject);
{如果键盘一直被按着则自动移动牌}
{用到的类内公共变量:Pai[],sys}
var i,j,p,q:integer;
    msg:Tmsg;
begin
 try
  i:=sys.mv.i;
  if i=NewMv then begin
   timer1.enabled:=false;
   exit;
  end;
  p:=g(sys.mv.m[i]);
  q:=f(sys.mv.m[i]);
  case s(sys.mv.m[i]) of
  s3tos3:begin {do s3tos3}
    p:=sys.st3[p][0];
    if s(p)=pk0 then p:=FindLast(p);
    q:=FindLast(sys.st3[q][0]);
  end;{m=s3tos3}
  s2tos3:begin
   p:=sys.st3[p][0];
   if s(p)=pk0 then p:=FindLast(p);
   q:=sys.st2[q];
  end;{m=s2tos3}
  s2tos1:begin
   p:=sys.st1[p];
   q:=sys.st2[q];
  end;{m=s2tos1}
  s3tos2:begin
   p:=sys.st2[p];
   q:=FindLast(sys.st3[q][0]);
  end;{m=s2tos3}
  s2tos2:begin {no use for ai}
   p:=sys.st2[p];
   q:=sys.st2[q];
  end;{m=s2tos2}
  s3tos1:begin {do s1 to s3}
   p:=sys.st1[p];
   q:=FindLast(sys.st3[q][0]);
  end;{m=s3tos1}
  end;{case sys.mv.m}
  Pai[q].inverse;Pai[q].repaint;
  Join(p,q);{inc(sys.mv.i);have done}
  getmessage(msg,0,WM_KEYDOWN,WM_KEYDOWN);
  sys.val:=aiEvaluate;
  caption:=inttostr(sys.val);
  Pai[q].inverse;Pai[q].repaint;
 except
  if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 end;
end;

procedure TFreecell.mHelp1Click(Sender: TObject);
{用到的类内公共变量:无}
begin
 if Timer1.enabled then Timer1.enabled:=false;{关闭时钟}
 application.helpcontext(0);
end;

procedure TPai.inverse;
{牌反显}
begin
 canvas.copymode:= cmDstInvert;
 canvas.copyrect(canvas.cliprect,canvas,canvas.cliprect);
end;
end.
