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

echo.
echo Server and first client launched.
echo If you want multiplayer, please open a second command prompt,
echo navigate to the project root, and run the client manually:
echo java --module-path lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -Djava.library.path=natives -cp out ui.Connect4Client
pause

