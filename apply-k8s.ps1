<#
.SYNOPSIS
    Применяет все Kubernetes-конфиги из директории deploy/k8s.
.DESCRIPTION
    Рекурсивно ищет YAML-файлы в поддиректориях и применяет их через kubectl apply -f.
#>

# Базовый путь до k8s конфигураций
$K8S_DIR = "deploy\k8s"

# Функция для применения всех YAML-файлов в директории
function Apply-K8sFiles {
    param (
        [string]$Directory
    )

    Write-Host "Applying files in $Directory..."

    # Получаем все YAML-файлы рекурсивно и сортируем по имени
    $yamlFiles = Get-ChildItem -Path $Directory -Recurse -Include "*.yaml", "*.yml" | Sort-Object Name

    if ($yamlFiles.Count -eq 0) {
        Write-Host "No YAML files found in $Directory"
        return
    }

    foreach ($file in $yamlFiles) {
        Write-Host "Applying $($file.FullName)"
        kubectl apply -f $file.FullName
    }
}

# Применяем конфигурации из всех поддиректорий
Apply-K8sFiles -Directory "$K8S_DIR\configmaps"
Apply-K8sFiles -Directory "$K8S_DIR\deployments"
Apply-K8sFiles -Directory "$K8S_DIR\HPA"
Apply-K8sFiles -Directory "$K8S_DIR\secrets"
Apply-K8sFiles -Directory "$K8S_DIR\services"
Apply-K8sFiles -Directory "$K8S_DIR\statefulsets"
Apply-K8sFiles -Directory "$K8S_DIR\ingress"

Write-Host "All configurations applied successfully."