NAME=testGetWindow
MASMROOT=/cygdrive/c/masm32
ML=$(MASMROOT)/bin/ml
LINK=$(MASMROOT)/BIN/LINK.EXE
RC=windres
GCC=g++

$(NAME).exe: $(NAME).o $(NAME).res
	$(GCC)  -mwindows  $(NAME).o $(NAME).res 
$(NAME).o: $(NAME).cc 
	$(GCC) -c -o $(NAME).o $(NAME).cc
$(NAME).res:$(NAME).rc
	$(RC) $< -O coff -o $@
