<html>
<head>
<title>Mufora</title>
</head>
<body>

<h1>List of Forums</h1>

<ul>
<#list it as forum>
<li>${forum.name?html}</li>
</#list>
</ul>

</body>
</html>