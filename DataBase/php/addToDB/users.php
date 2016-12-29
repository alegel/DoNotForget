<?php 
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */

echo "<br> In Add User page ";

// Check for required fields
if(isset($_POST['name']) || isset($_POST['phone']) || isset($_POST['server_id']))
{
	$name = $_POST['name'];
	$phone = $_POST['phone'];
	$server_id = $_POST['server_id'];
	
	if($name == '' || $phone == '' || $server_id == '')
	{	
		unset($name);
		unset($phone);
		unset($server_id);
		
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
	//mysql_query("SET NAMES 'UTF8';");
	
	$db_sel = mysqli_select_db($db, DB_DATABASE) or die(mysql_error());

	$sqlQuery = "SELECT * FROM users WHERE phone = '$phone'";
	$result = mysqli_query($db, $sqlQuery);
	$myrow = mysqli_fetch_array($result, MYSQLI_ASSOC);
	$count = mysqli_num_rows($result);
	if($count > 0)
	{	
/***** If User already exists - update his server_id ********************/	
		$str_Insert = "UPDATE users SET server_id = '$server_id' ";
		$str_Values = "WHERE phone = '$phone'";
	}
	else
	{
		$str_Insert = "INSERT INTO users (name, phone, server_id";
		$str_Values = " VALUES ('$name', '$phone', '$server_id'";
		$str_Insert .= ")";
		$str_Values .= ")";	
	}
/************************************************************************/
/*
	$str_Insert = "INSERT INTO users (name, phone, server_id";
	$str_Values = " VALUES ('$name', '$phone', '$server_id'";
	$str_Insert .= ")";
	$str_Values .= ")";	
*/	
	echo "The insert Command is:<br> ".$str_Insert." ".$str_Values."<br>";
	
	// Mysql inserting new row
	$result = mysqli_query($db, $str_Insert." ".$str_Values);
	// Check if row inserted or not
	if($result) {
		if($count <= 0)	// Notify other users only when new contact is added
		{
			include 'new_user_notif.php';	
		}
		// Successfully inserted
		$response['success'] = 1;
		$response['message'] = "User successfully inserted";

		echo utf8_encode(json_encode($response));
	} else {
		// Failed to Insert
		$response['success'] = 0;
		$response['message'] = "DataBase Error: ".mysql_error();
		echo utf8_encode(json_encode($response));
	}
	//mysqli_close($db);		// Added due to Hosting problems
}
else
{
	// Required field missing
	$response['success'] = 0;
	$response['message'] = "Required field is missing";
	echo utf8_encode(json_encode($response));
	mysqli_close($db);		// Added Dew to Hosting problems
	exit(0);
}

?>