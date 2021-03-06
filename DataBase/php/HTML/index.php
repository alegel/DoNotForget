<?php 
echo '
<head>
	<meta charset="utf-8"/>
	<title>DIVs</title>
	<link rel="stylesheet" href="style/style.css">
</head>
<body>
<div id="wrap">
	<header id="topper">
		<div id="dlogog">DoNotForget Reminder</div>
	</header>
	<!-- <nav>
		<ul>
			<li><a href="DIV_TEG.html">Home</a></li>
			<li><a href="about.html">About</a></li>
			<li><a href="DIV_TEG.html">Content</a></li>
		</ul>
	</nav> -->
	<section id="content">
		<div id="wrap1">
		<img src="images/1.jpg" class = "pic" />
		<img src="images/2.jpg" class = "pic" />
		<img src="images/3.jpg" class = "pic" />
		<img src="images/4.jpg" class = "pic" />
		<img src="images/5.jpg" class = "pic" />
		</div>
	</section>
	<footer>
	<p>The <b>“DoNotForget”</b> is a Reminder app to just remind you everything at a specified time.</p>
	<p><b>MOST IMPORTANT:</b> Unlike other reminders in Google play store, this one allows you to quickly schedule 
		a single or recurring reminders <b>NOT</b> only to you, but also <b>TO YOUR CONTACTS</b>.</p> 
	<p>This is very useful option, which allows you to schedule reminders for your parents, children, friends, coworkers and so on.</p>
	<p>This simple app is very easy to use. There are no unnecessary or complicated features. Reminders can be set in a matter of seconds.</p>
	<p><b>The main features are:</b>
		<ul>
			<li>An ability to schedule reminders to yourself and to other contacts.</li>
			<li>An ability to compose the contacts into groups and to set reminders for groups of contacts.</li>
			<li>In order to not to type the similar messages several times there is an option to save a list of useful messages and to use it when setting a reminder.</li>
			<li>The reminders could be set Daily, Weekly, Monthly and Yearly.</li>
			<li>There is an ability to Snooze reminders at different time intervals.</li>
			<li>No ads.
		</ul>
	</p>
	<p><b>IMPORTANT:</b> If you use any Task Manager or Task Killer application please add “DoNotForget” Reminder to the Ignored List. Otherwise “DoNotForget” Reminder may not work properly.</p>
	<p>
	<ul><b>Why does the app ask permissions to access personal data?</b>
		<li><b>Send SMS Messages</b> - Reminder needs this permission to send SMS to your phone in order to verify what You entered your phone number properly.
		It happens one time only at the registration stage. Except of this time <b>“DoNotForget”</b> app will never use SMS messages.</li>
		<li><b>Contact access</b> – <b>“DoNotForget”</b> app uses Your Contacts list only in cases, when you choose to send reminders to your contacts.</li>
		<li><b>Run at Startup</b> - Reminder needs this permission to ensure that your alarms are set after a device reboot.</li>
		<li><b>Internet access</b> – allows the app to send reminders to other contacts and to get notifications from Google Cloud Messaging.</li>
	</ul>
	</p>
	</footer>
</div>
</body>
';
?>