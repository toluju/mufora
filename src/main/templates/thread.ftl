<html>
<head>
<title>Mufora - Thread - ${it.name?html}</title>
</head>
<body>

<h1>Thread: ${it.name?html}</h1>

<a href="../../${it.forum.id}">Thread Index</a>

<ul>
<#list it.posts as post>
  <li>${post.content?html}</li>
</#list>
</ul>

<a href="${it.id}/post/new">New Post</a>

</body>
</html>