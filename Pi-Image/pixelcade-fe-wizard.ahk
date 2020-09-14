;version 3.1.0
;#ErrorStdOut :?:
#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
#SingleInstance force
;#Warn  ; Enable warnings to assist with detecting common errors.
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.
; "C:\Program Files\AutoHotkey\autohotkey.exe" "$(FULL_CURRENT_PATH)" add this to notepad++ run menu to run this direct 

PixelcadeCOMPortPrompt =
(
* Connect Pixelcade to your PC using the included USB A-A cable now 
* From Device Manager, click Ports and then look for the Pixelcade COM Port Number
* On Windows 10, Pixelcade will display as 'USB Serial Device'
* On Windows 7, Pixelcade will display as 'IOIO-OTG' (separate driver install required for Windows 7)

Note If you change your Arcade Front End later or need to run this setup wizard again, launch 'Pixelcade Arcade Front End Setup Wizard' from your Pixelcade LED Marquee start menu folder

Find the COM port number and PLEASE ONLY ENTER THE NUMBER here (Ex. 5, 6, 7, etc.)
)

PixelcadeCOMPortError =
(
**** ERROR ****
Sorry you did not enter just the number, please run this program again
and only enter the number only, omitting the COM text
)

PixelcadeMatrixQuestion =
(
* Press Yes if you have the Pixelcade LED Marquee / sizes P3, P4, P5, or P6

* Press No if you have the Pixelcade Pinball Dot Matrix Display / size P2.5

)

IniRead, PixelcadeCOMPort, settings.ini, PIXELCADE SETTINGS, port

;let's validate the text COM or com is there
If InStr(PixelcadeCOMPort, "COM")
    COMText=true
Else
    COMText=false
	
if (PixelcadeCOMPort = "COM99" or PixelcadeCOMPort = "COM" or COMText="false") ; to do add check if COM text not there
{
   Run, devmgmt.msc
   
   InputBox, UserInputCOMPortNumber, Arcade Front End Setup Wizard for Pixelcade,%PixelcadeCOMPortPrompt% , ,640, 300,800,0
   if ErrorLevel {
       MsgBox,64, Error,Sorry Pixelcade won't work until the COM Port has been entered, please run this again
	   ExitApp
	}   
   else
    If UserInputCOMPortNumber is not digit
    { 
       MsgBox %PixelcadeCOMPortError%
       Gui, Show
       ExitApp
    }
    
    PixelcadeCOMPortNew = COM%UserInputCOMPortNumber%
    IniWrite, %PixelcadeCOMPortNew%, %A_SCRIPTDIR%\settings.ini, PIXELCADE SETTINGS, port 
    MsgBox 64, Pixelcade COM Port, %PixelcadeCOMPortNew% will be used for Pixelcade
    
    MsgBox, 36,Pixelcade Model Selection, %PixelcadeMatrixQuestion%
    IfMsgBox Yes
        IniWrite, 128x32, %A_SCRIPTDIR%\settings.ini, PIXELCADE SETTINGS, ledResolution 
    else
    	IniWrite, 128x32C2, %A_SCRIPTDIR%\settings.ini, PIXELCADE SETTINGS, ledResolution 
}

; Now let's find the ledblinky path which the installer wrote for us in the file: installpaths.ini
installpathINI = %A_SCRIPTDIR%\installpaths.ini

if FileExist(installpathINI) {
	IniRead, LEDBlinkyPath, %A_SCRIPTDIR%\installpaths.ini, paths, ledblinky-path
			
		If (LEDBlinkyPath="") { ;this means pixelcade installer didn't find LEDBlinky so let's have the user tell us where it is
		
				FileSelectFile, LEDBlinkyPath, 3, , *** LEDBlinky Installation Location Required *** Please find and select LEDBlinky.exe, EXE Documents (LEDBlinky.exe)
				if (LEDBlinkyPath = "") 
					MsgBox, 16,Error,You did not specifiy your LEDBlinky installation and will need to install and configure LEDBlinky manually using LEDBlinkyConfig.exe	
				else 
					SplitPath, LEDBlinkyPath,, LEDBlinkyDir
		} 
		
		else {   ;the pixelcade installer DID find LEDBlinky but let's first validate it's the correct one as the user could have multiple LEDBlinky installations
					
					MsgBox, 36, LEDBlinky Location Validation, Detected LEDBlinky installation at:`n`n %LEDBlinkyPath%`n`n Is this Correct?
					IfMsgBox Yes 
							SplitPath, LEDBlinkyPath,, LEDBlinkyDir
					else 
						{
							FileSelectFile, LEDBlinkyPath, 3, , *** LEDBlinky Installation Location Required *** Please find and select LEDBlinky.exe, EXE Documents (LEDBlinky.exe)
							if (LEDBlinkyPath = "") 
								MsgBox, 16,Error,You did not specifiy your LEDBlinky installation and will need to install and configure LEDBlinky manually using LEDBlinkyConfig.exe
								
							else 
								SplitPath, LEDBlinkyPath,, LEDBlinkyDir
						}
			}
	
	LEDBlinkySettingsPath = %LEDBlinkyDir%\Settings.ini
	if FileExist(LEDBlinkySettingsPath) {
		
		MsgBox, 36,Pixelcade and LEDBlinky, Are you only using the Pixelcade LED Marquee and not using LEDBlinky for LED Button Control also?
		    IfMsgBox Yes
		        IniWrite, 1, %LEDBlinkySettingsPath%, OtherSettings, NoLEDs 
		    else
    			IniWrite, 0, %LEDBlinkySettingsPath%, OtherSettings, NoLEDs 
		
		IniWrite, 1, %LEDBlinkySettingsPath%, Pixelcade, UsePixelcade ;turn on pixelcade
		IniWrite, %A_SCRIPTDIR%, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeFolder ;pixelcade path
		IniWrite, 1, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeFEScroll ; update on front end scrolling events
		IniWrite, Thanks for Playing, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeFEQuitText ; text to play when FE quits
		IniWrite, 2, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeFEScreenSaverImageAniUser ; play an animation during screen saver event
		;IniWrite, 0pacghosts.png, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeFEScreenSaverAniUserFile ;screen saver animation to use
		IniWrite, <Random>, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeFEScreenSaverAniUserFile ;screen saver animation to use random
		
		
		IniWrite, Welcome and Game On!, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeFEStartText ;startup text
		IniWrite, Now Playing ~game~, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeGameActiveText ; game tab
		IniWrite, 2, %LEDBlinkySettingsPath%, Pixelcade, PixelcadeScrollTextSpeed  ;text scrolling speed
		
		IniWrite, Main Menu`,Main_Menu`,Favorites, %LEDBlinkySettingsPath%, FEOptions, FESystemListsNames 
		IniWrite, 1, %LEDBlinkySettingsPath%, FEOptions, DemoGameControls 
		IniWrite, 1, %LEDBlinkySettingsPath%, FEOptions, HLActive
		IniWrite, 0, %LEDBlinkySettingsPath%, OtherSettings, DisplayErrorsOnExit  ;disable show errors on exit
		
		Gui, Add, Text,, Please select your Arcade Front End
		Gui, Add, DropdownList,vfrontend,LaunchBox or BigBox|HyperSpin|GameEx or GameEx Arcade Edition|CoinOPS|Maximus Arcade|PinballX|Mala|AtomicFE|Other Front End
		Gui,Add,Button,gOk wp,Ok
		Gui, Show,x500 y300 w300 h200,Arcade Front End
		return

		Ok:
		Gui,Submit ;Remove Nohide if you want the GUI to hide

		if (frontend = "LaunchBox or BigBox") 
		{
		   IniWrite, launchbox, %LEDBlinkySettingsPath%, FEOptions, FE
		   
			; now let's edit LaunchBox / BigBox settings xml and turn on LEDBlinky there
			IniRead, LaunchBoxExePath, %A_SCRIPTDIR%\installpaths.ini, paths, launchbox-path
			
			If (LaunchBoxExePath="") { ;this means pixelcade installer did not find LaunchBox so let's have the user manually tell us where it is with a file select box
			
					FileSelectFile, LaunchBoxExePath, 3, , *** LaunchBox / BigBox Installation Location Required *** Please find and select LaunchBox.exe, EXE Documents (LaunchBox.exe)
					if (LaunchBoxExePath = "") 
						MsgBox, 16,Error,You did not specifiy your LaunchBox/BigBox installation and will need to configure this manually
					else 
						SplitPath, LaunchBoxExePath,, LaunchBoxDirPath
					
			} 
			
			else {   ;the pixelcade installer DID find LaunchBox/BigBox but let's first validate it's the correct one as the user could have multiple installations
				
				MsgBox, 36,LaunchBox/BigBox Location Validation, Detected your LaunchBox / BigBox installation at:`n`n %LaunchBoxExePath%`n`n Is this Correct?
				IfMsgBox Yes 
						SplitPath, LaunchBoxExePath,, LaunchBoxDirPath
				else 
					{
						FileSelectFile, LaunchBoxExePath, 3, , *** LaunchBox / BigBox Installation Location Required *** Please find and select LaunchBox.exe, EXE Documents (LaunchBox.exe)
						if (LaunchBoxExePath = "") 
							MsgBox, 16, Error, You did not specifiy your LaunchBox/BigBox installation and will need to configure this manually	
						else 
							SplitPath, LaunchBoxExePath,, LaunchBoxDirPath
					}
			}
				
			;now that we know the path to LB/BB settings.xml, let's enable LEDBlinky there, we are using the XML class to do this at the end of this script
			LaunchBoxXMLSettingsPath = %LaunchBoxDirPath%\Data\Settings.xml
			if FileExist(LaunchBoxXMLSettingsPath) {
							LaunchBoxXML:=new XML("LaunchBox",LaunchBoxXMLSettingsPath) ;load the launchbox settings xml
							Node1:=LaunchBoxXML.Add("Settings/EnableLedBlinky",,"true")
							Node2:=LaunchBoxXML.Add("Settings/LedBlinkyPath",,LEDBlinkyPath)
							LaunchBoxXML.Save(1)
			}
			else {
				MsgBox, 16, Error, LaunchBox/BigBox settings.xml not found, was looking for:`n`n%LaunchBoxXMLSettingsPath%
			}
		}
		
		else if (frontend = "HyperSpin")
		{
		    IniWrite, hyperspin, %LEDBlinkySettingsPath%, FEOptions, FE
			; now let's edit HyperSpin settings and turn on LEDBlinky in HyperSpin
			IniRead, HyperSpinExePath, %A_SCRIPTDIR%\installpaths.ini, paths, hyperspin-path
			
			If (HyperSpinExePath="") { ;this means pixelcade installer DID NOT find hyperspin so let's have the user manually tell us where it is
			
					FileSelectFile, HyperSpinExePath, 3, , *** HyperSpin Installation Location Required *** Please find and select HyperSpin.exe, EXE Documents (HyperSpin.exe)
					if (HyperSpinExePath = "") 
						MsgBox, 16, Error,You did not specifiy your HyperSpin installation and will need to configure this manually using HyperHQ.exe
					else 
						SplitPath, HyperSpinExePath,, HyperSpinDirPath
				} 
				else {   ;the pixelcade installer DID find HyperSpin but let's first validate it's the correct one as the user could have multiple HyperSpin installations
					
					MsgBox, 36,HyperSpin Location Validation, Detected HyperSpin installation at:`n`n %HyperSpinExePath%`n`n Is this Correct?
					IfMsgBox Yes 
							SplitPath, HyperSpinExePath,, HyperSpinDirPath
					else 
						{
							FileSelectFile, HyperSpinExePath, 3, , *** HyperSpin Installation Location Required *** Please find and select HyperSpin.exe, EXE Documents (HyperSpin.exe)
							if (HyperSpinExePath = "") 
								MsgBox,16, Error, You did not specifiy your HyperSpin installation and will need to configure this manually using HyperHQ.exe
							else 
								SplitPath, HyperSpinExePath,, HyperSpinDirPath
						}
				}
				
			; now that we know HyperSpin's setting path, let's turn on LEDBlinky there
			HyperSpinSettingsPath = %HyperSpinDirPath%\Settings\Settings.ini
			if FileExist(HyperSpinSettingsPath) {
				IniWrite, true, %HyperSpinSettingsPath%, LEDBlinky, Active
				IniWrite, %LEDBlinkyDir%\, %HyperSpinSettingsPath%, LEDBlinky, Path
			}
			else {
				MsgBox, 16, Error,%HyperSpinSettingsPath% not found, you'll need to enable LEDBlinky in HyperHQ manually
			}
		}
		
		else if (frontend = "GameEx or GameEx Arcade Edition")
		{
		    IniWrite, gameex, %LEDBlinkySettingsPath%, FEOptions, FE
			IniRead, GameExExePath, %A_SCRIPTDIR%\installpaths.ini, paths, gamex-path
			
			If (GameExExePath="") { ;this means pixelcade installer DID NOT find gameex so let's have the user manually tell us where it is
				FileSelectFile, GameExExePath, 3, , *** GameEx Installation Location Required *** Please find and select GameEx.exe, EXE Documents (GameEx.exe)
				if (GameExExePath = "") 
					MsgBox, 16, Error,You did not specifiy your GameEx installation and will need to configure this manually in GameEx Setup Wizard	
				else 
					SplitPath, GameExExePath,, GameExDirPath
			}
			
			else {   ;the pixelcade installer DID find GameEx but let's first validate it's the correct one as the user could have multiple GameEx or GameEx Arcade installations
				
				MsgBox, 36,GameEx Location Validation, Detected GameEx installation at:`n`n %GameExExePath%`n`n Is this Correct?
				IfMsgBox Yes
						SplitPath, GameExExePath,, GameExDirPath
				else 
					{
						FileSelectFile, GameExExePath, 3, , *** GameEx Installation Location Required *** Please find and select GameEx.exe, EXE Documents (GameEx.exe)
						if (GameExExePath = "") 
							MsgBox, 16, Error,You did not specifiy your GameEx installation and will need to configure this manually using the GameEx Setup Wizard
							
						else 
							SplitPath, GameExExePath,, GameExDirPath
					}
			}
				
			GameExPluginsPath = %GameExDirPath%\PLUGINS\LEDBlinky
			GameExSettingsPath = %GameExDirPath%\PLUGINS\LEDBlinky\settings.ini
			
			if (LEDBlinkySettingsPath = GameExSettingsPath) {
				;we don't want to copy over settings.ini if the ledblinky selected is the same as the ledblinky in gameex
				MsgBox, 64, Enable LEDBlinky from GameEx Setup Wizard,IMPORTANT: You must now manually download and enable the LEDBlinky plug-in from the GameEx Setup Wizard which will launch after you click OK
			}
			else {
				if InStr(FileExist(GameExPluginsPath), "D") {    ;if the LEDBlinky folder is not there in Plugins, then we'll create it before we copy over settings.ini
					  ; !FileExist did not work :-(
				}
				else {
					try 
						FileCreateDir, %GameExPluginsPath%
					catch e 
						MsgBox, 16, Error,Unable to create %GameExPluginsPath%
				}	
				try  {
					FileCopy,%LEDBlinkyDir%\settings.ini, %GameExSettingsPath% , 1
					MsgBox, 64, LEDBlinky Settings Copied,%LEDBlinkyDir%\settings.ini has been copied to: %GameExSettingsPath%`n`nIMPORTANT: You must now manually download and enable the LEDBlinky plug-in from the GameEx Setup Wizard which will launch after you click OK
				}
				catch e 
					MsgBox, 16, Error, Could not copy LEDBlinky settings.ini to %GameExSettingsPath%.`n`nSorry you will need to run LEDBlinkyConfig.exe manually from %GameExPluginsPath%
			}
			
			GameExSetupWizardPath = %GameExDirPath%\SetupWizard.exe
			if FileExist(GameExSetupWizardPath) 
				Run SetupWizard.exe, %GameExDirPath% ;run the GameEx Setup Wizard
			else 
				MsgBox, %GameExDirPath%\SetupWizard.exe not found
		}
		
		else if (frontend = "Maximus Arcade")
		{
		    IniWrite, maximusarcade, %LEDBlinkySettingsPath%, FEOptions, FE
			MsgBox, 64, *** IMPORTANT ***, You must manually enable LEDBlinky from the Maximus Arcade setup utility after this program completes
		}
		
		else if (frontend = "PinballX")
		{
		    IniWrite, pinballx, %LEDBlinkySettingsPath%, FEOptions, FE
			MsgBox, 64, *** IMPORTANT ***, You must manually enable LEDBlinky from the PinballX setup utility after this program completes
		}
		
		else if (frontend = "Mala")
		{
		    IniWrite, mala, %LEDBlinkySettingsPath%, FEOptions, FE
			MsgBox, 64, *** IMPORTANT ***, You must manually enable LEDBlinky from the MaLa setup utility after this program completes
		}
		
		else if (frontend = "AtomicFE")
		{
		    IniWrite, atomicfe, %LEDBlinkySettingsPath%, FEOptions, FE
			MsgBox, 64, *** IMPORTANT ***, You must manually enable LEDBlinky from the AtomicFE setup utility after this program completes
		}
		
		else if (frontend = "CoinOPS")
		{
			
			IniRead, CoinOPSExePath, %A_SCRIPTDIR%\installpaths.ini, paths, coinops-path
			
			If (CoinOPSExePath="") { ;this means pixelcade installer didn't find coinops so let's have the user manually tell us where it is
			
				FileSelectFile, CoinOPSExePath, 3, , *** CoinOPS Installation Location Required *** Please find and select CoinOPS.exe, EXE Documents (*.exe)
				if (CoinOPSExePath = "") 
					MsgBox, 16, Error,You did not specifiy your CoinOPS installation and will need to manually copy %A_SCRIPTDIR%retrofe.exe to your CoinOPS/core folder
				else 
					SplitPath, CoinOPSExePath,, CoinOPSDirPath
			}	
			else {   ;the pixelcade installer did find CoinOPS but let's first validate it's the correct one as the user could have multiple CoinOPS installations
				
				MsgBox, 36,CoinOPS Location Validation, Detected CoinOPS installation at:`n`n %CoinOPSExePath%`n`n Is this Correct?
				IfMsgBox Yes 
						SplitPath, CoinOPSExePath,, CoinOPSDirPath
				else 
					{
						FileSelectFile, CoinOPSExePath, 3, , *** CoinOPS Installation Location Required *** Please find and select CoinOPS.exe or alternative CoinOPS executable, EXE Documents (*.exe)
						if (CoinOPSExePath = "") 
							MsgBox, 16, Error,You did not specifiy your CoinOPS installation and will need to manually copy %A_SCRIPTDIR%retrofe.exe to your CoinOPS/core folder
						else 
							SplitPath, CoinOPSExePath,, CoinOPSDirPath
					}
				}
				
			CoinOPSRetroFEEXEPath = %CoinOPSDirPath%\core\retrofe.exe
			CoinOPSRetroFEPath = %CoinOPSDirPath%\core
				
			if FileExist(CoinOPSRetroFEEXEPath) { ;let' make sure the target is there before we over-write
				FileCopy, %A_SCRIPTDIR%\retrofe.exe, %CoinOPSRetroFEPath% , 1
				MsgBox, 64,CoinOPS Modification for Pixelcade, A special version of %A_SCRIPTDIR%\retrofe.exe that integrates with Pixelcade has been copied to %CoinOPSRetroFEPath%\retrofe.exe		
			}
			else {
				MsgBox, 16, Error,%CoinOPSRetroFEEXEPath% was not found, you'll need to manually overwrite this with %A_SCRIPTDIR%\retrofe.exe
			}
			
			;Becasue we are using CoinOPS, we don't need to configure LEDBlinky and Mame so we can quit here
			
			if( InStr( FileExist("C:\DirectOutput"), "D") ) {  ; if this directory is there, then let's copy the pixelcade matrix plug-in
				FileCopy, %A_SCRIPTDIR%\PixelcadeMTXPlugin.dll, C:\DirectOutput , 1
				MsgBox, 64,PixelcadeMatrix Plug-In, %A_SCRIPTDIR%\PixelcadeMTXPlugin.dll that enables PixelcadeMatrix has been copied to C:\DirectOutput\PixelcadeMTXPlugin.dll
			}
			
			
			MsgBox, 64, All Done, Pixelcade configuration for %frontend% is complete`n`nGame On!
			ExitApp
		}
		
		
		else if (frontend = "Other Front End")
		{
		    IniWrite, other, %LEDBlinkySettingsPath%, FEOptions, FE
			MsgBox, 64, *** IMPORTANT ***, You must manually enable LEDBlinky from your Arcade Frontend setup utility after this program completes
		}
		
		;Now let's look for mame and set the cfg path
		
		IniRead, MAMEExePath, %A_SCRIPTDIR%\installpaths.ini, paths, mame-path
		
		If (MAMEExePath="") { ;this means pixelcade installer DID NOT find MAME so let's have the user manually tell us where it is
			
			FileSelectFile, MAMEExePath, 3, , *** MAME Installation Location Required *** Please find and select mame.exe or mame64.exe, EXE(mame.exe;mame64.exe)
			if (MAMEExePath = "") 
				MsgBox, 16, Error,You did not specifiy your MAME installation and will need to configure this manually using LEDBlinkyConfig.exe
			else 
				SplitPath, MAMEExePath,, MAMEDirPath
		} 
		
		else {   ;the pixelcade installer DID find MAME but let's first validate it's the correct one as the user could have multiple mame installations
			
			MsgBox, 36,MAME Location Validation, Detected MAME installation at:`n`n%MAMEExePath%`n`nIt's fine also if you are using MAME 64-bit (mame64.exe). We just want to confirm this is the correct MAME folder.`n`nIs this the correct MAME folder location?
		    IfMsgBox Yes 
					SplitPath, MAMEExePath,, MAMEDirPath
		    else 
				{
					FileSelectFile, MAMEExePath, 3, , *** MAME Installation Location Required *** Please find and select mame.exe or mame64.exe, EXE (mame.exe;mame64.exe) ;user can pick mame.exe or mame64.exe
					if (MAMEExePath = "") 
						MsgBox, 16, Error,You did not specifiy your MAME installation and will need to configure this manually using LEDBlinkyConfig.exe
					else 
						SplitPath, MAMEExePath,, MAMEDirPath
				}
		}
		
		;now that we know the Mame path, let's add the cfg dir to LEDBlinky
		MAMECfgPath = %MAMEDirPath%\cfg
		
		if FileExist(MAMECfgPath)  				;let' make sure the dir is there first
			IniWrite, %MAMECfgPath%, %LEDBlinkySettingsPath%, MAMEConfig, Mame_cfg_folder	
		else 
			MsgBox, %MAMECfgPath% directory was not found, you'll need to manually add this in the mame tab in LEDBlinkyConfig.exe
		
		; ok now that mame cfg is taken care of, let's see if the user has mame.xml and create it if not
		if FileExist(MAMEExePath) { 				; let's make sure mame.exe exists before we run the command
			MameXMLPath = %MAMEDirPath%\mame.xml
			if FileExist(MameXMLPath) 
				IniWrite, %MAMEDirPath%\mame.xml, %LEDBlinkySettingsPath%, MAMEConfig, MameXmlFile
			else {
			    SplitPath, MAMEExePath , mame32or64 ;added this as the user may pick mame.exe or mame64.exe
				;MsgBox, 64, GENERATING MAME.XML, mame.xml was not found so we will now generate it now using this command:`n`n %MAMEDirPath%\mame.exe -listxml > mame.xml `n`n After you click OK `, a command line box will pop up `, please let this run until it goes away automatically AFTER A FEW MINUTES and DO NOT close this box
				MsgBox, 64, GENERATING MAME.XML, mame.xml was not found so we will now generate it now using this command:`n`n %MAMEDirPath%\%mame32or64% -listxml > mame.xml `n`n After you click OK `, a command line box will pop up `, please let this run until it goes away automatically AFTER A FEW MINUTES and DO NOT close this box
				;RunWait cmd /c mame  -listxml > mame.xml, %MAMEDirPath%, Max				
				SplashTextOn , 400, 100, GENERATING MAME.XML // DO NOT CLOSE, Please do not close the DOS/command line window`n`nThis will take a few minutes and will close automatically`, Thanks!
				RunWait cmd /c %mame32or64% -listxml > mame.xml, %MAMEDirPath%
				SplashTextOff
				IniWrite, %MAMEDirPath%\mame.xml, %LEDBlinkySettingsPath%, MAMEConfig, MameXmlFile
			}
		}
		
		else 
			MsgBox, You did not specifiy your MAME installation and will need to configure this manually using LEDBlinkyConfig.exe
		
		
		if( InStr( FileExist("C:\DirectOutput"), "D") ) {  ; if this directory is there, then let's copy the pixelcade matrix plug-in
			FileCopy, %A_SCRIPTDIR%\PixelcadeMTXPlugin.dll, C:\DirectOutput , 1
			MsgBox, 64,PixelcadeMatrix Plug-In, %A_SCRIPTDIR%\PixelcadeMTXPlugin.dll that enables PixelcadeMatrix has been copied to C:\DirectOutput\PixelcadeMTXPlugin.dll
		}
		
		MsgBox, 64, All Done, Pixelcade configuration for %frontend% is complete`n`nGame On!
		ExitApp
		return
	}
	
}
else {
    MsgBox, 16,LED Blinky Not Found, Sorry, LEDBlinky was not found. You'll need to setup and configure LED Blinky manually.
}



; we use this class to edit XML
Class XML{
	keep:=[]
	__Get(x=""){
		return this.XML.xml
	}__New(param*){
		;temp.preserveWhiteSpace:=1
		root:=param.1,file:=param.2,file:=file?file:root ".xml",temp:=ComObjCreate("MSXML2.DOMDocument"),temp.SetProperty("SelectionLanguage","XPath"),this.xml:=temp,this.file:=file,XML.keep[root]:=this
		if(FileExist(file)){
			FileRead,info,%file%
			if(info=""){
				this.xml:=this.CreateElement(temp,root)
				FileDelete,%file%
			}else
				temp.LoadXML(info),this.xml:=temp
		}else
			this.xml:=this.CreateElement(temp,root)
	}Add(XPath,att:="",text:="",dup:=0){
		p:="/",add:=(next:=this.SSN("//" XPath))?1:0,last:=SubStr(XPath,InStr(XPath,"/",0,0)+1)
		if(!next.xml){
			next:=this.SSN("//*")
			for a,b in StrSplit(XPath,"/")
				p.="/" b,next:=(x:=this.SSN(p))?x:next.AppendChild(this.XML.CreateElement(b))
		}if(dup&&add)
			next:=next.ParentNode.AppendChild(this.XML.CreateElement(last))
		for a,b in att
			next.SetAttribute(a,b)
		if(text!="")
			next.text:=text
		return next
	}CreateElement(doc,root){
		return doc.AppendChild(this.XML.CreateElement(root)).ParentNode
	}EA(XPath,att:=""){
		list:=[]
		if(att)
			return XPath.NodeName?SSN(XPath,"@" att).text:this.SSN(XPath "/@" att).text
		nodes:=XPath.NodeName?XPath.SelectNodes("@*"):nodes:=this.SN(XPath "/@*")
		while(nn:=nodes.item[A_Index-1])
			list[nn.NodeName]:=nn.text
		return list
	}Find(info*){
		static last:=[]
		doc:=info.1.NodeName?info.1:this.xml
		if(info.1.NodeName)
			node:=info.2,find:=info.3,return:=info.4!=""?"SelectNodes":"SelectSingleNode",search:=info.4
		else
			node:=info.1,find:=info.2,return:=info.3!=""?"SelectNodes":"SelectSingleNode",search:=info.3
		if(InStr(info.2,"descendant"))
			last.1:=info.1,last.2:=info.2,last.3:=info.3,last.4:=info.4
		if(InStr(find,"'"))
			return doc[return](node "[.=concat('" RegExReplace(find,"'","'," Chr(34) "'" Chr(34) ",'") "')]/.." (search?"/" search:""))
		else
			return doc[return](node "[.='" find "']/.." (search?"/" search:""))
	}Get(XPath,Default){
		text:=this.SSN(XPath).text
		return text?text:Default
	}Language(Language:="XSLPattern"){
		this.XML.SetProperty("SelectionLanguage",Language)
	}ReCreate(XPath,new){
		rem:=this.SSN(XPath),rem.ParentNode.RemoveChild(rem),new:=this.Add(new)
		return new
	}Save(x*){
		if(x.1=1)
			this.Transform()
		if(this.XML.SelectSingleNode("*").xml="")
			return m("Errors happened while trying to save " this.file ". Reverting to old version of the XML")
		filename:=this.file?this.file:x.1.1,ff:=FileOpen(filename,0),text:=ff.Read(ff.length),ff.Close()
		if(!this[])
			return m("Error saving the " this.file " XML.  Please get in touch with maestrith if this happens often")
		if(text!=this[])
			file:=FileOpen(filename,"rw"),file.Seek(0),file.Write(this[]),file.Length(file.Position)
	}SSN(XPath){
		return this.XML.SelectSingleNode(XPath)
	}SN(XPath){
		return this.XML.SelectNodes(XPath)
	}Transform(){
		static
		if(!IsObject(xsl))
			xsl:=ComObjCreate("MSXML2.DOMDocument"),xsl.loadXML("<xsl:stylesheet version=""1.0"" xmlns:xsl=""http://www.w3.org/1999/XSL/Transform""><xsl:output method=""xml"" indent=""yes"" encoding=""UTF-8""/><xsl:template match=""@*|node()""><xsl:copy>`n<xsl:apply-templates select=""@*|node()""/><xsl:for-each select=""@*""><xsl:text></xsl:text></xsl:for-each></xsl:copy>`n</xsl:template>`n</xsl:stylesheet>"),style:=null
		this.XML.TransformNodeToObject(xsl,this.xml)
	}Under(under,node,att:="",text:="",list:=""){
		new:=under.AppendChild(this.XML.CreateElement(node)),new.text:=text
		for a,b in att
			new.SetAttribute(a,b)
		for a,b in StrSplit(list,",")
			new.SetAttribute(b,att[b])
		return new
	}
}SSN(node,XPath){
	return node.SelectSingleNode(XPath)
}SN(node,XPath){
	return node.SelectNodes(XPath)
}m(x*){
	active:=WinActive("A")
	ControlGetFocus,Focus,A
	ControlGet,hwnd,hwnd,,%Focus%,ahk_id%active%
	static list:={btn:{oc:1,ari:2,ync:3,yn:4,rc:5,ctc:6},ico:{"x":16,"?":32,"!":48,"i":64}},msg:=[],msgbox
	list.title:="XML Class",list.def:=0,list.time:=0,value:=0,msgbox:=1,txt:=""
	for a,b in x
		obj:=StrSplit(b,":"),(vv:=List[obj.1,obj.2])?(value+=vv):(list[obj.1]!="")?(List[obj.1]:=obj.2):txt.=b "`n"
	msg:={option:value+262144+(list.def?(list.def-1)*256:0),title:list.title,time:list.time,txt:txt}
	Sleep,120
	MsgBox,% msg.option,% msg.title,% msg.txt,% msg.time
	msgbox:=0
	for a,b in {OK:value?"OK":"",Yes:"YES",No:"NO",Cancel:"CANCEL",Retry:"RETRY"}
		IfMsgBox,%a%
		{
			WinActivate,ahk_id%active%
			ControlFocus,%Focus%,ahk_id%active%
			return b
		}
}


