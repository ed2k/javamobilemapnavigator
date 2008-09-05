unit Hup2;{胡弛峰为孙寅定制Gauges Form, used in aiSearch 显示搜索扩展节点数}

interface

uses
  SysUtils, WinTypes, WinProcs, Messages, Classes, Graphics, Controls,
  Forms, Dialogs, Gauges, ExtCtrls, StdCtrls;

type
  Thuprogress = class(TForm)
    Gauge1: TGauge;
    Bevel1: TBevel;
  private
    { Private declarations }
  public
    { Public declarations }
  end;

var
  huprogress: Thuprogress;

implementation

{$R *.DFM}

end.
