Param()
$ErrorActionPreference = 'Stop'
Set-Location -Path (Split-Path -Parent $MyInvocation.MyCommand.Definition)

$bin = "C:\Program Files\Microsoft\Edge\Application\msedge.exe"
if (-not (Test-Path $bin)) {
  throw "Microsoft Edge not found at $bin"
}

function To-FileUrl([string]$path) {
  $p = [System.IO.Path]::GetFullPath($path) -replace '\\','/'
  "file:///$p"
}

& "$bin" --headless --disable-gpu --hide-scrollbars --window-size=512,512 --screenshot="icon-512.png" (To-FileUrl "icon-512.svg")
& "$bin" --headless --disable-gpu --hide-scrollbars --window-size=1024,500 --screenshot="feature-graphic-1024x500.png" (To-FileUrl "feature-graphic-1024x500.svg")

Write-Host "Export complete: icon-512.png, feature-graphic-1024x500.png"
