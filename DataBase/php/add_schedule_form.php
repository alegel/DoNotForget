<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Add new Patient</title>
</head>

<body>

<form action="addToDB/schedule.php" method="post" name="form">
<p>* Enter schedule_id:
<input name="schedule_id" type="text" size="20" maxlength="40" />
</p>
<p>* Enter fromDate:
<input name="fromDate" type="text" size="20" maxlength="25" />
</p>
<p>* Enter toDate:
  <input name="toDate" type="text" size="20" maxlength="25" />
</p>
<p>* Enter atTime:
<input name="atTime" type="text" size="20" maxlength="25" />
</p>
<p>* Enter msgText:
<input name="msgText" type="text" size="100" maxlength="100" />
</p>
<p>* Enter status:
<input name="status" type="text" size="10" maxlength="10" />
</p>
<p>* Enter recurring:
<input name="recurring" type="text" size="3" maxlength="3" />
</p>
<p>Enter onceDate:
<input name="onceDate" type="text" size="20" maxlength="20" />
</p>
<p>Enter onceTime:
<input name="onceTime" type="text" size="20" maxlength="20" />
</p>
<p>Enter playRing:
<input name="playRing" type="text" size="3" maxlength="3" />
</p>
<p>Enter vibrate:
<input name="vibrate" type="text" size="3" maxlength="3" />
</p>
<p>Enter weekDays:
<input name="weekDays" type="text" size="10" maxlength="10" />
</p>
<p>
<input name="submit" type="submit" value="Add Schedule" />
</p>
</form>
</body>
</html>