$username = "siti_rahayu"
$password = "password123"
$baseUrl = "http://localhost:8081/api"
$filename = "dummy.jpg"

# Create dummy file
Set-Content -Path $filename -Value "This is a dummy image content" -Encoding Ascii

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

Write-Host "`n2. Uploading KTP..."
# Use curl.exe for multipart upload
$authHeader = "Authorization: Bearer $token"
$uploadUrl = "$baseUrl/profile/upload-ktp"

try {
    # We use direct curl execution via cmd to avoid PowerShell alias issues
    $curlCommand = "cmd /c curl -s -X POST -H ""$authHeader"" -F ""file=@$filename"" ""$uploadUrl"""
    Write-Host "   Executing: $curlCommand"
    $output = Invoke-Expression $curlCommand
    Write-Host "   Response: $output"
} catch {
    Write-Host "   Upload failed!"
    Write-Host $_
}

Write-Host "`n3. Verifying Profile..."
try {
    $headers = @{ Authorization = "Bearer $token" }
    $profile = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Get -Headers $headers
    Write-Host "   Current Upload KTP Path: $($profile.data.hasKtpUploaded)"
    # We can also call getKtpImage to see the string path if changed return type, but controller returns base64? 
    # Wait, getKtpImage originally returned base64 string. 
    # My service `getKtpImage` returns the PATH string now (profile.getUploadKtp()).
    # But the controller logic `getKtpImage` returns it as "Base64 encoded KTP image" in description. 
    # Actually, I changed the `uploadKtp` method in Service to return the PATH. 
    # The `getKtpImage` method in Service returns `profile.getUploadKtp()`.
    # Prior to my changes, it was base64. Now it is a path string like "/uploads/ktp/..."
    # So calling GET /api/profile/ktp will return the path string.
    
    $ktpResponse = Invoke-RestMethod -Uri "$baseUrl/profile/ktp" -Method Get -Headers $headers
    Write-Host "   KTP Endpoint Data: $($ktpResponse.data)"
} catch {
    Write-Host "   Failed to fetch profile!"
}
