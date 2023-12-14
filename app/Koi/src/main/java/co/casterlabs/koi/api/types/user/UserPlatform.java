package co.casterlabs.koi.api.types.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum UserPlatform {
    TWITCH("Twitch"),
    TROVO("Trovo"),
    YOUTUBE("YouTube"),
    DLIVE("DLive"),
    KICK("Kick"),
    TIKTOK("TikTok"),

    // Coming up.
    NOICE("Noice"),
    LIVESPACE("LiveSpace"),
    X("𝕏"),
    FACEBOOK_GAMING("Facebook Gaming"),

    // Graveyard. R.I.P.
    CAFFEINE("Caffeine"),
    GLIMESH("Glimesh"),
    BRIME("Brime"),
    THETA("Theta"),
    YOUNOW("YouNow"),

    // Other.
    CASTERLABS_SYSTEM("System"),
    CUSTOM_INTEGRATION("Custom Integration 🔧"), // For custom events for non-streaming platforms.
    ;

    private @Getter String str;

    @Override
    public String toString() {
        return this.str;
    }

}
