<?php 
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */

echo "<br> In Add Schedule page ";

// Check for required fields
if(isset($_POST['schedule_id']) || isset($_POST['fromDate']) || isset($_POST['toDate'])
  		|| isset($_POST['atTime'])|| isset($_POST['msgText']) || isset($_POST['recurring']) 
		|| isset($_POST['scheduleFrom']))
{
	$schedule_id = $_POST['schedule_id'];
	$fromDate = $_POST['fromDate'];
	$toDate = $_POST['toDate'];
	$atTime = $_POST['atTime'];
	$msgText = $_POST['msgText'];
	$recurring = $_POST['recurring'];
	$scheduleFrom = $_POST['scheduleFrom'];
	
	if($schedule_id == '' || $fromDate == '' || $toDate == '' || $atTime == ''
	  || $msgText == '' || $recurring == ''  || $scheduleFrom == '')
	{	
		unset($schedule_id);
		unset($fromDate);
		unset($toDate);
		unset($atTime);
		unset($msgText);
		unset($recurring);
		unset($scheduleFrom);
		
		// One of the required fields is empty
		$response['success'] = 0;
		$response['message'] = "One of the required fields is empty";
		echo utf8_encode(json_encode($response));
		exit(0);
	}
//	echo "<br> schedule_id = ".$schedule_id;
	//echo "<br>msgText = " . $msgText . "<br>";
	
	// insert only existing felds
	$str_Insert = "INSERT INTO schedule (schedule_id, fromDate, toDate, atTime, msgText, recurring, scheduleFrom";
	$str_Values = " VALUES ('$schedule_id', '$fromDate', '$toDate', '$atTime', '$msgText', '$recurring', '$scheduleFrom'";
	
	if(isset($_POST['onceDate'])) {
		$onceDate = $_POST['onceDate'];
		if($onceDate != ''){
			$str_Insert .= ", onceDate";
			$str_Values .= ", '$onceDate'";
		}
	}
	else {
		unset($onceDate);
	}
	if(isset($_POST['onceTime'])){
		$onceTime = $_POST['onceTime'];
		if($onceTime != ''){
			$str_Insert .= ", onceTime";
			$str_Values .= ", '$onceTime'";
		}
	}
	else {
		unset($onceTime);
	}
	if(isset($_POST['playRing'])){
		$playRing = $_POST['playRing'];
		if($playRing != ''){
			$str_Insert .= ", playRing";
			$str_Values .= ", '$playRing'";
		}
	}
	else {
		unset($playRing);
	}
	if(isset($_POST['vibrate'])){
		$vibrate = $_POST['vibrate'];
		if($vibrate != ''){
			$str_Insert .= ", vibrate";
			$str_Values .= ", '$vibrate'";
		}
	}
	else {
		unset($vibrate);
	}
	if(isset($_POST['weekDays'])){
		$weekDays = $_POST['weekDays'];
		if($weekDays != ''){
			$str_Insert .= ", weekDays";
			$str_Values .= ", '$weekDays'";
		}
	}
	else {
		unset($weekDays);
	}
	$str_Insert .= ")";
	$str_Values .= ")";
	
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
	}
	
//	mysqli_query($db,"SET NAMES 'utf8';");
//	mysqli_set_charset($db,"utf8");
	
	$db_sel = mysqli_select_db($db, DB_DATABASE) or die(mysql_error());
/**********************************************************/
	
//	echo "The insert Command is:<br> ".$str_Insert." ".$str_Values."<br>";
	
	// Mysql inserting new row
	$result = mysqli_query($db, $str_Insert." ".$str_Values);
	
//	echo "The result is:<br> ".$result."<br>";
	
	// Check if row inserted or not
	if($result) {
		// Successfully inserted
		$response['success'] = 1;
		$response['message'] = "Schedule successfully inserted";
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