$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path $PSScriptRoot -Parent
if (-not $env:JAVA_HOME -and (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-21.0.12')) {
    $env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.12'
}
$mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
$mavenExecutable = if ($mavenCommand) { $mavenCommand.Source } else { 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' }
if (-not (Test-Path -LiteralPath $mavenExecutable)) { throw 'Instale Maven o abra el proyecto en NetBeans con Java 21.' }
Push-Location -LiteralPath $projectDirectory
try {
    & $mavenExecutable '-B' '-ntp' '-Dmaven.repo.local=.cache/maven' 'spring-boot:run' '-Dspring-boot.run.profiles=local'
    exit $LASTEXITCODE
} finally { Pop-Location }
