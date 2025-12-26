@echo off
echo ========================================
echo  ScholarLinkAI Fetch Orchestrator
echo ========================================
echo.
echo Starting Fetch Orchestrator...
echo This will fetch arXiv papers from yesterday to today
echo and process them through the Papers API.
echo.
echo Press any key to continue...
pause > nul
echo.
echo Running Fetch Orchestrator...
echo.

REM Change to project directory
cd /d %~dp0

REM Run the orchestrator
java -cp target/classes org.example.orchestrator_layer.FetchOrchestrator

echo.
echo Fetch Orchestrator completed!
echo Press any key to exit...
pause > nul
