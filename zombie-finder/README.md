##Zombie Finder

***Purpose:*** This non-executable jar file has a main method. It can be pointed at a GigaSpaces DataGrid and will report if there are any stray Grid components. "Stray grid components" are defined as DataGrid components (GSC, GSM, LUS, etc) that are running, but are for some reason not known to the Admin instances that manage the service grid.

***Usage:*** Building against the pom file in this directory will generate a non-executable jar file. It requires a single argument. The argument specifies the suffix for a properties file that is built into the root of this jar's classpath. For example, if src/main/resources/zombie-finder-dev.properties is the one you want to use, you would need to pass the word 'dev' to the main method.

Properties in that file are self-explanatory
