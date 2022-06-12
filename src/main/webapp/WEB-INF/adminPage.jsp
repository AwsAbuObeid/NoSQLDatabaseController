<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<style>
.form-inline .form-control{
  display: inline-block;
  width: auto;
  vertical-align: middle;
}
</style>
<head>
    <title>NoSqlDB</title>
    <link href="webjars/bootstrap/5.0.2/css/bootstrap.min.css" rel="stylesheet">
</head>

<body class="container-fluid px-4 py-4">
<h1 class="text-center mb-4">NoSqlDB Config</h1>
<form method="post" class="position-absolute top-0 end-0 px-2 py-2"> <input name="execute" type="submit" value="ShutDown Database" class="btn btn-danger" /> </form>
   <hr class="divider mb-4">

    <div class="row mb-4"">
    <div class="container col-xl-6 " >
       <div class="card px-4 py-4">

            <h2 class=" h2 mx-auto">Database Users Config</h2>
            <table class="table table-striped align-middle">
                <thead>
                    <tr>
                        <th>Database</th>
                        <th>username</th>
                        <th>Role</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <form method="post">
                        <c:forEach items="${users}" var="item">
                            <tr>
                                <td >${item.database}</td>
                                <td >${item.username}</td>
                                <td >${item.role}</td>
                                <form  method="POST">
                                <td><input name="execute" type="submit" value="Delete User" class="btn btn-danger" /></td>
                                <input type="hidden" name="deletedUser" value=${item.username} />
                                </form>
                            </tr>
                        </c:forEach>
                    </form>
                </tbody>
            </table>
            </form>
            <hr class="divider">
            <form method="POST" class="form-inline">
                <h2>Add a User :</h2>
                <div class="form-group">
                    <input required="required" name="database" type="text" placeholder="database" class="form-control"/>
                    <input required="required" name="username" type="text" placeholder="username" class="form-control "/>
                    <input required="required" name="password" type="password" placeholder="password" class="form-control"/>
                    <select class="form-control" required="required" name="role">
                        <option value="ADMIN">ADMIN</option>
                        <option value="USER">USER</option>
                      </select>

                    <input name="execute" type="submit" value="Add User" class="btn btn-primary" style="margin-left:10px;"/>
                </div>
                <h5 style="color:red;">${bad}</h5>
            </form>
             <hr class="divider">
             <form method="POST">
                <input name="execute" type="submit" value="Change MasterAdmin Credentials" class="btn btn-primary"/>
             </form>
       </div>
    </div>

    <div class="container col-xl-6" >
        <div class="card px-4 py-4 " >

            <h2 class=" h2 mx-auto" >Running Read Servers in the Cluster</h2>
            <table class="table table-striped align-middle">
                <thead>
                    <tr>
                        <th>Node ID</th>
                        <th>Port Number</th>
                        <th>load</th>
                        <th>Availability</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <form method="post">
                        <c:forEach items="${nodes}" var="item">
                            <tr>
                                <td >${item.id}</td>
                                <td >${item.port}</td>
                                <td >${item.load}</td>
                                <td >${item.status}</td>
                                <form  method="POST">
                                <td><input name="execute" type="submit" value="Stop Node" class="btn btn-danger" /></td>
                                <input type="hidden" name="stoppedNode" value=${item.id} />
                                </form>
                            </tr>
                        </c:forEach>
                    </form>
                </tbody>
            </table>
            </form>
            <hr class="divider">
            <form method="POST" class="form-inline">
                <h2>Create New Node :</h2>
                <div class="form-group">
                    <input required="required" name="port" type="number" placeholder="Port" class="form-control"style="margin-left:10px;"/>
                    <input name="execute" type="submit" value="Create New Node" class="btn btn-primary" style="margin-left:10px;"/>
                </div>
                <h5 style="color:red;">${badnode}</h5>
            </form>
            </div>
        </div>
    </div>
    <div class="row">
    <div class="container col-xl-6" >
            <div class="card px-4 py-4 " >

                <h2 class=" h2 mx-auto" >Databases List</h2>
                <table class="table table-striped align-middle">
                    <thead>
                        <tr>
                            <th>Count</th>
                            <th>Database Name</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <form method="post">
                            <c:forEach items="${dbs}" var="item" varStatus="loop">
                                <tr>
                                    <td >${loop.index+1}</td>
                                    <td >${item}</td>
                                    <form  method="POST">
                                    <td><input name="execute" type="submit" value="Delete Database" class="btn btn-danger" /></td>
                                    <input type="hidden" name="deletedDB" value=${item} />
                                    </form>
                                </tr>
                            </c:forEach>
                        </form>
                    </tbody>
                </table>
                </form>
                <hr class="divider">
                <form method="POST" class="form-inline">
                    <h2>Create New Database :</h2>
                    <div class="form-group">
                        <input required="required" name="DatabaseName" type="text" placeholder="DatabaseName" class="form-control"style="margin-left:10px;"/>
                        <input name="execute" type="submit" value="Create New Database" class="btn btn-primary" style="margin-left:10px;"/>
                    </div>
                    <h5 style="color:red;">${badDB}</h5>
                </form>
                </div>
            </div>
        </div>
    </div>
        <script src="webjars/jquery/1.9.1/jquery.min.js"></script>
        <script src="webjars/bootstrap/5.0.2/js/bootstrap.min.js"></script>
</body>
