# Java Karaoke
A neat little library for playing MP3+CDG zipfiles.  

## Requirements

* Zipfile name (minus extension) must EXACTLY match inner CDG and MP3 filenames.

## Dependencies
* [Apache Commons Logging 1.1](http://www.java2s.com/Code/JarDownload/commons-logging/commons-logging-1.1.jar.zip)
* [MP3SPI 1.9.4](http://www.javazoom.net/mp3spi/sources/mp3spi1.9.4.zip)
* [BasicPlayer 2.3](https://experimentojakuk.googlecode.com/svn-history/r18/branches/b03/lib/basicplayer-2.3.jar)

## Building

    ant compile
    ant jar
    ant run
