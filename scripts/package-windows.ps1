param(
    [switch]$SkipTests
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ([System.Environment]::OSVersion.Platform -ne [System.PlatformID]::Win32NT) {
    throw "Windows packaging must be run on Windows because jpackage creates a Windows executable."
}

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' was not found on PATH."
    }
}

Require-Command "mvn"
Require-Command "jpackage"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$pomPath = Join-Path $root "pom.xml"
[xml]$pom = Get-Content -Raw $pomPath

$artifactId = [string]$pom.project.artifactId
$version = [string]$pom.project.version
$appName = "3D Particle Simulator"
$safeName = "3d-particle-simulator"
$mainClass = "com.particle.sim.ParticleSimulatorApp"
$vendor = "OlivierVencken"

$targetDir = Join-Path $root "target"
$inputDir = Join-Path $targetDir "jpackage-input"
$libDir = Join-Path $inputDir "lib"
$packageLibDir = Join-Path $targetDir "lib"
$imageDestDir = Join-Path $targetDir "jpackage"
$appImageDir = Join-Path $imageDestDir $appName
$releaseDir = Join-Path $targetDir "release"
$mainJar = "$artifactId-$version.jar"
$mainJarPath = Join-Path $targetDir $mainJar
$zipPath = Join-Path $releaseDir "$safeName-$version-windows-x64.zip"

$mvnArgs = @(
    "clean",
    "package"
)

if ($SkipTests) {
    $mvnArgs += "-DskipTests"
}

foreach ($path in @($inputDir, $imageDestDir, $releaseDir)) {
    if (Test-Path $path) {
        Remove-Item -Recurse -Force -LiteralPath $path
    }
}

Write-Host "Building project..."
Push-Location $root
try {
    & mvn @mvnArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}

if (-not (Test-Path $mainJarPath)) {
    throw "Expected main jar was not found: $mainJarPath"
}
if (-not (Test-Path $packageLibDir)) {
    throw "Expected runtime dependency directory was not found: $packageLibDir"
}

New-Item -ItemType Directory -Force -Path $inputDir | Out-Null
Copy-Item -Force -Path $mainJarPath -Destination (Join-Path $inputDir $mainJar)
New-Item -ItemType Directory -Force -Path $libDir | Out-Null
Copy-Item -Recurse -Force -Path (Join-Path $packageLibDir "*") -Destination $libDir

if (Test-Path $imageDestDir) {
    Remove-Item -Recurse -Force -LiteralPath $imageDestDir
}
if (Test-Path $releaseDir) {
    Remove-Item -Recurse -Force -LiteralPath $releaseDir
}
New-Item -ItemType Directory -Force -Path $imageDestDir, $releaseDir | Out-Null

Write-Host "Creating Windows app image..."
$jpackageArgs = @(
    "--type", "app-image",
    "--name", $appName,
    "--app-version", $version,
    "--vendor", $vendor,
    "--input", $inputDir,
    "--main-jar", $mainJar,
    "--main-class", $mainClass,
    "--dest", $imageDestDir,
    "--java-options", "--enable-native-access=ALL-UNNAMED"
)

& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE."
}

$exePath = Join-Path $appImageDir "$appName.exe"
if (-not (Test-Path $exePath)) {
    throw "Expected executable was not created: $exePath"
}

foreach ($file in @("README.md", "LICENSE")) {
    $source = Join-Path $root $file
    if (Test-Path $source) {
        Copy-Item -Force -Path $source -Destination (Join-Path $appImageDir $file)
    }
}

Write-Host "Creating release zip..."
Compress-Archive -Path $appImageDir -DestinationPath $zipPath -Force

Write-Host ""
Write-Host "Release package created:"
Write-Host $zipPath
Write-Host ""
Write-Host "Executable inside zip:"
Write-Host "$appName\$appName.exe"
