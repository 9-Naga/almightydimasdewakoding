$username = "dewi_lestari"
$password = "password123"
$baseUrl = "http://localhost:8081/api"

Write-Host "1. Logging in as $username..."
$loginBody = @{
    username = $username
    password = $password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.accessToken
    Write-Host "   Login successful!"
} catch {
    Write-Host "   Login failed!"
    Write-Host $_.Exception.Response
    exit
}

$headers = @{
    Authorization = "Bearer $token"
}

Write-Host "`n2. Checking Profile Status (api/profile/status)..."
try {
    $statusResponse = Invoke-RestMethod -Uri "$baseUrl/profile/status" -Method Get -Headers $headers
    Write-Host "   Status Message: $($statusResponse.message)"
    Write-Host "   Is Complete: $($statusResponse.data)"
} catch {
    Write-Host "   Failed to check status!"
    Write-Host $_.Exception.Response
}
