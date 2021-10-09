window.onload = () => {
    const burger = document.getElementById("burger");
    const navContainer = document.getElementById("nav-container");

    // Hamburger toggle
    burger.onclick = () => {
        if (navContainer.classList.contains("mobile-visible")) {
            navContainer.classList.remove("mobile-visible");
        } else {
            navContainer.classList.add("mobile-visible");
        }
    };

    // Replace country code to flag emojis
    const flagElements = document.getElementsByClassName("country-flag");
    for (let i = 0; i < flagElements.length; i++) {
        const element = flagElements[i];
        if (element.textContent.toLowerCase() != "unknown")
            element.textContent = getFlagEmoji(element.textContent);
    }
};

function getFlagEmoji(countryCode) {
    const codePoints = countryCode
        .toUpperCase()
        .split('')
        .map(char =>  127397 + char.charCodeAt());
    return String.fromCodePoint(...codePoints);
}
