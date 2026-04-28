Set-Location $PSScriptRoot

$files = Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
New-Item -ItemType Directory -Path out -Force | Out-Null
javac -d out $files
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$externalJar = "C:\Users\adity\Downloads\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar"
$classpath = "out;lib/*"
if (Test-Path $externalJar) {
    $classpath = "out;lib/*;$externalJar"
}

java -cp $classpath com.exam.main.App
