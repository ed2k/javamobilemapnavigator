unit Hu1; {ºú³Ú·åÎªËïÒú¶¨ÖÆInput Form, used in Input Menu|parameters}

interface

uses WinTypes, WinProcs, Classes, Graphics, Forms, Controls, Buttons,
  StdCtrls,sysutils;
type
  Thu_input = class(TForm)
    OKBtn: TBitBtn;
    CancelBtn: TBitBtn;
    Label1: TLabel;
    L0: TEdit;
    Label2: TLabel;
    L1: TEdit;
    Label3: TLabel;
    L2: TEdit;
    Label4: TLabel;
    L3: TEdit;
    Label5: TLabel;
    L4: TEdit;
    Label6: TLabel;
    L5: TEdit;
    procedure OKBtnClick(Sender: TObject);
    procedure FormActivate(Sender: TObject);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

var
  hu_input: Thu_input;

implementation
uses   SFREE10;
{$R *.DFM}
procedure g;
begin
L0:=strtoint(hu_input.L0.text);
L1:=strtoint(hu_input.L1.text);
L2:=strtoint(hu_input.L2.text);
L3:=strtoint(hu_input.L3.text);
L4:=strtoint(hu_input.L4.text);
L5:=strtoint(hu_input.L5.text);
end;
procedure f;
begin
hu_input.L0.text:=inttostr(L0);
hu_input.L1.text:=inttostr(L1);
hu_input.L2.text:=inttostr(L2);
hu_input.L3.text:=inttostr(L3);
hu_input.L4.text:=inttostr(L4);
hu_input.L5.text:=inttostr(L5);
end;
procedure Thu_input.OKBtnClick(Sender: TObject);
begin
g;
end;

procedure Thu_input.FormActivate(Sender: TObject);
begin
f;
end;
end.
