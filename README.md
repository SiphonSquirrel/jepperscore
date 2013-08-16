JepperScore
===========

JepperScore is a realtime LAN party scoreboard and statistic collector. It relies on custom built scrapers for each game supported to provide the real time statistics, which are persistently stored for analysis later.

Game Support
------------

1.  Battlefield 1942 - WIP

Building
--------

*  Install Maven 3 --> [Maven 3.0.5](https://maven.apache.org/docs/3.0.5/release-notes.html)
*  Clone project --> git clone https://github.com/SiphonSquirrel/jepperscore.git
*  Run Maven --> mvn package

Running
-------

**Note 1:** These instructors are incredibly light (closer to quick notes) until they can be properly written.

**Note 2:** You will need to download and install [ActiveMQ 5.8.0](https://activemq.apache.org/activemq-580-release.html)

**Note 3:** The [Active MQ Connection String] parameter can usually be set to --> tcp://ActiveMQHostName:61616

### JepperConsole

The main class is _jepperscore.jepperconsole.Main_ and takes the arguements: [Active MQ Connection String]

### Battlefield 1942 Scraper

The main class is _jepperscore.scraper.bf1942.Main_ and takes the arguements: [Active MQ Connection String] [BF 1942 Log Directory] [Hostname] [Query Port]
