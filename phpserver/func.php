<?php

	define('MYSQL_HOST', 'localhost');
	define('MYSQL_USER', 'root');
	define('MYSQL_PASS', 'pisces228');
	define('MYSQL_DB', 'lunchnow');
	define('MYSQL_TABLE', 'promote');
	
	define('APP_SECRET', 'P3OQn5wnzgDLFhN5h/bBhoeLISOunCGngAgzjy2NQbI=');
	define('PACKAGE_NAME', 'com.baixing.lunchnow');
	define('PUSH_URL', 'https://api.xmpush.xiaomi.com/v1/send');
	
	class DataConnection {
		private static $connection = null;

		public static function getConnection() {
			if (self::$connection === null) {
				self::$connection = mysql_connect(MYSQL_HOST, MYSQL_USER, MYSQL_PASS) or die(mysql_error());
				mysql_select_db(MYSQL_DB) or die(mysql_error());
				mysql_query('set names utf8') or die(mysql_error());
			}
			return self::$connection;
		}

		public static function closeConnection() {
			if (isset(self::$connection))
				mysql_close(self::$connection) or die(mysql_error());
			self::$connection = null;
		}
	}
	
	function sendToAlias($message, $ttl, $alias)
	{
		if(empty($message) || empty($alias) || !is_int($ttl))
			echo 'wrong parameter';
		else
		{
			$fields = array('payload' => $message, 'ttl' => $ttl, 'restricted_package_name' => PACKAGE_NAME, 'alias' => $alias);
			$headers = array('Authorization: key=' . APP_SECRET, 'Content-Type: application/x-www-form-urlencoded');
			
			// Open connection
			$ch = curl_init();

			// Set the url, number of POST vars, POST data
			curl_setopt($ch, CURLOPT_URL, PUSH_URL);
			curl_setopt($ch, CURLOPT_POST, true);
			curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
			curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($fields));
			
			// Execute post
			$result = curl_exec($ch);
			
			// Close connection
			curl_close($ch);
		}
	}
	
	function broadcast($message, $ttl, $topic)
	{
		if(empty($message) || empty($topic) || !is_int($ttl))
			echo 'wrong parameter';
		else
		{
			$fields = array('payload' => $message, 'ttl' => $ttl, 'restricted_package_name' => PACKAGE_NAME, 'topic' => $topic);
			$headers = array('Authorization: key=' . APP_SECRET, 'Content-Type: application/x-www-form-urlencoded');
			
			// Open connection
			$ch = curl_init();

			// Set the url, number of POST vars, POST data
			curl_setopt($ch, CURLOPT_URL, PUSH_URL);
			curl_setopt($ch, CURLOPT_POST, true);
			curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
			curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($fields));
			
			// Execute post
			$result = curl_exec($ch);
			
			// Close connection
			curl_close($ch);
			echo $result;
		}
	}
	
	function joining($receiver, $promoter)
    {
		$con = DataConnection::getConnection();
		
      	$query = sprintf("SELECT promoter, receiver, orders FROM promote WHERE promoter='%s' AND receiver='%s'", mysql_real_escape_string($promoter), mysql_real_escape_string($receiver));
		$result = mysql_query($query, $con) or die(mysql_error());
		if (mysql_num_rows($result) == 0) {
			$query = sprintf("INSERT INTO promote(promoter, receiver) VALUES('%s', '%s')", mysql_real_escape_string($promoter), mysql_real_escape_string($receiver));
			$ret = mysql_query($query, $con) or die(mysql_error());
			sendToAlias("成功邀请" . $receiver, 0, $promoter);
		} else {
			sendToAlias($receiver . "回来了", 0, $promoter);
		}
		echo "";
	}
	
	function order($receiver, $promoter)
    {
      	$con = DataConnection::getConnection();
		
      	$query = sprintf("SELECT promoter, receiver, orders FROM promote WHERE promoter='%s' AND receiver='%s'", mysql_real_escape_string($promoter), mysql_real_escape_string($receiver));
		$result = mysql_query($query, $con) or die(mysql_error());
		if (mysql_num_rows($result) == 0) {
			$query = sprintf("INSERT INTO promote(promoter, receiver) VALUES('%s', '%s')", mysql_real_escape_string($promoter), mysql_real_escape_string($receiver));
			$ret = mysql_query($query, $con) or die(mysql_error());
		}
		$row = mysql_fetch_assoc($result);
		$order = $row['orders'] + 1;
		$query = sprintf("UPDATE promote SET orders = " . $order . " WHERE promoter = '%s' AND receiver = '%s'", mysql_real_escape_string($promoter), mysql_real_escape_string($receiver));
		$ret = mysql_query($query, $con) or die(mysql_error());
		sendToAlias($receiver . "订餐成功", 0, $promoter);
		echo "";
	}
	
	function info($promoter)
    {
      	$con = DataConnection::getConnection();
		
      	$query = sprintf("SELECT receiver, orders FROM promote WHERE promoter = '%s'", mysql_real_escape_string($promoter));
        $ret = mysql_query($query, $con) or die(mysql_error());
		$content = "";
		while ($row = mysql_fetch_assoc($ret)) {
			$content = $content . $row['receiver'] . "(" . $row['orders'] . ") ";
		}
		echo $content;
    }