<?php
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */
$response_array["users"] = array();

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
	$result = mysqli_query($db, "SELECT phone FROM users");
	
	$myrow = mysqli_fetch_array($result,MYSQLI_ASSOC);
	if(mysqli_num_rows($result) > 0)
	{	
		//$response_array['data'] = "schedules";
		do {
			$response['phone'] = $myrow['phone'];			
			array_push($response_array["users"], $response);
			
		}while($myrow = mysqli_fetch_array($result,MYSQLI_ASSOC));
		$response_array["success"] = 1;
		echo utf8_encode(json_encode($response_array));
		mysqli_close($db);		// Added due to Hosting problems
	}
	else
	{
		$response['success'] = 0;
		$response['message'] = "There are no users";
		echo utf8_encode(json_encode($response));
		mysqli_close($db);		// Added due to Hosting problems
		exit(0);
	}


?>
</body>
</html>