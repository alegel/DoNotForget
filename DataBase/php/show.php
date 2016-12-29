<?php
require_once __DIR__ . '../db/db_connect.php';
$response = array();	/* Array for JSON response */

echo "<br> In Add Show page ";

/* Samples
$result = mysql_query("SELECT * FROM Patient WHERE id = '2'",$db);
$result = mysql_query("SELECT * FROM Patient ORDER BY phone LIMIT 1",$db);
*/
$db = new DB_CONNECT();
$result = mysql_query("SELECT * FROM users ORDER BY id");
$sel = "";
$myrow = mysql_fetch_array($result);
do {
/*	echo "Patient N - ".$myrow['id']."; Name - ".$myrow['name'].", Phone - ".$myrow['phone']."<br>"; */
	printf("<br>User N - %s, Name - %s, Phone - %s<br>",$myrow['id'],$myrow['name'],$myrow['phone']);
	//$sel .= "<br>"."id:".$myrow['id']."name:".$myrow['name']."phone:".$myrow['phone'];
	$response['id'] = $myrow['id'];
	$response['name'] = $myrow['name'];
	$response['phone'] = $myrow['phone'];
	$sel .= "<br>".$response['id'].$response['name'].$response['phone'];
}while($myrow = mysql_fetch_array($result));
echo $sel;

?>
</body>
</html>