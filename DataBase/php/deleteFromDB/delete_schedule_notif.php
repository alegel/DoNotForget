<?php 
require_once __DIR__ . '../../db/db_connect.php';

echo "<br> In delete schedule notif. ";
if(isset($_POST['phone']))
{
	$phone = $_POST['phone'];
	if($phone == '')
	{	
		unset($phone);
		
		// One of the required fields is empty
		$response['success'] = 0;
		$response['message'] = "One of the required fields is empty";
		echo utf8_encode(json_encode($response));
		exit(0);
	}
	
/*** Added new connection to DB, due to Hosting problems **************/
		define('DB_USER', "u845466442_alexg");
		define('DB_PASSWORD', "alegel2806");
		define('DB_DATABASE', "u845466442_donot");
		define('DB_SERVER', "localhost");
	
		$db = mysqli_connect(DB_SERVER, DB_USER, DB_PASSWORD, DB_DATABASE);
			// Check connection
		if (mysqli_connect_errno())
		{
		   echo "Failed to connect to MySQL: " . mysqli_connect_error();
		   
		   	$response['success'] = 0;
			$response['message'] = "Failed to connect to MySQL";
			echo utf8_encode(json_encode($response));
			exit(0);
		}
		
		$db_sel = mysqli_select_db($db, DB_DATABASE) or die(mysql_error());
/**********************************************************/
	
	$message = $phone;
	$title = 'Delete schedule';
	$path_to_fcm = 'https://fcm.googleapis.com/fcm/send';
	$server_key = 'AIzaSyC7SWmUyjxHxfWF2wUtjljAec-C7AIKn-o';
	$sql = "SELECT server_id FROM users WHERE phone = '$phone'";
	//$sql = "SELECT server_id FROM users WHERE phone = '0528373974'";
	
	$res = mysqli_query($db, $sql);
	/*
	$my_row = mysqli_fetch_row($res);
	$key - $my_row[0];
	*/
	/**********************************/
	$myrow = mysqli_fetch_array($res,MYSQLI_ASSOC);
	if(mysqli_num_rows($res) > 0)
	{
		$key = $myrow['server_id'];
		echo "<br> server_id = ".$key;
		
	}
	else
	{
		echo "<br> The row is empty";
		$response['success'] = 0;
		$response['message'] = "The row is empty";
		echo utf8_encode(json_encode($response));
		exit(0);
	}
	
	/**********************************/
	$headers = array(
						'Authorization:key=' .$server_key,
						'Content-Type:application/json'
					);
					
	$fields = array(
						'to'=>$key,
						'data'=>array('title'=>$title,'body'=>$message)
					);
	
	$j = json_decode(sendCurl($path_to_fcm, $headers, $fields));
	$succ = $j->{'success'};
	$fail = $j->{'failure'};
	if($succ == 0)	// Send the Notification again
	{
		echo "\n Deleting Schedule Notification success is: " .$succ;
		echo "\n Deleting Schedule Notification failure is: " .$fail;
		echo "\n Sending Notification Again";
		
		$j = json_decode(sendCurl($path_to_fcm, $headers, $fields));
		$succ = $j->{'success'};
		$fail = $j->{'failure'};
	}
	echo "\n Deleting Schedule Notification success is: " .$succ;
	echo "\n Deleting Schedule Notification failure is: " .$fail;
	
	$response['success'] = $succ;
	$response['message'] = "Deleting Schedule Notification";
	echo utf8_encode(json_encode($response));
	
	mysqli_close($db);		// Added due to Hosting problems
}
else
{
	// Required field missing
	$response['success'] = 0;
	$response['message'] = "Required field is missing";
	echo utf8_encode(json_encode($response));
	mysqli_close($db);		// Added due to Hosting problems
	exit(0);
}

	function sendCurl($path_to_fcm, $headers, $fields)
	{
		$payload = json_encode($fields);
		$curl_session = curl_init();
		
		curl_setopt($curl_session, CURLOPT_URL, $path_to_fcm);
		curl_setopt($curl_session, CURLOPT_POST, true);
		curl_setopt($curl_session, CURLOPT_HTTPHEADER, $headers);
		curl_setopt($curl_session, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($curl_session, CURLOPT_SSL_VERIFYPEER, false);
		curl_setopt($curl_session, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4);
		curl_setopt($curl_session, CURLOPT_POSTFIELDS, $payload);
		
		$res = curl_exec($curl_session);
		curl_close($curl_session);
		
		return $res;
	}

?>