package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class Donation {
    @JsonField("animated_image")
    private String animatedImage;
    private String currency;
    private double amount;
    private String image;
    private Donation.DonationType type;
    private String name;

    public static enum DonationType {
        CASTERLABS_TEST,

        CAFFEINE_PROP,

        TWITCH_BITS,

        TROVO_SPELL,

        YOUTUBE_SUPER_CHAT,
        YOUTUBE_SUPER_STICKER,

        DLIVE_SUPER_CHAT,
        DLIVE_LEMON,

        ;
    }
}