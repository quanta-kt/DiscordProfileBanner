<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="/static/css/common.css">
    <link rel="stylesheet" href="/static/css/user-stats.css">
    <script src="/static/js/user-stats.js"></script>
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
                <li><a href="/" class="navLinks">Home</a></li>
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

<section class="container mt-5">
    <div class="container user-details">
        <img class="user-avatar" src="${profilePicture}">
        <div class="user-text">
            <p class="username has-text-primary">${username}</p>
            <p><strong class="has-text-primary mt-half">Total visits:</strong> ${stat.totalVisits}</p>
            <p><strong class="has-text-primary">Unique visits:</strong> ${stat.uniqueVisits}</p>
        </div>
    </div> 
</section>

<section class="container mt-2" id="top-countries-container">
    <h2 class="has-text-center">Visits from top countries</h2>

    <div class="top-countries">
        <#list stat.topCountries as country, visits>
            <div class="country-item">
                <div class="country-flag">${country!"Unknown"}</div>
                <div class="country-visits">${visits}</div>
            </div>
        </#list>
    </div>
</section>

</body>
</html>