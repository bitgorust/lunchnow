@echo off
jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -storepass baixing -keystore baixing.keystore -signedjar LunchNow-signed.apk LunchNow-unsigned.apk baixing.keystore
zipalign.exe -f -v 4 LunchNow-signed.apk LunchNow.apk