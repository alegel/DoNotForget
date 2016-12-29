<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Add new Patient</title>
</head>

<body>

<form action="patient_table/add_patient.php" method="post" name="form">
<p>Enter Patient name:
<input name="name" type="text" size="20" maxlength="40" />
</p>
<p>Enter Patient phone:
<input name="phone" type="text" size="20" maxlength="25" />
</p>
<p>Enter Server_id:
  <input name="server_id" type="text" size="20" maxlength="25" />
</p>
<p>Enter Pakage_id:
<input name="pakage_id" type="text" size="20" maxlength="25" />
</p>
<p>Enter group_id:
<input name="group_id" type="text" size="3" maxlength="3" />
</p>
<p>Enter medicines_id:
<input name="medicines_id" type="text" size="3" maxlength="3" />
</p>
<p>Enter procedures_id:
<input name="procedures_id" type="text" size="3" maxlength="3" />
</p>
<p>Enter schedule_id:
<input name="schedule_id" type="text" size="3" maxlength="3" />
</p>
<p>Enter measurements_id:
<input name="measurements_id" type="text" size="3" maxlength="3" />
</p>
<p>
<input name="submit" type="submit" value="Add Patient" />
</p>
</form>
</body>
</html>