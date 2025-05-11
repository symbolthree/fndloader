@echo off
REM ############################################
REM # FNDLOADER Batch File (Client Version)
REM # $Header: /TOOL/FNDLOADER_V4/build_template/FNDLOADER_CONSOLE.bat 1     2/09/17 8:49a Christopher Ho $
REM #############################################

setlocal
java ^
-DlogPath=%USERPROFILE%\symbolthree\fndloader\log ^
-Dlog4j.configurationFile=lib\log4j2.xml ^
-cp ^
.\lib\FNDLOADER-@build.version@.jar;^
.\lib\CALLA-1.3.jar;^
.\lib\log4j-api-2.7.jar;^
.\lib\log4j-core-2.7.jar;^
.\lib\jna-4.2.2.jar;^
.\lib\jna-platform-4.2.2.jar;^
.\lib\jaxen-1.1.6.jar;^
.\lib\jdom-2.0.6.jar;^
.\lib\commons-io-2.5.jar;^
.\lib\collections.zip;^
.\lib\fndext.jar;^
.\lib\netcfg.jar;^
.\lib\ojdbc6.jar;^
.\lib\share.jar;^
.\lib\uix2.jar;^
.\lib\xmlparserv2.jar;^
.\lib\oamdsdt.jar;^
.\lib\mdsrt.jar;^
.\lib\mdsdt.jar;^
.\lib\xdo.zip ^
symbolthree.flower.CALLA -display CONSOLE
endlocal
pause

