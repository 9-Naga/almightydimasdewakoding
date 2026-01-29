$username = "dewi_lestari"
$password = "password123"
$baseUrl = "http://localhost:8081/api"

$loginBody = @{ username = $username; password = $password } | ConvertTo-Json
$token = (Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json").data.accessToken
$headers = @{ Authorization = "Bearer $token" }

try {
    $profile = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Get -Headers $headers
    Write-Host "Profile Data:"
    Write-Host "Tanggal Lahir: '$($profile.data.tanggalLahir)'"
    Write-Host "Is Null? $(-not $profile.data.tanggalLahir)"

    $status = Invoke-RestMethod -Uri "$baseUrl/profile/status" -Method Get -Headers $headers
    Write-Host "Status Response:"
    $status | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Error:"
    Write-Host $_.Exception.Message
}
