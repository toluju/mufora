<html>
<head>
<title>Mufora - Thread - ${it.name?html}</title>
</head>
<body>

<h1>Thread: ${it.name?html}</h1>

<ul>
<#list it.posts as post>
  <li>${post.content?html}</li>
</#list>
</ul>

</body>
</html>