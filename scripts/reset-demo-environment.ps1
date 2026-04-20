param(
    [switch]$SkipBackendRestart
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptRoot
$composeFile = Join-Path $repoRoot "infra\docker-compose.yml"
$backendDir = Join-Path $repoRoot "backend"
$backendJar = Join-Path $backendDir "target\skillswap-market-backend-0.0.1-SNAPSHOT.jar"
$backendStdout = Join-Path $backendDir "backend.stdout.log"
$backendStderr = Join-Path $backendDir "backend.stderr.log"
$backendHealthUrl = "http://127.0.0.1:8080/api/health"
$postgresContainer = "skillswap-market-postgres"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Stop-BackendJar {
    $processes = Get-CimInstance Win32_Process |
            Where-Object {
                $_.Name -eq "java.exe" -and
                $_.CommandLine -like "*skillswap-market-backend-0.0.1-SNAPSHOT.jar*"
            }

    if (-not $processes) {
        Write-Host "No running backend jar process found."
        return
    }

    $processIds = @($processes | Select-Object -ExpandProperty ProcessId)
    Stop-Process -Id $processIds -Force
    Write-Host ("Stopped backend process ids: " + ($processIds -join ", "))
}

function Wait-ForPostgres {
    $maxAttempts = 30
    for ($attempt = 1; $attempt -le $maxAttempts; $attempt++) {
        $status = docker inspect -f "{{.State.Health.Status}}" $postgresContainer 2>$null
        if ($LASTEXITCODE -eq 0 -and $status.Trim() -eq "healthy") {
            Write-Host "Postgres container is healthy."
            return
        }
        Start-Sleep -Seconds 2
    }

    throw "Postgres container did not become healthy in time."
}

function Start-BackendJar {
    if (-not (Test-Path $backendJar)) {
        throw "Backend jar not found: $backendJar. Build the backend first."
    }

    if (Test-Path $backendStdout) {
        Remove-Item $backendStdout -Force
    }
    if (Test-Path $backendStderr) {
        Remove-Item $backendStderr -Force
    }

    Start-Process java `
        -ArgumentList "-jar", $backendJar `
        -WorkingDirectory $backendDir `
        -RedirectStandardOutput $backendStdout `
        -RedirectStandardError $backendStderr | Out-Null
}

function Wait-ForBackend {
    $maxAttempts = 30
    for ($attempt = 1; $attempt -le $maxAttempts; $attempt++) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing $backendHealthUrl -TimeoutSec 3
            if ($response.StatusCode -eq 200) {
                Write-Host "Backend health endpoint is up."
                return
            }
        } catch {
        }

        Start-Sleep -Seconds 2
    }

    throw "Backend health endpoint did not become available in time."
}

Write-Step "Stopping local backend jar"
Stop-BackendJar

Write-Step "Resetting Postgres data volume"
docker compose -f $composeFile down -v
docker compose -f $composeFile up -d

Write-Step "Waiting for Postgres healthcheck"
Wait-ForPostgres

if ($SkipBackendRestart) {
    Write-Host ""
    Write-Host "Database reset is complete."
    Write-Host "Restart the backend manually to rerun Flyway and seed baseline demo data."
    exit 0
}

Write-Step "Restarting backend jar"
Start-BackendJar

Write-Step "Waiting for backend health endpoint"
Wait-ForBackend

Write-Host ""
Write-Host "Demo environment has been reset to baseline."
Write-Host "Frontend: http://127.0.0.1:5173"
Write-Host "Backend:  http://127.0.0.1:8080/api/health"
