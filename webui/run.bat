@ECHO OFF

set APP_HOME=%~dp0
for %%i in (%APP_HOME%lib\webui-*.jar) do java -jar %%i