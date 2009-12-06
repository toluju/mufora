<html>
<head>
<title>Mufora - Forum - ${it.name?html}</title>
</head>
<body>

<h1>Forum - ${it.name?html}</h1>

<a href="..">Forum Index</a>

<ul>
<#list it.threads as thread>
  <li><a href="${it.id}/thread/${thread.id}">${thread.name?html}</a></li>
</#list>
</ul>

<a href="${it.id}/thread/new">New Thread</a>

</body>
</html>