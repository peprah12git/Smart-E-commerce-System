param(
    [string]$MavenVersion = "3.9.6"
)

$MavenHome = "C:\tools\maven"
$DownloadUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/apache-maven-$MavenVersion-bin.zip"
$ZipFile = "$env:TEMP\maven.zip"

Write-Host "Installing Maven $MavenVersion..." -ForegroundColor Green
Write-Host "Download URL: $DownloadUrl"

try {
    # Download Maven
    Write-Host "Downloading Maven..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $DownloadUrl -OutFile $ZipFile -ErrorAction Stop
    
    # Create tools directory if needed
    if (!(Test-Path $MavenHome)) {
        New-Item -ItemType Directory -Path $MavenHome -Force | Out-Null
    }
    
    # Extract Maven
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $ZipFile -DestinationPath $MavenHome -Force
    
    # Rename the extracted folder
    $ExtractedFolder = Get-ChildItem -Path $MavenHome -Directory | Select-Object -First 1
    if ($ExtractedFolder) {
        Move-Item -Path "$($ExtractedFolder.FullName)\*" -Destination $MavenHome -Force
        Remove-Item -Path $ExtractedFolder.FullName -Force
    }
    
    # Add Maven to PATH
    $MavenBin = "$MavenHome\bin"
    $CurrentPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
    if ($CurrentPath -notlike "*$MavenBin*") {
        [System.Environment]::SetEnvironmentVariable("Path", "$CurrentPath;$MavenBin", "User")
        Write-Host "Added Maven to PATH"
    }
    
    Write-Host "Maven installed successfully!" -ForegroundColor Green
    Write-Host "Maven location: $MavenHome"
    Write-Host ""
    Write-Host "Please restart your PowerShell/CMD terminal and run:"
    Write-Host "mvn clean javafx:run"
    
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
} finally {
    # Cleanup
    if (Test-Path $ZipFile) {
        Remove-Item $ZipFile -Force
    }
}
