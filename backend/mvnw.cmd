@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper from %WRAPPER_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$u='%WRAPPER_URL%'; $o='%WRAPPER_JAR%';" ^
    "New-Item -ItemType Directory -Force (Split-Path -Parent $o) | Out-Null;" ^
    "Invoke-WebRequest -UseBasicParsing -Uri $u -OutFile $o"
  if errorlevel 1 exit /b 1
)

set "MAVEN_OPTS=%MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%"
set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXEC%" set "JAVA_EXEC=java"

"%JAVA_EXEC%" -classpath "%WRAPPER_JAR%" -Dmaven.home= -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
if errorlevel 1 exit /b 1
endlocal
exit /b 0

