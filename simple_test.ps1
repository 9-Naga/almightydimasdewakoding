$baseUrl = "http://localhost:8081/api"

# 1. Login
$loginBody = '{"username":"siti_rahayu","password":"password123"}'
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.accessToken
$headers = @{ Authorization = "Bearer $token" }
Write-Host "Login successful!"

# 2. Get Profile
$profile = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Get -Headers $headers
Write-Host "tanggalLahir: $($profile.data.tanggalLahir)"
Write-Host "ktpUrl: $($profile.data.ktpUrl)"
Write-Host "hasKtpUploaded: $($profile.data.hasKtpUploaded)"

# 3. Check profile status
$status = Invoke-RestMethod -Uri "$baseUrl/profile/status" -Method Get -Headers $headers
Write-Host "Profile Complete: $($status.data)"
Write-Host "Status Message: $($status.message)"

# 4. Test image access if ktpUrl exists
if ($profile.data.ktpUrl) {
    $imgUrl = "http://localhost:8081$($profile.data.ktpUrl)"
    Write-Host "Testing image URL: $imgUrl"
    try {
        $imgResponse = Invoke-WebRequest -Uri $imgUrl -Method Head -UseBasicParsing
        Write-Host "Image accessible! Status: $($imgResponse.StatusCode)"
    } catch {
        Write-Host "Image NOT accessible: $($_.Exception.Message)"
    }
}
