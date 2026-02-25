@echo off
setlocal

:: Build the Docker image using the Dockerfile in the current directory
echo Building Docker image...
docker build -t experiment_image .
if %ERRORLEVEL% neq 0 (
    echo Docker build failed.
    exit /b %ERRORLEVEL%
)

:: Run the Docker container with the specified volumes
echo Running Docker container...
docker run --name experiment_container ^
    -v ./repetition_results/database:/home/user/java/build/resources/main/database ^
    -v ./repetition_results/images:/home/user/results/images ^
    experiment_image
if %ERRORLEVEL% neq 0 (
    echo Docker run failed.
    exit /b %ERRORLEVEL%
)

echo Docker container is running.
endlocal