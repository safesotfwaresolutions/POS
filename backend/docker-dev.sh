#!/usr/bin/env bash
# ============================================================================
# docker-dev.sh — Script de utilidad para el control del entorno Docker de Desarrollo
# ============================================================================
# USO: ./docker-dev.sh [comando]
# ============================================================================

set -euo pipefail

# --- Colores para salida legible ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

show_help() {
    echo -e "${CYAN}Uso: ./docker-dev.sh [comando]${NC}"
    echo -e "${YELLOW}Comandos disponibles:${NC}"
    echo "  start    - Levanta los contenedores en segundo plano (docker compose up -d)"
    echo "  stop     - Detiene los contenedores (docker compose down)"
    echo "  rebuild  - Reconstruye y levanta los contenedores (docker compose up -d --build)"
    echo "  restart  - Reinicia los servicios (docker compose restart)"
    echo "  logs     - Muestra logs en vivo del backend (docker compose logs -f app)"
    echo "  shell    - Abre una consola interactiva en el contenedor del backend (docker compose exec app bash)"
    echo "  status   - Muestra el estado de los contenedores (docker compose ps)"
}

if [ $# -lt 1 ]; then
    show_help
    exit 0
fi

ACTION="$1"

case "$ACTION" in
    start)
        echo -e "${GREEN}Levantando entorno Docker de desarrollo...${NC}"
        docker compose up -d
        ;;
    stop)
        echo -e "${YELLOW}Deteniendo contenedores de desarrollo...${NC}"
        docker compose down
        ;;
    rebuild)
        echo -e "${GREEN}Reconstruyendo imágenes y levantando entorno...${NC}"
        docker compose up -d --build
        ;;
    restart)
        echo -e "${GREEN}Reiniciando contenedores...${NC}"
        docker compose restart
        ;;
    logs)
        echo -e "${CYAN}Mostrando logs del contenedor del backend (Ctrl+C para salir)...${NC}"
        docker compose logs -f app
        ;;
    shell)
        echo -e "${GREEN}Entrando en la terminal del contenedor...${NC}"
        docker compose exec app bash
        ;;
    status)
        docker compose ps
        ;;
    *)
        show_help
        exit 1
        ;;
esac
