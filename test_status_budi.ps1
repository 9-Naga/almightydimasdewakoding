$username = "budi_santoso"
$password = "password123"
$baseUrl = "http://localhost:8081/api"

try {
    Write-Host "1. Logging in as $username..."
    $loginBody = @{ username = $username; password = $password } | ConvertTo-Json
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.data.accessToken
    $headers = @{ Authorization = "Bearer $token" }
    Write-Host "   Login successful!"

    Write-Host "`n2. Checking Profile Data..."
    $profile = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Get -Headers $headers
    Write-Host "   Tanggal Lahir: '$($profile.data.tanggalLahir)'"
    
    Write-Host "`n3. Checking Profile Status..."
    $status = Invoke-RestMethod -Uri "$baseUrl/profile/status" -Method Get -Headers $headers
    Write-Host "   Is Complete: $($status.data)"
    Write-Host "   Message: $($status.message)"

} catch {
    Write-Host "Error occurred:"
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Response Body: $($reader.ReadToEnd())"
    }
}
