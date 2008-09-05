NAME=testHDC
MASMROOT=/cygdrive/d/masm32
ML=$(MASMROOT)/bin/ml
LINK=$(MASMROOT)/BIN/LINK.EXE
RC=windres
GCC=g++

OBJECTS= test

#$(NAME).exe: $(NAME).o $(NAME).res
#	$(GCC)  -mwindows  $(NAME).o $(NAME).res -lgdi32 
#$(NAME).o: $(NAME).cc 
#	$(GCC) -c -o $(NAME).o $(NAME).cc 

#$(NAME).res:$(NAME).rc
#	$(RC) $< -O coff -o $@

a.exe: testHDC.o charactermap.o
	$(GCC) testHDC.o charactermap.o -lgdi32

testHDC.o : testHDC.cc
	$(GCC) -c -o $@ testHDC.cc

charactermap.o: charactermap.cc charactermap.h
	$(GCC) -c -o $@ charactermap.cc
