@echo off
rem Runs the Sakefile command runner (tooling\sake\sake.scala). Unix: ./sake
rem
rem With clang available, sake is compiled once with Scala Native and cached in .sake\ (it then
rem starts in milliseconds); the cache follows a hash of sake.scala, so edits rebuild it.
rem Otherwise, or with SAKE_JVM=1, it runs on the JVM through scala-cli.
setlocal
set "SAKE_HOME=%~dp0"
if defined SAKE_JVM goto jvm
set "SUM="
for /f "skip=1 delims=" %%H in ('certutil -hashfile "%SAKE_HOME%tooling\sake\sake.scala" MD5') do if not defined SUM set "SUM=%%H"
set "SUM=%SUM: =%"
set "BIN=%SAKE_HOME%.sake\sake-%SUM%.exe"
if exist "%BIN%" goto native
if exist "%BIN%.failed" (
  echo sake: running on the JVM ^(the native build failed, see .sake\build.log^) 1>&2
  goto jvm
)
where clang >nul 2>nul
if errorlevel 1 (
  echo sake: clang not found, running on the JVM 1>&2
  goto jvm
)
echo sake: compiling natively with Scala Native ^(once, a minute or so^)... 1>&2
if not exist "%SAKE_HOME%.sake" mkdir "%SAKE_HOME%.sake"
call scala-cli --power package --native "%SAKE_HOME%tooling\sake" -o "%BIN%" -f > "%SAKE_HOME%.sake\build.log" 2>&1
if errorlevel 1 (
  rem A stale or half-written incremental build (e.g. an interrupted one) can fail: retry once from clean.
  call scala-cli clean "%SAKE_HOME%tooling\sake" >> "%SAKE_HOME%.sake\build.log" 2>&1
  call scala-cli --power package --native "%SAKE_HOME%tooling\sake" -o "%BIN%" -f >> "%SAKE_HOME%.sake\build.log" 2>&1
)
if errorlevel 1 (
  if exist "%BIN%" del /q "%BIN%"
  type nul > "%BIN%.failed"
  echo sake: native build failed ^(see .sake\build.log^), running on the JVM 1>&2
  goto jvm
)
:native
"%BIN%" %*
exit /b %ERRORLEVEL%
:jvm
call scala-cli run --quiet "%SAKE_HOME%tooling\sake" -- %*
exit /b %ERRORLEVEL%
