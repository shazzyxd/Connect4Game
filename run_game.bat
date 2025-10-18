@echo off
echo ==========================================
echo      Launching Connect4Game
echo ==========================================
echo.

set JAVAFX_LIB=lib
set NATIVES_LIB=natives

REM -------------------------
REM Compile all source files
REM -------------------------
echo Compiling all source files...
javac --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics -d out src\core\*.java src\ui\*.java

if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b
)

echo Compilation successful.
echo.

REM -------------------------
REM Start the server in a new window
REM -------------------------
echo Starting the server...
start cmd /k "java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics -Djava.library.path=%NATIVES_LIB% -cp out core.Connect4Server"

REM -------------------------
REM Launch first client
REM -------------------------
echo Launching first client...
start cmd /k "java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics -Djava.library.path=%NATIVES_LIB% -cp out ui.Connect4Client"

REM -------------------------
REM Ask if user wants multiplayer
REM -------------------------
:ASKMULTI
set /p MULTI=Do you want to launch a second client for multiplayer? (Y/N)
if /i "%MULTI%"=="Y" (
    timeout /t 2 /nobreak
    echo Launching second client...
    start cmd /k "java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics -Djava.library.path=%NATIVES_LIB% -cp out ui.Connect4Client"
    goto DONE
) else if /i "%MULTI%"=="N" (
    goto DONE
) else (
    echo Please enter Y or N.
    goto ASKMULTI
)

:DONE
echo.
echo Server and first client launched.
echo If you want single-player, just continue with the first client.
pause

