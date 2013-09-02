<?php
	shell_exec('jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -storepass baixing -keystore baixing.keystore -signedjar LunchNow-signed.apk LunchNow-unsigned.apk baixing.keystore');
	shell_exec('zipalign.exe -f -v 4 LunchNow-signed.apk LunchNow.apk');
	