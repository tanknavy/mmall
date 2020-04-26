<%@ page language="java"  contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html lang="zh">
<head>
    <title>Login Page</title>
    <meta charset="utf-8">
</head>

<body>
<h2>Hello World!</h2>
    <!-- form:form action="${pageContext.request.contextPath}/authenticateTheUser" -->
	<form name="login" action="/mmall/user/login.do" method="POST">
		<p>
			User name: <input type="text" name="username" />
		</p>
		<p>
			Password: <input type="text" name="password" />
		</p>
		<input type="submit" value="Login" />
	</form>


Spring mvn file upload
<form name="form1" action="/mmall/manage/product/upload.do" method="post", enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="springmvc upload" />
</form>

Spring mvn rich file upload 富文本文件上传
<form name="form2" action="/mmall/manage/product/richtext_upload.do" method="post", enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="springmvc upload" />
</form>

</body>
</html>
