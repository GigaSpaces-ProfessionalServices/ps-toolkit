@ECHO OFF

set APP_HOME=%~dp0
for %%i in (%APP_HOME%lib\script-creator-*.jar) do java -jar %%i