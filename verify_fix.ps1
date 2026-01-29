$username = "siti_rahayu"
$password = "password123"
$baseUrl = "http://localhost:8081/api"
$filename = "dummy_fix.jpg"

# 1. Login
Write-Host "1. Logging in..."
$loginBody = @{ username = $username; password = $password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.accessToken
$headers = @{ Authorization = "Bearer $token" }
Write-Host "   Login successful!"

# 2. Update Profile with tanggalLahir
Write-Host "`n2. Updating Profile (setting tanggalLahir to 1995-12-31)..."
$updateBody = @{
    fullName = "Siti Rahayu Updated"
    address = "Jl. Baru No. 123"
    identityNumber = "1234567890123456"
    tanggalLahir = "1995-12-31" # Format yyyy-MM-dd
    bankName = "BCA"
    bankAccountNumber = "1234567890"
    bankAccountHolderName = "Siti Rahayu"
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Put -Headers $headers -Body $updateBody -ContentType "application/json"
    Write-Host "   Update Response Code: $($updateResponse.code)"
    Write-Host "   Updated Tanggal Lahir: $($updateResponse.data.tanggalLahir)"
} catch {
    Write-Host "   Update Failed: $($_.Exception.Message)"
}

# 3. Verify Persistence (Get Profile)
Write-Host "`n3. Verifying Persistence..."
$profile = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Get -Headers $headers
Write-Host "   Fetched Tanggal Lahir: $($profile.data.tanggalLahir)"

if ($profile.data.tanggalLahir -eq "1995-12-31") {
    Write-Host "   SUCCESS: Date saved correctly!"
} else {
    Write-Host "   FAILURE: Date mismatch!"
}

# 4. Upload KTP and Verify Access
Write-Host "`n4. Uploading KTP..."
Set-Content -Path $filename -Value "dummy image content" -Encoding Ascii
$uploadUrl = "$baseUrl/profile/upload-ktp"
$authHeader = "Authorization: Bearer $token"
$curlCommand = "cmd /c curl -s -X POST -H ""$authHeader"" -F ""file=@$filename"" ""$uploadUrl"""
$output = Invoke-Expression $curlCommand
# Parse JSON from curl output manually or regex
# Assuming output is JSON
Write-Host "   Upload response: $output"

# Extract URL from profile data
$profile = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Get -Headers $headers
$imageUrl = $profile.data.ktpUrl
Write-Host "   Image URL (ktpUrl): '$imageUrl'"

# 5. Check if Image is Accessible
if ($imageUrl) {
    Write-Host "`n5. Verifying Image Access ($imageUrl)..."
    try {
        $imgCheck = Invoke-WebRequest -Uri "http://localhost:8081$imageUrl" -Method Head
        Write-Host "   Image Status: $($imgCheck.StatusCode)"
        if ($imgCheck.StatusCode -eq 200) {
            Write-Host "   SUCCESS: Image is accessible!"
        }
    } catch {
        Write-Host "   FAILURE: Could not access image! ($($_.Exception.Message))"
    }
} else {
    Write-Host "   FAILURE: No image URL found!"
}
