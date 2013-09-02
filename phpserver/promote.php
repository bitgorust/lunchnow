<?php
	require_once('func.php');
	
	switch ($_GET["action"])
	{
	case 'download':
      	$udid = $_GET["udid"];
		$file = 'apk/LunchNow-' . $udid . '.apk';
		
		//$t_start = microtime(true);
		
		if (!file_exists($file)) {
			require_once('class.phplock.php');
			
			$lock = new PHPLock( 'lock/', 'sign_apk' );
			$lock->startLock();
			$status = $lock->Lock();
			if (!$status) {
				exit("lock error");
			}
			
			$fp = fopen('PROMOTER_ID', 'w');
			fwrite($fp, $udid);
			fclose($fp);
			
			$unsigned_file = 'LunchNow-unsigned.apk';
			require_once('pclzip.lib.php');
			$archive = new PclZip($unsigned_file);
			$archive->delete(PCLZIP_OPT_BY_NAME, 'assets/PROMOTER_ID');
			$archive->add('PROMOTER_ID', PCLZIP_OPT_ADD_PATH, 'assets');
			
			shell_exec('jarsigner -verbose -digestalg SHA1 -sigalg MD5withRSA -storepass baixing -keystore baixing.keystore -signedjar LunchNow-signed.apk LunchNow-unsigned.apk baixing.keystore');
			shell_exec('zipalign.exe -f -v 4 LunchNow-signed.apk ' . $file);
			unlink('LunchNow-signed.apk');
			
			$lock->unlock();
			$lock->endLock();
		}
		
		//$t_end = microtime(true);
		//echo (($t_end - $t_start) * 1000) . ' ms';
		
		$filename = 'LunchNow.apk';
		header("Content-type: application/octet-stream");
		header('Content-Disposition: attachment; filename="' . $filename . '"');
		header("Content-Length: ". filesize($file));
		readfile($file);
		
		break;
	case 'join':
		$receiver = $_GET["receiver"];
		$promoter = $_GET["promoter"];
		joining($receiver, $promoter);
		break;
	case 'order':
		$receiver = $_GET["receiver"];
		$promoter = $_GET["promoter"];
		order($receiver, $promoter);
		break;
    case 'info':
      	$promoter = $_GET["udid"];
      	info($promoter);
      	break;
	default:
		break;
	}