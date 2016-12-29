<?php
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */
$response_array["schedules"] = array();

echo "<br> In schedules page ";

if(isset($_POST['phone']))
{
	$phone = $_POST['phone'];
	if($phone == '')
	{	
		unset($phone);
		// One of the required fields is empty
		$response['success'] = 0;
		$response['message'] = "phone field is empty";
		echo utf8_encode(json_encode($response));
		exit(0);
	}
	
	//$db = new DB_CONNECT();	// CLOSED due to Hosting problems
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
	}
	
	$db_sel = mysqli_select_db($db, DB_DATABASE) or die(mysql_error());
/**********************************************************/
	$result = mysqli_query($db, "SELECT * FROM schedule WHERE schedule_id IN 
						(select schedule_id FROM contact_schedule WHERE phone = '$phone' AND status = 'new')")
						or die(mysql_error());

	$myrow = mysqli_fetch_array($result,MYSQLI_ASSOC) or die(mysql_error());
	
	if(mysqli_num_rows($result) > 0)
	{	
		//$response_array['data'] = "schedules";
		do {
			$response['recurring'] = $myrow['recurring'];
			$response['onceDate'] = $myrow['onceDate'];
			$response['fromDate'] = $myrow['fromDate'];
			$response['toDate'] = $myrow['toDate'];
			$response['onceTime'] = $myrow['onceTime'];
			$response['atTime'] = $myrow['atTime'];
			$response['scheduleFrom'] = $myrow['scheduleFrom'];
			if($myrow['playRing'] == '')
				$response['playRing'] = 0;
			else
				$response['playRing'] = $myrow['playRing'];
			if($myrow['vibrate'] == '')
				$response['vibrate'] = 0;
			else
				$response['vibrate'] = $myrow['vibrate'];
			$response['weekDays'] = $myrow['weekDays'];
			$response['msgText'] = $myrow['msgText'];
			
			array_push($response_array["schedules"], $response);
			
		}while($myrow = mysqli_fetch_array($result, MYSQLI_ASSOC));
		$response_array["success"] = 1;
		echo utf8_encode(json_encode($response_array));
		mysqli_close($db);		// Added due to Hosting problems
	}
	else
	{
		$response['success'] = 0;
		$response['message'] = "There are no schedules";
		echo utf8_encode(json_encode($response));
		mysqli_close($db);		// Added due to Hosting problems
		exit(0);
	}
}

?>
</body>
</html>