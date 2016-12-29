<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Add new Patient</title>
</head>

<body>

<form action="addToDB/users.php" method="post" name="form">
<p>Enter User name:
<input name="name" type="text" size="20" maxlength="25" />
</p>
<p>Enter User phone:
<input name="phone" type="text" size="20" maxlength="25" />
</p>
<p>Enter Server_id:
  <input name="server_id" type="text" size="20" maxlength="25" />
</p>
<p>
<input name="submit" type="submit" value="Add User" />
</p>
</form>
</body>
</html>