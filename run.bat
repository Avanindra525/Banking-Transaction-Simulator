@echo off
setlocal EnableDelayedExpansion

if "%DB_USERNAME%"=="" set DB_USERNAME=root

if not "%SERVER_PORT%"=="" (
	set "APP_PORT=%SERVER_PORT%"
	call :is_port_in_use !APP_PORT!
	if "!PORT_IN_USE!"=="1" (
		echo Port !APP_PORT! is already in use. Set a different SERVER_PORT value.
		exit /b 1
	)
) else (
	set "APP_PORT="
	for %%P in (8081 8082 8083 8084 8085 8086 8087 8088 8089 8090) do (
		call :is_port_in_use %%P
		if "!PORT_IN_USE!"=="0" (
			set "APP_PORT=%%P"
			goto :port_selected
		)
	)

	echo No free port found in the range 8081-8090. Close an app or set SERVER_PORT manually.
	exit /b 1
)

:port_selected
echo Starting app with MySQL user: %DB_USERNAME%
echo Starting app on port: !APP_PORT!
"C:\Program Files\Maven\bin\mvn.cmd" spring-boot:run "-Dspring-boot.run.arguments=--server.port=!APP_PORT!"
exit /b %errorlevel%

:is_port_in_use
set "PORT_IN_USE=0"
netstat -ano | findstr /R /C:":%~1 .*LISTENING" >nul
if %errorlevel%==0 set "PORT_IN_USE=1"
exit /b 0

