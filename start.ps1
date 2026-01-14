# Build et lancement du projet
# Ce fichier permet de builder sur n'importe quel PC avec Docker

Write-Host "=== Nettoyage ===" -ForegroundColor Cyan
docker compose down -v

Write-Host "`n=== Configuration DNS Docker ===" -ForegroundColor Cyan
# Configure Docker pour utiliser les DNS publics (fix problème résolution DNS)
$daemonJson = @{
    "dns" = @("8.8.8.8", "8.8.4.4", "1.1.1.1")
}

$dockerConfigPath = "$env:USERPROFILE\.docker\daemon.json"
$dockerConfigDir = Split-Path $dockerConfigPath

if (!(Test-Path $dockerConfigDir)) {
    New-Item -ItemType Directory -Path $dockerConfigDir -Force | Out-Null
}

$daemonJson | ConvertTo-Json | Set-Content $dockerConfigPath
Write-Host "DNS configurés dans Docker daemon.json" -ForegroundColor Green

Write-Host "`n=== Building Docker images ===" -ForegroundColor Cyan
Write-Host "Note: Le premier build peut prendre 10-15 minutes pour télécharger les dépendances Maven." -ForegroundColor Yellow

docker compose build --no-cache

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n=== Starting services ===" -ForegroundColor Green
    docker compose up
} else {
    Write-Host "`nBuild failed. Vérifiez les logs ci-dessus." -ForegroundColor Red
    Write-Host "Si le problème DNS persiste, redémarrez Docker Desktop." -ForegroundColor Yellow
}
