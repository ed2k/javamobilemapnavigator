program Project1;

uses
  Forms,
  Unit1 in 'Unit1.pas' {Form1};
type
  TForm1 = class(TForm)
  private
    { Private declarations }
  public
    { Public declarations }
  end;
var
  Form1: TMineForm;

{$R *.RES}

begin
  Application.Initialize;
  Application.CreateForm(TMineForm, Form1);
  Application.Run;
end.
