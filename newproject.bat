@echo off
cls  
color A  
:MAIN  
cls  
echo =============================================================
echo What is the Project name? 
echo (This cannot include spaces, or any other special characters) 
echo =============================================================
REM Get the user's input
set /p var=
REM If the folder already exists, do nothing. We don't want to overwrite anything
if exist "%var%" goto :eof
REM If the user just pressed enter, without giving a name, do nothing
mkdir "%var%"
cd "%var%"
REM Unzip the basic app using java. Since you are going to work on Android, I assume you have the JDK.
call jar xf C:\Dropbox\eclipse\workspace\thebasicapp.zip
REM Replace all of the human readable strings with your name
call C:\Dropbox\eclipse\fart -c -r -i *.* thebasicappreadable "%var%"
REM Remove spaces from the name
SETLOCAL EnableDelayedExpansion
set set var=!var: =!
REM Make the name lowercase
call :tolower var
REM Rename one of the directories
cd src\com\pcessflight
rename thebasicapp "!var!"
cd ..\..\..
REM Replace all of the compressed app names
call C:\Dropbox\eclipse\fart -c -r -i *.* thebasicapp "!var!"

:tolower
for %%L IN (a b c d e f g h i j k l m n o p q r s t u v w x y z) DO SET %1=!%1:%%L=%%L!
goto :EOF