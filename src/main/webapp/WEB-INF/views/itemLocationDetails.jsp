<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<style>

    #map {
        height: 400px;
        width: 100%;
    }

    .radio-group label {
        overflow: hidden;
    } .radio-group input {
          /* This is on purpose for accessibility. Using display: hidden is evil.
          This makes things keyboard friendly right out tha box! */
          height: 1px;
          width: 1px;
          position: absolute;
          top: -20px;
      } .radio-group .not-active  {
            color: #3276b1;
            background-color: #fff;
        }


</style>
<html lang="en">

<head>
    <!-- Site Title -->
    <title>MBExplorer | ${item.location} </title>
    <!-- Bootstrap 4 core CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous">
    <!-- Custom Styles -->

    <!-- Fonts -->
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Rubik:400,500" rel="stylesheet">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

    <!-- Snackbar/Toast setup -->
    <link href="<c:url value="/resources/css/toaster.css"/>" rel="stylesheet">
    <script>
        $(document).ready(function() {
            var status = eval("${show}");
            if (status == true) {
                var x = document.getElementById("snackbar")
                x.className = "show";
                setTimeout(function () {
                    x.className = x.className.replace("show", "");
                }, 3000);
            }
            else{
                // do nothing
            }
        });
    </script>
</head>
<%
    session=request.getSession(false);
    if(session.getAttribute("email")==null)
    {
        response.sendRedirect(request.getContextPath() + "login");
    }
%>
<body>

<!-- navigation menu -->
<nav class="navbar doc-nav navbar-expand-lg navbar-inverse bg-primary">

    <div class="container">

        <div class="col-md-6">
            <img src="../resources/images/logo.png" alt="">
        </div>
        <div class="col-md-6">
            <div class="row">
                <a href="/"><i class="fa fa-home" style="font-size:25px;color:white;margin-left: 85px; margin-right: 10px;"></i></a>
                <ul class="nav navbar-nav">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" style="color:white"><span style="vertical-align: middle;">${email}</span>
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li><a href="/logout">Log out</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>

    </div><!-- container full-width -->
</nav>
<!-- / navigation menu -->

<br>
<br>

<!-- Body -->
<div class="container" id="mainContainer">
    <div class="card" id="container">
        <div class="card-block">
            <h4 class="card-title">${item.location}</h4>
            <h6 class="card-subtitle mb-2 text-muted">Location bookmark</h6>
            <c:if test="${folder.locked}"> <p style="color: mediumvioletred"><b> This item is locked due to its parent folder being currently locked.</b></p> </c:if>
            <div style="height:10px;font-size:1px;">&nbsp;</div>
            <p class="card-text"> <b>Path:</b> <br> ${item.path} </p>
            <p class="card-text"> <b>Location:</b> <br> ${item.location} </p>
            <p class="card-text"> <b>Latitude:</b> <br> ${item.latitude} </p>
            <p class="card-text"> <b>Longitude:</b> <br> ${item.longitude}  </p>
            <div id="map"></div>
        </div>
    </div>
    <br>
    <!-- Add management buttons -->
    <p style="display:inline">
        <c:choose>
        <c:when test="${folder.locked}">
            <button type="button" class="btn btn disabled">Edit item</button>
        </c:when>
        <c:otherwise>
            <button type="button" class="btn btn-warning" data-toggle="modal" data-target="#editItemForm">Edit item</button>
        </c:otherwise>

        </c:choose>
            <c:choose>
            <c:when test="${folder.locked}">
            <button type="button" class="btn btn disabled">Move item</button>&nbsp
            </c:when>
            <c:otherwise>
            <button type="button" class="btn btn-warning" data-toggle="modal" data-target="#moveItemForm">Move item</button>&nbsp
            </c:otherwise>
        </c:choose>
            <!-- Delete item form/button-->
    <form method="POST" action="../delete/itemLocation" style="display:inline">
        <input type="hidden" name="path" value="${item.path}"/>
    <c:choose>
        <c:when test="${folder.locked}">
            <button type="button" class="btn btn disabled">Delete item</button>&nbsp
        </c:when>
        <c:otherwise>
            <button type="submit" class="btn btn-danger">Delete item</button>&nbsp
        </c:otherwise>
    </c:choose>
    </form>
    <button type="button" class="btn btn-primary" onclick="window.location.href='../'" >Main page</button>

    <!-- Edit item form-->
    <div id="editItemForm" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Edit item</h4>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" method="POST" action="/edit/itemLocation">
                        <input type="hidden" name="path" value="${item.path}"/>
                        <p></p>
                        <p>Updated Location name: <br><input type="text" name="location" pattern="[a-zA-Z0-9\s]+" title="Alphanumerical characters only!" value="${item.location}" required/></p>
                        <p>Updated Latitude: <br><input type="text" name="latitude" pattern="-?\d{1,3}\.\d+" title="Enter a valid latitude! Must be in DD Coordinates format!" value="${item.latitude}" required/>
                        <p>Updated Longitude: <br><input type="text" name="longitude" pattern="-?\d{1,3}\.\d+" title="Enter a valid longitude! Must be in DD Coordinates format!" value="${item.longitude}" required/></p>
                        <p><button type="submit" class="btn btn-success">Edit</button></p>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Move item form-->
    <div id="moveItemForm" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Move item</h4>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <h6 class="card-subtitle mb-2 text-muted">Specify parent path, make sure the parent folders are already created.</h6>
                    <form class="form-horizontal" method="POST" action="/move/itemLocation">
                        <input type="hidden" name="path" value="${item.path}"/>
                        <p>Parent path to move to: <br><input type="text" name="newPath" placeholder="Parent|Subparent"required/></p>
                        <p><button type="submit" class="btn btn-success">Submit</button></p>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div id="snackbar">${message}</div>
</div>
<!-- / Body -->

<!-- Set up Google Maps marker and map -->
<script>
    function initMap() {
        var uluru = {lat: ${item.latitude}, lng: ${item.longitude}};
        var map = new google.maps.Map(document.getElementById('map'), {
            zoom: 10,
            center: uluru
        });
        var marker = new google.maps.Marker({
            position: uluru,
            map: map
        });
    }
</script>


<!-- Core JavaScript -->
<script src="<c:url value="/resources/js/jquery.min.js"/>"></script>
<script src="<c:url value="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.3/umd/popper.min.js"/>" integrity="sha384-vFJXuSJphROIrBnz7yo7oB41mKfc8JzQZiCq4NCceLEaO4IHwicKwpJf9c9IpFgh" crossorigin="anonymous"></script>
<script src="<c:url value="/resources/js/bootstrap.min.js"/>"></script>
<!-- / Core JavaScript -->

<!-- Google Maps API -->
<script async defer src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDGtX92C42iSrpnCOAKs8yDORAOK6MJ2dw&callback=initMap"
        type="text/javascript"></script>

<!-- Smooth Scrolling -->
<script src="<c:url value="/resources/js/jquery.easing.min.js"/>"></script>
<script src="<c:url value="/resources/js/smooth-scroll.js"/>"></script>
<!-- / Smooth Scrolling -->

<!-- Prism -->
<script src="<c:url value="/resources/js/prism.js"/>"></script>
<!-- / Prism -->

<!-- Bootstrap TreeView -->
<script src="<c:url value="/resources/js/bootstrap-treeview.js"/>"></script>
<link href="<c:url value="/resources/css/bootstrap-treeview.css"/>" rel="stylesheet">
<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
<!-- / Bootstrap TreeView -->

</div>
</div>
<p>&nbsp</p>
</body>

</html>