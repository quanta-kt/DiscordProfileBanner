# DiscordProfileBanner
This tool generates a Discord profile banner image in realtime. I wrote it for use in [my AniList profile](https://anilist.co/user/Quanta/).

An example in action:

![generated image](https://discord-prof.herokuapp.com/721012149933310029.png?)

## How does it work?
- A Discord bot connects to the Discord gatewaty.
- A Ktor server accepts requests at path `/{id}.png` where `id` is the Discord user ID.
- On each request, bot fetches requested user's profile and activity status and an image is generated which contains all of this info.

## Licence
This project is Licensed under MIT. Please check the [LICENSE](https://github.com/quanta-kt/DiscordProfileBanner/blob/master/LICENSE) file for details.
