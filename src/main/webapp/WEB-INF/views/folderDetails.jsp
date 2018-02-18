<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<style>

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
    <title>MBExplorer | ${folder.name} </title>
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
<div class="container">
    <div class="card" id="container">
        <div class="card-block">
            <h4 class="card-title">${folder.name}</h4>
            <h6 class="card-subtitle mb-2 text-muted">Folder</h6>
            <c:if test="${folder.locked}"> <p style="color: mediumvioletred"><b> This folder is locked, you are limited to only view its content or update its locked status.</b></p> </c:if>
            <div style="height:10px;font-size:1px;">&nbsp;</div>
            <p class="card-text"> <b>Path:</b> <br> ${folder.path} </>
            <p class="card-text"> <b>Locked:</b> <br> ${folder.locked} </>
            <p class="card-text"> <b>Content:</b> <br> <!-- TreeView containing hierarchy of bookmarks --> <div id="subfoldertreeview" class=""></div>
        </div>
    </div>
    <br>
    <!-- Add management buttons -->
    <!-- Add bookmark Button -->
    <!-- <button type="button" class="btn btn-primary" data-toggle="collapse" data-parent="#optionsContainer" data-target="#addBookmarkForm">Add bookmark</button> -->

    <div style="display:inline">
        <!-- Add bookmark Button -->
        <c:choose>
            <c:when test="${folder.locked}">
                <button type="button" class="btn btn disabled">Add bookmark</button>
            </c:when>
            <c:otherwise>
                <button type="button" class="btn btn-success" data-toggle="modal" data-target="#addBookmarkModal">Add bookmark</button>
            </c:otherwise>
        </c:choose>
        <!-- / Add bookmark Button -->

        <!-- Add subfolder Button -->
        <c:choose>
            <c:when test="${folder.locked}">
                <button type="button" class="btn btn disabled">Add subfolder</button>
            </c:when>
            <c:otherwise>
                <button type="button" class="btn btn-success" data-toggle="modal" data-target="#addSubfolderModal">Add subfolder</button>
            </c:otherwise>
        </c:choose>
        <!-- / Add bookmark Button -->

        <!-- Add Update Folder Button -->
        &nbsp&nbsp&nbsp<button type="button" class="btn btn-warning" data-toggle="modal" data-target="#updateFolderModal">Update folder</button>
        <!-- / Add Rename Folder Button -->

        <!-- Add Move Folder Button -->
        <c:choose>
            <c:when test="${folder.locked == true || isroot == true}">
                &nbsp&nbsp&nbsp<button type="button" class="btn btn disabled" title="You can't move the root folder!">Move folder</button>
            </c:when>
            <c:otherwise>
                <button type="button" class="btn btn-warning" data-toggle="modal" data-target="#moveFolderModal">Move folder</button>
            </c:otherwise>
        </c:choose>
        <!-- / Add Rename Folder Button -->

        <!-- Add Delete Folder Button -->
        <c:choose>
            <c:when test="${folder.locked}">
                &nbsp&nbsp&nbsp<button type="submit" class="btn btn disabled">Delete folder</button>
            </c:when>
            <c:otherwise>
                <form method="POST" action="../delete/folder" style="display:inline">
                    <input type="hidden" name="path" value="${folder.path}"/>
                    &nbsp&nbsp&nbsp<button type="submit" class="btn btn-danger">Delete folder</button>
                </form>
            </c:otherwise>
        </c:choose>
        <!-- / Add Delete folder Button -->

    </div>

    <hr>
    <div style="display:inline">
        <button type="button" class="btn btn-info" data-toggle="collapse" data-target="#searchForm">Search tree</button>


        <!-- Go back Button -->
        <button type="button" class="btn btn-primary" onclick="window.location.href='../'" >Main page</button>
        <!-- / Go back Button -->

        <!-- collapsible search  -->
        <div style="height:5px;font-size:1px;">&nbsp;</div>
        <div id="searchForm" class="collapse">
            <input type="input" class="form-control" id="input-search" placeholder="Type to search..." value="">
        </div>
    </div> </p></p>

    <!-- Modal Forms -->

    <!-- Update folder Modal -->
    <div id="updateFolderModal" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Update folder</h4>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" method="POST" action="/edit/folder">
                        <input type="hidden" name="path" value="${folder.path}"/>
                        <p></p>
                            <c:choose>
                            <c:when test="${folder.locked}">
                            <input type="hidden" name="name" value="${folder.name}" required/></p>
                            </c:when>
                            <c:otherwise>
                                <p>Updated folder name:<br>
                            <input type="text" name="name" pattern="[a-zA-Z0-9\s]+" title="Alphanumerical characters only!" value="${folder.name}" required/></p>
                        </c:otherwise>
                        </c:choose>
                        <p>
                            Locked:<br>
                            <label class="custom-control custom-radio">
                                <input id="true" name="locked" type="radio" class="custom-control-input" value="true" required>
                                <span class="custom-control-indicator"></span>
                                <span class="custom-control-description">Yes</span>
                            </label>
                            <label class="custom-control custom-radio">
                                <input id="false" name="locked" type="radio" class="custom-control-input" value="false" required>
                                <span class="custom-control-indicator"></span>
                                <span class="custom-control-description">No</span>
                            </label>
                        </p>
                        <p><button type="submit" class="btn btn-success">Update</button></p>
                    </form>
                </div>
            </div>

        </div>
    </div>

    <!-- Add Bookmark Modal -->
    <div id="addBookmarkModal" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Add bookmark</h4>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <!-- Radiobutton selection -->
                    <label class="custom-control custom-radio">
                        <input id="link" name="bookmarktype" type="radio" value="link" class="custom-control-input">
                        <span class="custom-control-indicator"></span>
                        <span class="custom-control-description">Link</span>
                    </label>
                    <label class="custom-control custom-radio">
                        <input id="text" name="bookmarktype" type="radio" value="text" class="custom-control-input">
                        <span class="custom-control-indicator"></span>
                        <span class="custom-control-description">Text</span>
                    </label>
                    <label class="custom-control custom-radio">
                        <input id="location" name="bookmarktype" type="radio" value="location" class="custom-control-input">
                        <span class="custom-control-indicator"></span>
                        <span class="custom-control-description">Location</span>
                    </label>

                    <!-- Add link form -->
                    <div id="ifLink">
                        <form class="form-horizontal" method="POST" action="/create/itemLink">
                            <input type="hidden" name="path" value="${folder.path}"/>
                            <p></p>
                            <p>Link title:<br>
                                <input type="text" name="title" pattern="[a-zA-Z0-9\s]+" title="Alphanumerical characters only!" required/></p>
                            <p>Url:<br>
                                <input type="text" name="url" required/></p>
                            <p><button type="submit" class="btn btn-success">Add</button></p>
                        </form>
                    </div>

                    <!-- Add text form -->
                    <div id="ifText" >
                        <form class="form-horizontal" id="ifTextForm" method="POST" action="/create/itemText">
                            <input type="hidden" name="path" value="${folder.path}"/>
                            <p></p>
                            <p>Text title:<br>
                                <input type="text" name="title" pattern="[a-zA-Z0-9\s]+" title="Alphanumerical characters only!" required/></p>
                            <p>Content:<br>
                                <textarea class="form-control" rows="5" id="comment" form="ifTextForm" name="content" required></textarea>
                            <p><button type="submit" class="btn btn-success">Add</button></p>
                        </form>
                    </div>

                    <!-- Add location form -->
                    <div id="ifLocation">
                        <form class="form-horizontal" method="POST" action="/create/itemLocation">
                            <input type="hidden" name="path" value="${folder.path}"/>
                            <p></p>
                            <p>Location name: <br><input type="text" name="location" pattern="[a-zA-Z0-9\s]+" title="Alphanumerical characters only!" required/></p>
                            <p>Latitude: <br><input type="text" name="latitude" placeholder="DD Coordinates format" pattern="-?\d{1,3}\.\d+" title="Enter a valid latitude! Must be in DD Coordinates format!" required/>
                            <p>Longitude: <br><input type="text" name="longitude" placeholder="DD Coordinates format" pattern="-?\d{1,3}\.\d+" title="Enter a valid longitude! Must be in DD Coordinates format!" required/></p>
                            <p><button type="submit" class="btn btn-success">Add</button></p>
                        </form>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <!-- Add Subfolder Modal -->
    <div id="addSubfolderModal" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Add subfolder</h4>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" method="POST" action="/create/folder">
                        <input type="hidden" name="path" value="${folder.path}"/>
                        <p></p>
                        <p>Subfolder name: <br><input type="text" pattern="[a-zA-Z0-9\s]+" title="Alphanumerical characters only!" name="name" required/></p>
                        <p><button type="submit" class="btn btn-success">Add</button></p>
                    </form>
                </div>
            </div>

        </div>
    </div>

    <!-- Move Folder Modal -->
    <div id="moveFolderModal" class="modal fade" role="dialog">
        <div class="modal-dialog">

            <!-- Modal content-->
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Move folder</h4>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <h6 class="card-subtitle mb-2 text-muted">Specify parent path, make sure the parent folders are already created.</h6>
                    <form class="form-horizontal" method="POST" action="/move/folder">
                        <input type="hidden" name="path" value="${folder.path}"/>
                        <p>Parent path to move to: <br><input type="text" name="newPath" placeholder="Parent|Subparent"required/></p>
                        <p><button type="submit" class="btn btn-success">Submit</button></p>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="snackbar">${message}</div>
<!-- / Body -->


<!-- Core JavaScript -->
<script src="<c:url value="/resources/js/jquery.min.js"/>"></script>
<script src="<c:url value="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.3/umd/popper.min.js"/>" integrity="sha384-vFJXuSJphROIrBnz7yo7oB41mKfc8JzQZiCq4NCceLEaO4IHwicKwpJf9c9IpFgh" crossorigin="anonymous"></script>
<script src="<c:url value="/resources/js/bootstrap.min.js"/>"></script>
<!-- / Core JavaScript -->

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

<script>

    <!-- Bootstrap tree view setup -->
    function getTree() {
        // Some logic to retrieve, or generate tree structure

        var data = [${subfoldertree}];


        if(data == '['){
            data = [{text:"No subfolders and no bookmarks inside this directory!"}];
            return data;
        }
        else{
            return data;
        }
    }


    var $searchableTree = $('#subfoldertreeview').treeview({

        selectedBackColor:"#0275D8",
        data: getTree(),
        enableLinks: true,
        levels: 10000
    });

    var search = function(e) {
        var pattern = $('#input-search').val();
        var options = {
            ignoreCase: true,
            exactMatch: false,
            revealResults: false
        };
        var results = $searchableTree.treeview('search', [ pattern, options ]);

        var output = '<p>' + results.length + ' matches found</p>';
        $.each(results, function (index, result) {
            output += '<p>- ' + result.text + '</p>';
        });
        $('#search-output').html(output);
    }

    $('#btn-search').on('click', search);
    $('#input-search').on('keyup', search);

    $('#btn-clear-search').on('click', function (e) {
        $searchableTree.treeview('clearSearch');
        $('#input-search').val('');
        $('#search-output').html('');
    });

    <!-- RadioButton selection and respective form display -->

    var linkCollapsible = document.getElementById("ifLink");
    var textCollapsible =  document.getElementById("ifText");
    var locationCollapsible =  document.getElementById("ifLocation");

    <!-- set default as forms not showing at all, until the user clicks on one of the radiobuttons -->
    linkCollapsible.style.display = 'none';
    textCollapsible.style.display = 'none';
    locationCollapsible.style.display = 'none';

    $(document).ready(function(){
        $('#link').change(function() {
            if(this.checked) {
                $("#ifLink").show();
                $("#ifText").hide();
                $("#ifLocation").hide();
            }
        });
        $('#text').change(function() {
            if(this.checked) {
                $("#ifLink").hide();
                $("#ifText").show();
                $("#ifLocation").hide();
            }
        });
        $('#location').change(function() {
            if(this.checked) {
                $("#ifLink").hide();
                $("#ifText").hide();
                $("#ifLocation").show();
            }
        });

    }
    );
</script>

</div>
</div>
<p>&nbsp</p>
</body>

</html>