<?php
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */
$response_array["schedules"] = array();

if(isset($_POST['status']))
{
	$status = $_POST['status'];
}

if(isset($_POST['phone']))
{
	$phone = $_POST['phone'];
}
if($status == ''|| $phone == '')
{
	unset($status);
	unset($phone);
		
	// One of the required fields is empty
	$response['success'] = 0;
	$response['message'] = "One of the required fields is empty";
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
$result = mysqli_query($db, "UPDATE contact_schedule SET status = '$status' WHERE phone = '$phone'");


if($result == true){
	$response['success'] = 1;
	$response['message'] = "The raws were updated successfully";
	echo utf8_encode(json_encode($response));
	mysqli_close($db);		// Added due to Hosting problems
	exit(0);
}
else
{
	$response['success'] = 0;
	$response['message'] = "The raws were not updated, error: ".mysql_error();
	echo utf8_encode(json_encode($response));
	mysqli_close($db);		// Added due to Hosting problems
	exit(0);
}

?>
</body>
</html>