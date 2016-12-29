<?php
require_once __DIR__ . '../../db/db_connect.php';
$response = array();	/* Array for JSON response */

echo "<br> In User exists ";

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
	$sqlQuery = "SELECT * FROM users WHERE phone = '$phone'";
	$result = mysqli_query($db, $sqlQuery);
	if($result)
	{
		$myrow = mysqli_fetch_array($result, MYSQLI_ASSOC);
		echo "<br>".$myrow['phone']."<br>";
		if($myrow['phone'] == '')
		{
			$response['success'] = 0;
			$response['message'] = "This user does not exist in database";
			echo utf8_encode(json_encode($response));
			mysqli_close($db);		// Added due to Hosting problems
			exit(0);
		}
		else
		{
			$response['success'] = 1;
			$response['message'] = "This phone exists in database";
			echo utf8_encode(json_encode($response));
			mysqli_close($db);		// Added due to Hosting problems
			exit(0);
		}
	}
	else
	{
		$response['success'] = 1;
		$response['message'] = "This phone exists in database";
		echo utf8_encode(json_encode($response));
		mysqli_close($db);		// Added due to Hosting problems
		exit(0);
	}
}

?>
</body>
</html>