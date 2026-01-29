$username = "siti_rahayu"
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

# 2. Get current profile to fetch required fields
Write-Host "`n2. Fetching current profile to get required fields..."
try {
    $currentProfile = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Get -Headers $headers
    $p = $currentProfile.data
    
    Write-Host "   Current Bank Name: $($p.bankName)"
    Write-Host "   Current Tanggal Lahir: $($p.tanggalLahir)"
} catch {
    Write-Host "   Failed to fetch profile!"
    exit
}

# 3. Update Profile
Write-Host "`n3. Updating Profile (Bank -> BCA, DOB -> 2000-06-21)..."

# Construct request body using existing data for required fields
$updateBody = @{
    fullName = $p.fullName
    address = $p.address
    identityNumber = $p.identityNumber
    bankName = "BCA"  # Changed as requested
    bankAccountNumber = $p.bankAccountNumber
    bankAccountHolderName = $p.bankAccountHolderName
    tanggalLahir = "2000-06-21" # Format YYYY-MM-DD
    uploadKtp = "dummy_base64_ktp_string" # Assuming we want to keep/set this
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$baseUrl/profile" -Method Put -Headers $headers -Body $updateBody -ContentType "application/json"
    Write-Host "   Update Response Code: $($updateResponse.code)"
    Write-Host "   Updated Bank Name: $($updateResponse.data.bankName)"
    Write-Host "   Updated Tanggal Lahir: $($updateResponse.data.tanggalLahir)"
} catch {
    Write-Host "   Update Failed!"
    Write-Host $_.Exception.Response
    # Print detailed error if available
    if ($_.Exception.Response.GetResponseStream()) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "   Error Body: $($reader.ReadToEnd())"
    }
}

# 4. Check API Status
Write-Host "`n4. Checking Profile Status (api/profile/status)..."
try {
    $statusResponse = Invoke-RestMethod -Uri "$baseUrl/profile/status" -Method Get -Headers $headers
    Write-Host "   Status Message: $($statusResponse.message)"
    Write-Host "   Is Complete: $($statusResponse.data)"
} catch {
    Write-Host "   Failed to check status!"
}
