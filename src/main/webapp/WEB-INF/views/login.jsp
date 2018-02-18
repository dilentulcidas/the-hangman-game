<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%
        session=request.getSession(false);
        if(session.getAttribute("email")==null)
        {
            // do nothing
        }
        else{
            // already signed in
            response.sendRedirect(request.getContextPath() + "");
        }
    %>
    <style>
        body {
            height: 100%;
            overflow-y: hidden;
            overflow-x: hidden;
            background-image: url("http://wallpaperesque.com/wp-content/uploads/plixpapers1504/the_coolest_book_wallpaper_background_27381.jpg");
            background-color: #000000;
            background-position: center;
            background-repeat: no-repeat;
            background-size: cover;
            -webkit-background-size: cover;
            -moz-background-size: cover;
            -o-background-size: cover;
            height:100%;
        }

        #footer {
            position: fixed;
            bottom: 10px;
            width: 100%;
            text-align: center;
        }

        .firebaseui-id-page-callback { background: none !important; }

    </style>

    <title>Login - MBExplorer</title>

    <!-- Snackbar/Toast setup -->
    <link href="<c:url value="/resources/css/toaster.css"/>" rel="stylesheet">

    <!-- Firebase Auth Management -->
    <script src="https://www.gstatic.com/firebasejs/4.10.0/firebase.js"></script>
    <script src="https://cdn.firebase.com/libs/firebaseui/2.6.1/firebaseui.js"></script>
    <link type="text/css" rel="stylesheet" href="https://cdn.firebase.com/libs/firebaseui/2.6.1/firebaseui.css" />
    <script src="<c:url value="/resources/js/firebaseconfig.js"/>"></script>
    <script type="text/javascript">

        //// LOGIN SETUP
        // FirebaseUI config.
        var uiConfig = {
            signInSuccessUrl: '/',
            credentialHelper: firebaseui.auth.CredentialHelper.ACCOUNT_CHOOSER_COM,
            // Will use popup for IDP Providers sign-in flow instead of the default, redirect.
            signInFlow: 'popup',
            signInOptions: [
                // Leave the lines as is for the providers you want to offer your users.
                firebase.auth.GoogleAuthProvider.PROVIDER_ID,
            ],
            // Terms of service url.
            tosUrl: 'https://pastebin.com/raw/eQhFXbzr',
            callbacks: {
                signInSuccess: function (user, credential, redirectUrl) {
                    // Process result. This will not trigger on merge conflicts.
                    // On success redirect to signInSuccessUrl.

                    // hide google login part
                    ///document.getElementById('firebaseui-auth-container').style.visibility="hidden";

                    //// send parameters to server
                    var userEmail = user.email;

                    // send post request to server
                    var to_return;

                    function ajax() {
                        // NOTE:  This function must return the value
                        //        from calling the $.ajax() method.
                        return $.ajax({
                            type: "POST",
                            contentType : "application/json",
                            url: '/',
                            async: false,
                            data: JSON.stringify({"email": userEmail}),
                            success : function(data) {
                                console.log("SUCCESS: ", data);
                                to_return = data;
                            },
                            error : function(e) {
                                console.log("ERROR: ", e);
                            },
                            done : function(e) {
                                console.log("DONE");
                            }
                        });
                    }

                    // do the ajax
                    ajax();

                    console.log("To return value: "+to_return);

                    // if it failed return false but before that put login btn visible again so user can try again
                    if (!to_return){
                        document.getElementById("firebaseui-auth-container").style.visibility = "visible";
                    }
                    return to_return;
                },
            }
        };

        // Initialize the FirebaseUI Widget using Firebase.
        var ui = new firebaseui.auth.AuthUI(firebase.auth());
        // The start method will wait until the DOM is loaded.
        ui.start('#firebaseui-auth-container', uiConfig);

    </script>

    <!-- Bootstrap 4 core CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous">

    <!-- Custom Styles -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
</head>
<body>

<!-- Body -->
<div class="h-100 row align-items-center" id="contents">
    <div class="col">
        <center><img src="../resources/images/loginlogo.png" alt="" align="middle"></center>
        <div id="firebaseui-auth-container"></div>
        <div id="footer"><h10 style="color:white;font-size:15px;font-family: monospace;">Made by Dilen Tulcidas, ©2017 </h10></div>
    </div>
    <div id="snackbar">${message}</div>
</div>
<!-- /Body -->

<!-- Core JavaScript -->
<script src="<c:url value="/resources/js/jquery.min.js"/>"></script>
<script src="<c:url value="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.3/umd/popper.min.js"/>" integrity="sha384-vFJXuSJphROIrBnz7yo7oB41mKfc8JzQZiCq4NCceLEaO4IHwicKwpJf9c9IpFgh" crossorigin="anonymous"></script>
<script src="<c:url value="/resources/js/bootstrap.min.js"/>"></script>
<!-- / Core JavaScript -->

<!-- Smooth Scrolling -->
<script src="<c:url value="/resources/js/jquery.easing.min.js"/>"></script>
<script src="<c:url value="/resources/js/smooth-scroll.js"/>"></script>
<!-- / Smooth Scrolling -->
</body>
</html>
