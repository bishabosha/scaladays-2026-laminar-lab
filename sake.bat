@echo off
rem Runs the Sakefile command runner (tooling\sake) with scala-cli. Unix: ./sake
scala-cli run --quiet "%~dp0tooling\sake" -- %*
exit /b %ERRORLEVEL%
