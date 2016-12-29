<?php 
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */

echo "<br> In add contact_schedule page ";

// Check for required fields
if(isset($_POST['schedule_id']) || isset($_POST['fromPhone']) || isset($_POST['phone']))
{
	$schedule_id = $_POST['schedule_id'];
	$fromPhone = $_POST['fromPhone'];
	$phone = $_POST['phone'];
	
	if($schedule_id == '' || $fromPhone == '' || $phone == '')
	{	
		unset($schedule_id);
		unset($fromPhone);
		unset($phone);
		
		// One of the required fields is empty
		$response['success'] = 0;
		$response['message'] = "One of the required fields is empty";
		echo utf8_encode(json_encode($response));
		exit(0);
	}
	//echo "<br> schedule_id = ".$schedule_id;
	//$db = new DB_CONNECT();
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
/************** Verify that this user exists in DB **************************/
	$sqlQuery = "SELECT * FROM users WHERE phone = '$phone'";
	$res = mysqli_query($db, $sqlQuery);
	if(!$res)
	{
		echo "<br>Phone: ".$phone."<br>";
		$response['success'] = 0;
		$response['message'] = "DataBase Error: ".mysql_error();
		echo utf8_encode(json_encode($response));
		mysqli_close($db);		// Added due to Hosting problems
		exit(0);
	}
	if(mysqli_num_rows($res) > 0)
	{
		$myrow = mysqli_fetch_array($res,MYSQLI_ASSOC);
		$response['success'] = 1;
		$response['message'] = "This phone exists in database ".$myrow['phone'];
		echo utf8_encode(json_encode($response));
	}
	else
	{
		$response['success'] = 0;
		$response['message'] = "This user does not exist in database ".$phone;
		echo utf8_encode(json_encode($response));
		mysqli_close($db);		// Added due to Hosting problems
		exit(0);
	}
	
/****************************************************************************/
	// insert only existing felds
	$str_Insert = "INSERT INTO contact_schedule (schedule_id, fromPhone, phone, status";
	$str_Values = " VALUES ('$schedule_id', '$fromPhone', '$phone', 'new'";

	$str_Insert .= ")";
	$str_Values .= ")";
	
	//echo "The insert Command is:<br> ".$str_Insert." ".$str_Values."<br>";
	
	// Mysql inserting new row
	$result = mysqli_query($db, $str_Insert." ".$str_Values);
	// Check if row inserted or not
	if($result) {
		// Successfully inserted
		$response['success'] = 1;
		$response['message'] = "contactSchedule successfully inserted";

		echo utf8_encode(json_encode($response));
	} else {
		// Failed to Insert
		$response['success'] = 0;
		$response['message'] = "DataBase Error: ".mysql_error();
		echo utf8_encode(json_encode($response));
	}
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

?>