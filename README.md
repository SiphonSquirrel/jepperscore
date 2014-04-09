JepperScore
===========

JepperScore is a realtime LAN party scoreboard and statistic collector. It relies on custom built scrapers for each game supported to provide the real time statistics, which are persistently stored for analysis later.

Game Support
------------

**Fully Supported:**
 * [BattleField 1942](https://github.com/SiphonSquirrel/jepperscore/wiki/BattleField-1942)
 * [Enemy Territory : Quake Wars](https://github.com/SiphonSquirrel/jepperscore/wiki/Enemy-Territory-Quake-Wars)
 
**Partially Supported:**
 * [UT2004](https://github.com/SiphonSquirrel/jepperscore/wiki/UT2004)
 * [RTCW : Enemy Territory](https://github.com/SiphonSquirrel/jepperscore/wiki/Enemy-Territory-Legacy)
 * [Call of Duty 4](https://github.com/SiphonSquirrel/jepperscore/wiki/Call-of-Duty)
 
**Planned:**
 * BattleField 2

See [Game Support](https://github.com/SiphonSquirrel/jepperscore/wiki/Game-Support) in online manual for more information.

Building
--------

 *  Install Maven 3 --> [Maven 3.0.5](https://maven.apache.org/docs/3.0.5/release-notes.html)
 *  Clone project --> git clone https://github.com/SiphonSquirrel/jepperscore.git
 *  Run Maven --> mvn package

Running
-------

**Note 1:** These instructors are incredibly light (closer to quick notes) until they can be properly written.

**Note 2:** You will need to download and install a backend. Your choices are listed here on the [backends page](https://github.com/SiphonSquirrel/jepperscore/wiki/Backends).

### Games

See [Game Support](https://github.com/SiphonSquirrel/jepperscore/wiki/Game-Support) in online manual for more information.

### JepperConsole

This application prints out the messages it sees to the console. Good for debugging!

The main class is _jepperscore.jepperconsole.Main_
and takes the arguments: -c [Message Source Class] -s [Message Source Setup]

### JepperVCR

This application has two parts: a Play and a Record. The application is for recording and playing back events seen on the backend.

The main class for the Record part is: _jepperscore.tools.jeppervcr.Record_
and takes the arguments: -c [Message Source Class] -s [Message Source Setup] -o [Output File]

The main class for the Play part is: _jepperscore.tools.jeppervcr.Play_
and takes the arguments: -c [Message Destination Class] -s [Message Destination Setup] -i [Input File]
