<!DOCTYPE html>

<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Discord Profile Banner</title>

    <link rel="icon" href="https://discord.com/assets/847541504914fd33810e70a0ea73177e.ico">
    <link rel="stylesheet" href="/static/css/style.css">
</head>

<body>


    <!-- Nvigation Bar -->
    <header id="header">
        <nav id=navBar>

            <div id="divLinks">
                <ul>
                    <li><a href="#home" class="navLinks">Home</a></li>
                    <li><a href="#features-1" class="navLinks">Features</a></li>
                    <li><a href="#leaderboard" class="navLinks">Leaderboard</a></li>
                </ul>
            </div>

            <div id="navButtonDiv">
                <a href="https://discord.gg/3CvbmHruFS" target="_blank" class="buttonLinks"
                    id="navButtonServer">Server</a>
                <a href="https://github.com/quanta-kt/DiscordProfileBanner" target="_blank" class="buttonLinks"
                    id="navButtonGithub">GitHub</a>
            </div>

        </nav>
    </header>
 

    <!-- Search Box -->
    <section class="container" id="home">

        <h1 id="heading">Discord Profile Banner</h1>
        <div class="textPara">
            Get your own Banner now!
        </div>

        <div class="searchBox">
            <input type="text" id="searchBar" placeholder="Enter your username" tabindex="1">
            <div id="divButton">
                <button tabindex="2" class="searchButton" id="getButton">
                    Get my Banner!
                </button>
                <button tabindex="3" class="searchButton" id="viewStats">
                    View Statistics
                </button>
            </div>
        </div>

        <div id="note">
            In order for your banner to work, you need to be in our <a href="https://discord.gg/3CvbmHruFS"
                target="_blank" tabindex="4">support server</a>
        </div>

    </section>


    <!-- Features -->
    <section class="container" id="features-1">
        <div class="imageCard" id="card-1">
            <img class="featureImage" id="image-1"
                src="/static/images/rem.png">
            <img class="featureImage" id="image-2"
                src="/static/images/rana.png">
        </div>

        <div class="featureDesc">
            <div class="titleForImage">Real time Activity</div>
            <div class="descForTitle">Your banner always shows your Discord activity and gets updated with it.</div>
        </div>
    </section>


    <section class="container" id="features-2">
        <div class="imageCard" id="card-2">
            <img class="featureImage" id="image-3"
                src="/static/images/quanta.png">
            <img class="featureImage" id="image-4"
                src="/static/images/elaina.png">
        </div>

        <div class="featureDesc">
            <div class="titleForImage leftAlign">Customizable</div>
            <div class="descForTitle leftAlign">Hide your Discord Tag, Custom Status, or set a Custom Background to the
                banner as you like!</div>
        </div>
    </section>


    <!-- Global Leaderboard table -->
    <section class="container" id="leaderboard">
        <h2 id="globalLB">Global leaderboard</h2>

        <div class="tableDiv">
            <table class="table">
                <thead>
                    <tr id="noHover">
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