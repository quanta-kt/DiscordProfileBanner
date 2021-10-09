window.onload = () => {
    const burger = document.getElementById("burger");
    const navContainer = document.getElementById("nav-container");
    const userNameInput = document.getElementById("input-username");
    const statsButton = document.getElementById("btn-get-stats");
    const bannerButton = document.getElementById("btn-get-banner");
    const errorText = document.getElementById("error-text");
    const bannerView = document.getElementById("banner-view");
    const bannerImg = document.getElementById("banner-img");
    const bannerUrlInput = document.getElementById("banner-url-input");

    // Hamburger toggle
    burger.onclick = () => {
        if (navContainer.classList.contains("mobile-visible")) {
            navContainer.classList.remove("mobile-visible");
        } else {
            navContainer.classList.add("mobile-visible");
        }
    };

    statsButton.onclick = async () => {
        const username = userNameInput.value;

        // Hide previous error
        errorText.style.display = 'none';
        // Disable button to avoid repetitive clicks
        statsButton.disabled = true;

        try {
            if (/^\d+$/.test(username)) {
                var userId = username;
            } else {
                var userId = await resolveUserId(username);
                if (!userId) {
                    errorText.innerText = "Can't find that user!";
                    errorText.style.display = 'inline';
                    return;
                }
            }

            document.location.href = `stats/${userId}`;
        } finally {
            statsButton.disabled = false;
        }
    };

    bannerButton.onclick = async () => {
        const username = userNameInput.value;

        // Hide previous error
        errorText.style.display = 'none';
        // Disable button to avoid repetitive clicks
        bannerButton.disabled = true;

        try {
            if (/^\d+$/.test(username)) {
                var userId = username;
            } else {
                var userId = await resolveUserId(username);
                if (!userId) {
                    errorText.innerText = "Can't find that user!";
                    errorText.style.display = 'inline';
                    return;
                }
            }
        } finally {
            bannerButton.disabled = false;
        }
        const url = String(new URL(`/banner/${userId}.png`, window.origin));
        bannerUrlInput.value = bannerImg.src = url;
        bannerView.style.display = 'inherit';

        bannerUrlInput.focus();
        bannerUrlInput.select();
    };
};

async function resolveUserId(username) {
    const url = `/api/resolve-user/${encodeURIComponent(username)}`;
    try {
        const resp = await fetch(url);
        const data = await resp.json()
        return data["id"];
    } catch(e) {
        return null;
    }
}
