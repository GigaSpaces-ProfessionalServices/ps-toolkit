##Zombie Finder

> The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.

The Apache v2 license applies. Here's [the fine print](../license.txt).

***Purpose*** 

Reports stray DataGrid components. "Stray DataGrid components" are defined as DataGrid components (GSC, GSM, LUS, etc) that are running, but are for some reason not known to the Admin instances that manage the service grid.

***To use*** 

1. Edit [zombie-finder-dev.properties](./src/main/resources/zombie-finder-dev.properties)

2. Build

```
mvn clean package
```

3. Run

```
java -jar target/zombie-finder.jar com.zombiefinder.app.ZombieFinder dev
```