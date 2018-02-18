<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="th" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<style>

    table{
        border-collapse: separate !important;
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
    <title>MBExplorer</title>
    <!-- Bootstrap 4 core CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous">
    <!-- Custom Styles -->

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

    <!-- Fonts -->
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Rubik:400,500" rel="stylesheet">

    <!-- Google charts -->
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

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

    <h1>Bookmarks</h1>

    <br>

    <p class="card-subtitle mb-2 text-muted">To add a bookmark or a subfolder, click on one of the folders and manage from there. <br>To check an item's details simply click on the respective item, marked by its type.</p>
    <br>

    <!-- TreeView containing hierarchy of bookmarks -->
    <c:choose>
        <c:when test="${isempty}">
            <div>
                <h6 class="card-subtitle mb-2 text-muted">There are no bookmarks to display. Start by creating a folder and store bookmarks inside it.</h6>
            </div>
        </c:when>
        <c:otherwise>
            <div id="treeview-searchable" class=""></div>
        </c:otherwise>
    </c:choose>

    <h3>&nbsp</h3>

    <!-- Contains buttons to add bookmark/folder -->
    <div id="optionsContainer" style="">
        <!-- Add folder Button -->
        <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#addFolderForm">Add folder at root</button>&nbsp&nbsp
        <c:choose>
            <c:when test="${isempty}">
                <button type="button" class="btn disabled">Search tree</button>
            </c:when>
            <c:otherwise>
                <button type="button" class="btn btn-info" data-toggle="collapse" data-parent="#optionsContainer" data-target="#searchForm">Search tree</button>
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${isempty}">
                <button type="button" class="btn btn disabled">Tree visualisation</button>
            </c:when>
            <c:otherwise>
                <button type="button" class="btn btn-success" data-toggle="modal" data-target="#treeVisuals">Tree visualisation</button>
            </c:otherwise>
        </c:choose>
        <!-- collapsible search  -->
        <div style="height:5px;font-size:1px;">&nbsp;</div>
        <div id="searchForm" class="collapse">
            <input class="form-control" type="input" id="input-search" placeholder="Type to search..." value="">
        </div>

        <div id="addFolderForm" class="modal fade" role="dialog">
            <div class="modal-dialog">

                <!-- Modal content-->
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title">Add Folder at root</h4>
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" method="POST" action="/create/folder">
                            <input type="hidden" name="path" value="none"/>
                            <p></p>
                            <p>Folder name: <br><input type="text" name="name" name="title" pattern="[a-zA-Z0-9\s]+" title="Alphanumerical characters only!" required/></p>
                            <p><button type="submit" class="btn btn-success">Add</button></p>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div id="treeVisuals" class="modal fade" role="dialog">
            <div class="modal-dialog">

                <!-- Modal content-->
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title">Tree Visualisation</h4>
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                    </div>
                    <div class="modal-body">
                        <h5>Treemap</h5>
                        <hr>
                        <div style="height:5px;font-size:1px;">&nbsp;</div>
                        <div id="treemap"></div>
                        <div style="height:10px;font-size:1px;">&nbsp;</div>
                        <h5>Organisation chart</h5>
                        <hr>
                        <div style="height:5px;font-size:1px;">&nbsp;</div>
                        <div id="orgchart"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- / Contains buttons to add bookmark/folder -->

    <div id="snackbar">${message}</div>
</div>
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

        var data = ${completetree};

        if(data == '['){
            data = [{text:"No bookmarks yet! Start by creating a folder and store bookmarks inside it."}];
            return data;
        }
        else{
            return data;
        }
    }


    var $searchableTree = $('#treeview-searchable').treeview({

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

    <!-- Google treemap draw -->
    google.charts.load('current', {'packages':['treemap']});
    google.charts.setOnLoadCallback(drawTreemapChart);

    function drawTreemapChart() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'ID');
        data.addColumn('string', 'Parent');
        data.addColumn('number', 'Number Of Lines');
        data.addRows(${treemap});

        var tree = new google.visualization.TreeMap(document.getElementById('treemap'));

        var options = {
            highlightOnMouseOver: false,
            maxDepth: 1,
            maxPostDepth: 2,
            minHighlightColor: '#8c6bb1',
            midHighlightColor: '#9ebcda',
            maxHighlightColor: '#edf8fb',
            minColor: '#009688',
            midColor: '#f7f7f7',
            maxColor: '#ee8100',
            headerHeight: 15,
            showScale: false,
            height: 200,
            useWeightedAverageForAggregation: true
        };

        tree.draw(data, options);
    }

    <!-- Google organizational chart draw -->
    google.charts.load('current', {packages:["orgchart"]});
    google.charts.setOnLoadCallback(drawChart);

    function drawChart() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Name');
        data.addColumn('string', 'Parent');

        // For each orgchart box, provide the name, manager, and tooltip to show.
        data.addRows(${chart});

        // Create the chart.
        var chart = new google.visualization.OrgChart(document.getElementById('orgchart'));
        // Draw the chart, setting the allowHtml option to true for the tooltips.
        chart.draw(data, {allowHtml:true});
    }

</script>
</div>
</div>

</body>

</html>