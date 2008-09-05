program S1941;

uses
  Forms,
  Sfree10 in 'SFREE10.PAS' {Freecell},
  Hu1 in 'HU1.PAS' {hu_input},
  Hup2 in 'HUP2.PAS' {huprogress};

{$R *.RES}

begin
  Application.HelpFile := 'sfree.hlp';
  Application.CreateForm(TFreecell, Freecell);
  Application.CreateForm(Thu_input, hu_input);
  Application.CreateForm(Thuprogress, huprogress);
  Application.Run;
end.
