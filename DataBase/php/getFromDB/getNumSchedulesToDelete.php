<?php
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */
$response_array["schedules"] = array();

if(isset($_POST['schedule_id'])  || isset($_POST['phone']))
{
	$schedule_id = $_POST['schedule_id'];
	$phone 		 = $_POST['phone'];
	if($schedule_id == ''  || $phone == '')
	{	
		unset($schedule_id);
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
/*** Verify, that there are not the same schedules for other contacts ********/
	$sql_command = "select schedule_id FROM contact_schedule WHERE phone <> '$phone'
					AND schedule_id = '$schedule_id' AND status <> 'del'";
					
	echo "sql_command: " . $sql_command;
	$result = mysqli_query($db, $sql_command);

	$myrow = mysqli_fetch_array($result,MYSQLI_ASSOC);
	$count = mysqli_num_rows($result);
	
	echo "<br>count: " . $count;
	
	if($count > 0)
	{
		// Do not delete this schedule_ID from 'schedule' Table
		$response['success'] = 0;
		$response['message'] = "This schedule is used by other Contacts";
		echo utf8_encode(json_encode($response));
		mysqli_close($db);		// Added due to Hosting problems
		exit(0);
		
	}
/*****************************************************************************/	
	$result = mysqli_query($db, "SELECT * FROM schedule WHERE schedule_id = '$schedule_id'");
	
	$myrow = mysqli_fetch_array($result,MYSQLI_ASSOC);
	$count = mysqli_num_rows($result);
	if($count > 0)
	{	
		$response['success'] = 1;
		$response['message'] = "There are schedules";
		echo utf8_encode(json_encode($response));
		mysqli_close($db);		// Added due to Hosting problems
		exit(0);
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