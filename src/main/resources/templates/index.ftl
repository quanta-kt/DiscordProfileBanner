<!DOCTYPE html>

<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Discord Profile Banner</title>
    <meta name="description" content="A profile banner for you to show off your Discord status!">

    <link rel="icon" href="https://discord.com/assets/847541504914fd33810e70a0ea73177e.ico">
    <link rel="stylesheet" href="static/css/index.css">
    <link rel="stylesheet" href="static/css/common.css">
    <script src="static/js/index.js"></script>
</head>

<body>
    <!-- Navigation Bar -->
    <header class="header">

        <div class="burger-container">
            <button class="burger" id="burger">
                <div></div>
                <div></div>
                <div></div>
            </button>
        </div>

        <div class="nav-container" id="nav-container">
            <nav class="nav-bar" id="nav-bar">
                <ul class="nav-links">
                    <li><a href="#home" class="navLinks">Home</a></li>
                    <li><a href="#features" class="navLinks">Features</a></li>
                    <li><a href="#leaderboard" class="navLinks">Leaderboard</a></li>
                </ul>
            </nav>

            <div class="nav-buttons" id="nav-buttons">
                <a href="https://discord.gg/3CvbmHruFS" target="_blank" class="button mr-half ml-half">
                    <span class="button-icon ic-discord"></span>Server
                </a>
                <a href="https://github.com/quanta-kt/DiscordProfileBanner" target="_blank" class="button mr-half ml-half">
                    <span class="button-icon ic-github"></span>GitHub
                </a>
            </div>
        </div>
    </header>

    <section class="container vertical-center" id="home">
        <h1 class="mt-1 has-text-center" id="heading">Discord Profile Banner</h1>
        <p class="mt-1 has-text-center">Get your own Banner now!</p>

        <div class="center-flex">
            <div class="banner-box">
                <input type="text" class="text-input has-text-center has-text-bold has-text-large has-shadow" id="input-username" placeholder="Discord Username/ID" tabindex="1">
                <div class="button-bar">
                    <button class="button" tabindex="2" id="btn-get-banner">Get my Banner!</button>
                    <button class="button" tabindex="3" id="btn-get-stats">View Statistics</button>
                </div>
                <p class="has-text-error has-text-center mt-half" id="error-text">Lol you noob!</p>
            </div>
        </div>

        <div class="center-flex" id="banner-view">
            <div class="banner-box">
                <p class="has-text-center">Here is your banner:</p>
                <img id="banner-img" class="banner-preview mt-half" src="">
                <input id="banner-url-input" readOnly="true" class="text-input mt-1 mr-half" type="text" value="">
            </div>
        </div>

        <p class="mt-1 has-text-center">In order for your banner to work, you need to be in our
            <a href="https://discord.gg/3CvbmHruFS" target="_blank" tabindex="4"> support server</a>
        </p>
    </section>

    <!-- Features -->
    <section class="mt-5 container" id="features">

        <div class="feature">
            <div class="feature-text">
                <h2 class="feature-title has-text-right">Real time Activity</h2>
                <p class="feature-description has-text-right">Your banner always shows your Discord activity and gets updated with it.</p>
            </div>

            <div class="feature-image-container">
                <img class="feature-image" id="image-1"
                    src="static/images/rem.png">
                <img class="feature-image" id="image-2"
                    src="static/images/rana.png">
            </div>
        </div>

        <div class="mt-5 feature-reversed">
            <div class="feature-text">
                <h2 class="feature-title">Customizable</h2>
                <p class="feature-description">Hide your Discord Tag, Custom Status, or set a Custom Background to the
                    banner as you like!</p>
            </div>

            <div class="feature-image-container">
                <img class="feature-image" id="image-3"
                    src="static/images/quanta.png">
                <img class="feature-image" id="image-4"
                    src="static/images/elaina.png">
            </div>
        </div>
    </section>

    <!-- Global Leaderboard table -->
    <section class="container mb-2 mt-3" id="leaderboard">
        <h2 class="has-text-center mb-1">Global leaderboard</h2>

        <div class="center-flex">
            <table class="leaderboard-table">
                <thead>
                    <tr>
                        <th>Rank</th>
                        <th>Username</th>
                        <th>Total visits</th>
                        <th>Unique visits</th>
                    </tr>
                </thead>
                <tbody>
                    <#list ranks as rank>
                    <tr>
                        <td>${rank?counter}</td>
                        <td>${rank.username}</td>
                        <td>${rank.totalVisits}</td>
                        <td>${rank.uniqueVisits}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </section>
</body>

</html>