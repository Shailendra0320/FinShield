$port = 5173
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:$port/")
$listener.Start()
Write-Host "FinShield React Dashboard Server running live on http://localhost:$port/"

$filePath = "C:\GitHub and Antigravity\FinShield_Monolith\frontend\preview.html"
$content = [System.IO.File]::ReadAllBytes($filePath)

while ($listener.IsListening) {
    try {
        $context = $listener.GetContext()
        $response = $context.Response
        $response.ContentType = "text/html; charset=utf-8"
        $response.ContentLength64 = $content.Length
        $response.OutputStream.Write($content, 0, $content.Length)
        $response.OutputStream.Close()
    } catch {
        # ignore abort on stop
    }
}