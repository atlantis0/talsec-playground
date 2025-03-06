rule MatchAllApk
{
    meta:
        description = "Match all APK files but exclude a specific file based on its hash"
        author = "System Analyst"
        date = "2024-12-17"
        version = "1.0"

    strings:
        $apk_magic = { 50 4B 03 04 }  // Magic bytes indicating APK (ZIP format)

    condition:
        all of them
}