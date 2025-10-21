# Connect4 Game

Version: 1.0

Java Version: 11

Tested on Windows 11

## Overview

This is a Java-based Connect4 game that supports both single-player (against the computer) and multiplayer (two players on separate machines or the same machine). The game uses JavaFX for the GUI and socket communication for multiplayer sessions.

## Requirements

Java 11 installed on your system.

The project folder contains the following:

src/ — Java source code

lib/ — necessary libraries

natives/ — required native files for JavaFX

rungame.bat — batch file to compile and run the game (only for Windows 11)



## Running the Game

Automatic Launch (Single Machine)

You can compile and run the server two clients automatically using the batch file (Windows 11):

`run_game.bat`


The server will start.
The first client GUI will launch, prompting you to enter the server **public IP4 address** (default: localhost for same machine) then select Single Player or Multiplayer.


### Single-Player Mode

If you want to play single-player, you can simply use the first client and close any additional client prompts.


### Multiplayer Mode

If you want to play multiplayer:

Either open a second instance of the client on another machine or use the pre-opened second client from the automatic launch.

Enter the IP address of the machine running the server (default: localhost for same machine).

Click Multiplayer on both clients.

You will now be able to play on two separate GUIs against each other.



## Important Notes

Avoid double-clicking when making a move:

Each move is queued on the server. If a player clicks multiple times before the other player goes, moves will be executed automatically on your next turns.

Always wait for the opponent's turn before clicking your next move.

### Network Multiplayer:

To connect from a second machine, make sure the server machine’s IP address is used instead of localhost.

Ensure firewalls allow communication on port 8000.

### Known Issues:

The first client must wait for a second client to join if multiplayer is selected.
