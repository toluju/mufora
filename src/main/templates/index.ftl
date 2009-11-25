<html>
<head>
<title>Mufora</title>
</head>
<body>

<h1>List of Forums</h1>

<ul>
<#list it as forum>
<li><a href="forum/${forum.id}">${forum.name?html}</a></li>
</#list>
</ul>

</body>
</html>