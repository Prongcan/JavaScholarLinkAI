@echo off
echo ========================================
echo  ScholarLinkAI Recommendation Orchestrator
echo ========================================
echo.
echo Starting Recommendation Orchestrator...
echo This will analyze user interests and generate
echo personalized paper recommendations.
echo.
echo Press any key to continue...
pause > nul
echo.
echo Running Recommendation Orchestrator...
echo.

REM Change to project directory
cd /d %~dp0

REM Run the orchestrator
java -cp target/classes org.example.orchestrator_layer.RecommendationOrchestrator

echo.
echo Recommendation Orchestrator completed!
echo Press any key to exit...
pause > nul
