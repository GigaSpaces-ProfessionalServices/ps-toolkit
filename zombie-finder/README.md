##Zombie Finder

***Purpose*** 

Reports stray DataGrid components. "Stray DataGrid components" are defined as DataGrid components (GSC, GSM, LUS, etc) that are running, but are for some reason not known to the Admin instances that manage the service grid.

***To implement*** 

1. Edit [zombie-finder-dev.properties](./src/main/resources/zombie-finder-dev.properties)

2. Build

```
mvn clean package
```

3. Run

```
java -jar target/zombie-finder.jar com.zombiefinder.app.ZombieFinder dev
```