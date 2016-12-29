<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Add contact_schedule_form</title>
</head>

<body>

<form action="addToDB/contactSchedule.php" method="post" name="form">
<p>Enter schedule ID:
<input name="schedule_id" type="text" size="20" maxlength="40" />
</p>
<p>From phone:
<input name="fromPhone" type="text" size="20" maxlength="25" />
</p>
<p>Phone:
  <input name="phone" type="text" size="20" maxlength="25" />
</p>
<p>
<input name="submit" type="submit" value="Add data to contact_Schedules" />
</p>
</form>
</body>
</html>