@echo off

if "%1" == ""  (
@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\setenv.bat"
)

if "%1" == "version"  (
%JAVACMD% -cp "%JSHOMEDIR%/lib/JSpaces.jar";"%JSHOMEDIR%/lib/openspaces/openspaces.jar" org.openspaces.maven.support.OutputVersion
goto END
)

FOR /F "usebackq tokens=*" %%i IN (`installmavenrep.bat version`) DO @set VERSION=%%i

%JAVACMD% -cp "%JSHOMEDIR%/lib/JSpaces.jar";"%JSHOMEDIR%/lib/openspaces/openspaces.jar" org.openspaces.maven.support.POMGenerator %TEMP% %VERSION% ../lib/openspaces/maven-openspaces-plugin/

echo ""
echo ""
echo "Installing Version %VERSION%"
echo ""
echo ""

REM Jini Jars
@call mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-start -Dversion=%VERSION% -DpomFile=%TEMP%/jini-start-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/start.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-jsk-lib -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-lib-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/jsk-lib.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-jsk-platform -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-platform-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/jsk-platform.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-jsk-resources -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-resources-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/jsk-resources.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-reggie -Dversion=%VERSION% -DpomFile=%TEMP%/jini-reggie-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/reggie.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-mahalo -Dversion=%VERSION% -DpomFile=%TEMP%/jini-mahalo-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/mahalo.jar"

REM GigaSpaces Jars
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=gs-boot -Dversion=%VERSION% -DpomFile=%TEMP%/gs-boot-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/ServiceGrid/gs-boot.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=gs-service -Dversion=%VERSION% -DpomFile=%TEMP%/gs-service-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/ServiceGrid/gs-service.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=gs-lib -Dversion=%VERSION% -DpomFile=%TEMP%/gs-lib-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/ServiceGrid/gs-lib.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=JSpaces -Dversion=%VERSION% -DpomFile=%TEMP%/JSpaces-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/JSpaces.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=openspaces -Dversion=%VERSION% -DpomFile=%TEMP%/openspaces-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/openspaces/openspaces.jar"
call mvn install:install-file -DgroupId=gigaspaces -DartifactId=mule-os -Dversion=%VERSION% -Dpackaging=jar -DpomFile=%TEMP%/mule-os-pom.xml -Dfile="%JSHOMEDIR%/lib/openspaces/mule-os.jar"

REM Build and install OpenSpaces Maven Plugin 
call mvn -f ../lib/openspaces/maven-openspaces-plugin/pom.xml install 

REM Copy licenese file
call mvn os:install-license -Dfile="%JSHOMEDIR%\gslicense.xml" -Dversion=%VERSION%

:END
