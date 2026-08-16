<#
.SYNOPSIS
  Script de utilidad para el control del entorno Docker de Desarrollo.
.DESCRIPTION
  Uso: .\docker-dev.ps1 <comando>
  Comandos disponibles:
    start    - Levanta los contenedores en segundo plano.
    stop     - Detiene y remueve los contenedores de desarrollo.
    rebuild  - Reconstruye las imágenes y levanta los contenedores.
    restart  - Reinicia los contenedores.
    logs     - Muestra y sigue los logs del backend.
    shell    - Entra al contenedor del backend de desarrollo de manera interactiva.
    status   - Muestra el estado de los contenedores.
#>

param (
    [string]$Action = ""
)

$AllowedActions = @("start", "stop", "rebuild", "restart", "logs", "shell", "status")

if ($Action -eq "" -or $AllowedActions -notcontains $Action) {
    Write-Host "Uso: .\docker-dev.ps1 <comando>" -ForegroundColor Cyan
    Write-Host "Comandos disponibles:" -ForegroundColor Yellow
    Write-Host "  start    - Levanta los contenedores en segundo plano (docker compose up -d)"
    Write-Host "  stop     - Detiene los contenedores (docker compose down)"
    Write-Host "  rebuild  - Reconstruye y levanta los contenedores (docker compose up -d --build)"
    Write-Host "  restart  - Reinicia los servicios (docker compose restart)"
    Write-Host "  logs     - Muestra logs en vivo del backend (docker compose logs -f app)"
    Write-Host "  shell    - Abre una consola interactiva en el contenedor del backend (docker compose exec app bash)"
    Write-Host "  status   - Muestra el estado de los contenedores (docker compose ps)"
    exit 0
}

switch ($Action) {
    "start" {
        Write-Host "Levantando entorno Docker de desarrollo..." -ForegroundColor Green
        docker compose up -d
    }
    "stop" {
        Write-Host "Deteniendo contenedores de desarrollo..." -ForegroundColor Yellow
        docker compose down
    }
    "rebuild" {
        Write-Host "Reconstruyendo imágenes y levantando entorno..." -ForegroundColor Green
        docker compose up -d --build
    }
    "restart" {
        Write-Host "Reiniciando contenedores..." -ForegroundColor Green
        docker compose restart
    }
    "logs" {
        Write-Host "Mostrando logs del contenedor del backend (Ctrl+C para salir)..." -ForegroundColor Cyan
        docker compose logs -f app
    }
    "shell" {
        Write-Host "Entrando en la terminal del contenedor..." -ForegroundColor Green
        docker compose exec app bash
    }
    "status" {
        docker compose ps
    }
}
