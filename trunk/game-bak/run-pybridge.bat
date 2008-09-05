rem run this file to set up cygwin path, 
rem under win2k use nmake/cl/link from VS8 not 2003 server SDK, use which <cmd> to test
rem use dos2unix win32-setup.sh win32-setup.sh fix $'\r' problem
rem put cygwin path after sdk to avoid link.exe shadowing
rem sometime <xxx> is not recognized program due to slow pipe???
rem use full path "c:/.../xcopy" for xcopy in if statement of Makefile.nmake
rem set pyb=C:\Documents and Settings\lmcyisu\My Documents\todo\game\pybridge-0.3.0

rem Set Path=%path%;%pyb%
cd C:\Documents and Settings\lmcyisu\My Documents\todo\game\pybridge-0.3.0

c:\Python25\python.exe c:\sy\test\pybridge\trunk\pybridge\bin\pybridge