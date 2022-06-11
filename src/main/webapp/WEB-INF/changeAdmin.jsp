<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<head>
    <title>NoSqlDB</title>
    <link href="webjars/bootstrap/5.0.2/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container" style="width:300px">
        </form>
        <form method="POST">
            <h2 class="mb-4 mt-4">SetUp Admin: </h2>
            <div class="form-group">
                Username : <input required="required" name="username" type="text" placeholder="username" class="form-control mb-4"/>
                Password : <input required="required" name="password" type="password" placeholder="password" class="form-control mb-4"/>
                <input name="execute" type="submit" value="Set Admin Credentials" class="btn btn-primary form-control mb-4"/>
            </div>
        </form>
    </div>
        <script src="webjars/jquery/1.9.1/jquery.min.js"></script>
        <script src="webjars/bootstrap/5.0.2/js/bootstrap.min.js"></script>
</body>
