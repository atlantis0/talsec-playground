rule android_metasploit
{
	meta:
	  author = "https://twitter.com/plutec_net"
	  description = "This rule detects apps made with metasploit framework"

	strings:
	  $a = "*Lcom/metasploit/stage/PayloadTrustManager;"
	  $b = "(com.metasploit.stage.PayloadTrustManager"
	  $c = "Lcom/metasploit/stage/Payload$1;"
	  $d = "Lcom/metasploit/stage/Payload;"

	condition:
	  1 of ($a,$b,$c,$d)
}