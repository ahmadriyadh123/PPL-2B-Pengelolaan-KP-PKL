@echo off
echo Building auth-commons...
cd /d "c:\Users\DIMAS\Documents\Semester 4\PPL (Pr)\PPL-2B-Pengelolaan-KP-PKL\account-service"
call mvnw.cmd clean install -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 -f "..\auth-commons\pom.xml"
echo Done. Exit=%ERRORLEVEL%
