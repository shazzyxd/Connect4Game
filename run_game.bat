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
start "" java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics -Djava.library.path=%NATIVES_LIB% -cp out core.Connect4Server

REM -------------------------
REM Launch first client
REM -------------------------
echo Launching first client...
start "" java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics -Djava.library.path=%NATIVES_LIB% -cp out ui.Connect4Client

REM -------------------------
REM Launch Second client
REM -------------------------
echo Launching second client...
start "" java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml,javafx.graphics -Djava.library.path=%NATIVES_LIB% -cp out ui.Connect4Client

echo.
echo Server and two clients launched.
echo If you want single player, you can close out a window
pause

