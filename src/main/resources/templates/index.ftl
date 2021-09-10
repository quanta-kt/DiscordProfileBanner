<html>
    <body>
        <#list ranks as rank>
        <p>${rank?counter}</p>
        <p>${rank.username}</p>
        <p>${rank.totalVisits}</p>
        <p>${rank.uniqueVisits}</p>
        <hr>
        </#list>
    </body>
</html>