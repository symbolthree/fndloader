; $Header: /TOOL/FNDLOADER_V4/build_template/FNDLOADER_client.iss 2     2/09/17 8:49a Christopher Ho $
; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{FD1CF012-7D92-4FD9-A11F-AB07872D00B9}
AppName=FNDLOADER  @build.version@
AppVerName=FNDLOADER @build.version@
AppPublisher=symbolthree.com
AppPublisherURL=http://www.symbolthree.com
AppSupportURL=http://www.symbolthree.com
AppUpdatesURL=http://www.symbolthree.com
AppCopyright=Copyright (C) Christopher Ho @ symbolthree.com
DefaultDirName=C:/FNDLOADER4
DefaultGroupName=symbolthree\FNDLOADER
AllowNoIcons=false
LicenseFile=D:\WORK\FNDLOADER4\build_template\LICENSE.txt
OutputBaseFilename=FNDLOADER_@build.version@.@build.number@
Compression=lzma
SolidCompression=true
SetupLogging=false
OutputDir=D:\WORK\FNDLOADER4\setup
WizardImageFile=D:\WORK\FNDLOADER4\build_template\installer.bmp
SetupIconFile=
WizardImageStretch=false
WizardImageBackColor=clWhite
AppMutex=FNDLOADER @build.version@ Setup
UninstallDisplayIcon={app}\FNDLOADER.ICO
PrivilegesRequired=none
VersionInfoTextVersion=@build.version@ build @build.number@
VersionInfoVersion=@build.version@.@build.number@.0

[Languages]
Name: english; MessagesFile: compiler:Default.isl
;Name: chineseTrad; MessagesFile: compiler:Languages\ChineseTrad.isl
;Name: chineseSimp; MessagesFile: compiler:Languages\chineseSimp.isl
;Name: Japanese; MessagesFile: compiler:Languages\Japanese.isl
;Name: French; MessagesFile: compiler:Languages\French.isl
;Name: Russian; MessagesFile: compiler:Languages\Russian.isl
;Name: German; MessagesFile: compiler:Languages\German.isl
;Name: Italian; MessagesFile: compiler:Languages\Italian.isl
;Name: Spanish; MessagesFile: compiler:Languages\Spanish.isl
;Name: Portuguese; MessagesFile: compiler:Languages\Portuguese.isl

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}

[Files]
;Source: "FNDLOADER_@build.version@\log\*"; DestDir: "{app}\log"; Flags: recursesubdirs createallsubdirs
Source: "FNDLOADER_@build.version@\lib\*"; DestDir: "{app}\lib"; Flags: recursesubdirs createallsubdirs

Source: "FNDLOADER_@build.version@\CLIENT\client_files.txt"; DestDir: "{app}\CLIENT"

Source: "FNDLOADER_@build.version@\doc\*.*"; DestDir: "{app}\doc"; Flags: recursesubdirs createallsubdirs

;Source: "FNDLOADER_@build.version@\output\*"; DestDir: "{app}\output"; Flags: recursesubdirs createallsubdirs

;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\APPL_TOP\*.lct"; DestDir: "{app}\CLIENT\11.5.0\APPL_TOP"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\APPL_TOP\*.dll"; DestDir: "{app}\CLIENT\11.5.0\APPL_TOP"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\APPL_TOP\*.exe"; DestDir: "{app}\CLIENT\11.5.0\APPL_TOP"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\APPL_TOP\fnd\11.5.0\mesg\US.msb"; DestDir: "{app}\CLIENT\11.5.0\APPL_TOP\fnd\11.5.0\mesg"; Flags: recursesubdirs createallsubdirs

;Source: "FNDLOADER_@build.version@\CLIENT\12.0.0\APPL_TOP\*.lct"; DestDir: "{app}\CLIENT\12.0.0\APPL_TOP"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\12.0.0\APPL_TOP\*.dll"; DestDir: "{app}\CLIENT\12.0.0\APPL_TOP"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\12.0.0\APPL_TOP\*.exe"; DestDir: "{app}\CLIENT\12.0.0\APPL_TOP"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\12.0.0\APPL_TOP\fnd\12.0.0\mesg\US.msb"; DestDir: "{app}\CLIENT\12.0.0\APPL_TOP\fnd\12.0.0\mesg"; Flags: recursesubdirs createallsubdirs

;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\ORACLE_HOME\BIN\*.dll"; DestDir: "{app}\CLIENT\11.5.0\ORACLE_HOME\BIN"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\ORACLE_HOME\NLSRTL33\*.nlb"; DestDir: "{app}\CLIENT\11.5.0\ORACLE_HOME\NLSRTL33"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\ORACLE_HOME\NLSRTL33\*.msb"; DestDir: "{app}\CLIENT\11.5.0\ORACLE_HOME\NLSRTL33"; Flags: recursesubdirs createallsubdirs

;Source: "FNDLOADER_@build.version@\CLIENT\12.0.0\ORACLE_HOME\BIN\*.dll"; DestDir: "{app}\CLIENT\12.0.0\ORACLE_HOME\BIN"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\12.0.0\ORACLE_HOME\nls\*.nlb"; DestDir: "{app}\CLIENT\12.0.0\ORACLE_HOME\nls"; Flags: recursesubdirs createallsubdirs

;Source: "FNDLOADER_@build.version@\CLIENT\11.5.0\OA_HTML\*.xml"; DestDir: "{app}\CLIENT\11.5.0\OA_HTML"; Flags: recursesubdirs createallsubdirs
;Source: "FNDLOADER_@build.version@\CLIENT\12.0.0\OA_HTML\*.xml"; DestDir: "{app}\CLIENT\12.0.0\OA_HTML"; Flags: recursesubdirs createallsubdirs

Source: "FNDLOADER.exe"; DestDir: "{app}"
;Source: "FNDLOADER_CONSOLE.exe"; DestDir: "{app}"
Source: "build_template\FNDLOADER.ICO"; DestDir: "{app}"
;Source: "build_template\editProperties.bat"; DestDir: "{app}"
;Source: "build_template\editConfig.bat"; DestDir: "{app}"
;Source: "build_template\GPL.txt"; DestDir: "{app}"
;Source: "build_template\README"; DestDir: "{app}"
;Source: "build_template\LICENSE.txt"; DestDir: "{app}"
;Source: "build_template\LICENSE_3RD_PARTY.txt"; DestDir: "{app}"
Source: "build_template\splash.gif"; DestDir: "{app}"
;Source: "FNDLOADER_@build.version@\*.zip"; DestDir: "{app}"
Source: "FNDLOADER_@build.version@\FNDLOADER.bat"; DestDir: "{app}"
Source: "FNDLOADER_@build.version@\FNDLOADER_CONSOLE.bat"; DestDir: "{app}"
Source: "FNDLOADER_@build.version@\flower.properties"; DestDir: "{app}"
Source: "FNDLOADER_@build.version@\FNDLOADER.xml"; DestDir: "{app}"
Source: "FNDLOADER_@build.version@\FNDLOADER.dtd"; DestDir: "{app}"

[Icons]
Name: {group}\{cm:UninstallProgram,FNDLOADER}; Filename: {uninstallexe}; Tasks: ; Languages: 
Name: {group}\FNDLOADER @build.version@; Filename: {app}\FNDLOADER.EXE; WorkingDir: {app}; IconIndex: 0
Name: {group}\FNDLOADER @build.version@ (Console); Filename: {app}\FNDLOADER_CONSOLE.BAT; IconFilename: {app}\FNDLOADER.ico; IconIndex: 0; WorkingDir: {app}
;Name: {group}\Edit Properties; Filename: {app}\editProperties.bat; IconFilename: {app}\FNDLOADER.ICO; IconIndex: 0; WorkingDir: {app}; Languages: 
;Name: {group}\Edit Config File; Filename: {app}\editConfig.bat; IconFilename: {app}\FNDLOADER.ICO; IconIndex: 0; WorkingDir: {app}; Languages: 
Name: {commondesktop}\FNDLOADER @build.version@; Filename: {app}\FNDLOADER.EXE; Tasks: desktopicon; WorkingDir: {app}; IconIndex: 0

;[Run]
;Filename: {app}\FNDLOADER.EXE; Description: {cm:LaunchProgram,SYMPLiK FNDLOADER}; Flags: nowait postinstall skipifsilent
