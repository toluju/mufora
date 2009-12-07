<html>
<head>
<title>Mufora - Thread - ${it.name?html}</title>
<link rel="stylesheet" type="text/css" media="screen" href="/css/styles.css"/>
<script type="text/javascript" src="/js/jquery.js"></script>
<script type="text/javascript" src="/js/showdown.js"></script>
<script type="text/javascript">

var converter = new Showdown.converter();

$(document).ready(function() {
  $("li").each(function() {
    $(this).html(converter.makeHtml($(this).html()));
  });
});

</script>
</head>
<body>

<h1>Thread: ${it.name?html}</h1>

<a href="../../${it.forum.id}">Thread Index</a>

<ul>
<#list it.posts as post>
  <li>${post.content?html}</li>
</#list>
</ul>

<h2>New Post</h2>
<form action="${it.id}/post/new" method="post">
  <label for="name">Content:</label>
  <textarea name="content" rows="12" cols="50"></textarea>
  <input type="submit"/>
</form>

</body>
</html>