package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public enum Locale {
    // @formatter:off
    DA_DK    (WritingDirection.LEFT_TO_RIGHT, "🇩🇰", "Dansk"),
    DE_DE    (WritingDirection.LEFT_TO_RIGHT, "🇩🇪", "Deutsch"),
    EN_US    (WritingDirection.LEFT_TO_RIGHT, "🇺🇸", "English"),
    ES_ES    (WritingDirection.LEFT_TO_RIGHT, "🇪🇸", "Español"),
    FR_FR    (WritingDirection.LEFT_TO_RIGHT, "🇫🇷", "Français"),
    ID_ID    (WritingDirection.LEFT_TO_RIGHT, "🇮🇩", "Bahasa Indonesia"),
//    LV_LV    (WritingDirection.LEFT_TO_RIGHT, "🇱🇻", "Latviešu"),
//    PT_BR    (WritingDirection.LEFT_TO_RIGHT, "🇧🇷", "Português (Brasil)"),
    RU_RU    (WritingDirection.LEFT_TO_RIGHT, "🇷🇺", "Русский"),
    TR_TR    (WritingDirection.LEFT_TO_RIGHT, "🇹🇷", "Türkçe"),

    // @formatter:on
    ;

    public final WritingDirection direction;
    public final String emoji;
    public final String name;

    public String getCode() {
        return this.name();
    }

    public static enum WritingDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put("direction", this.direction.name())
            .put("emoji", this.emoji)
            .put("name", this.name);
    }

}
