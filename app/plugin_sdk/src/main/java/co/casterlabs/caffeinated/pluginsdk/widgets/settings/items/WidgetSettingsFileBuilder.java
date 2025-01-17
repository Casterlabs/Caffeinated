package co.casterlabs.caffeinated.pluginsdk.widgets.settings.items;

import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItemType;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class WidgetSettingsFileBuilder {
    private @With String id;
    /**
     * @deprecated This has been deprecated in favor of the lang system.
     */
    @Deprecated
    private @With String name;

    private @With String[] allowedTypes;

    public WidgetSettingsFileBuilder() {
        this.id = null;
        this.name = null;
        this.allowedTypes = null;
    }

    @SuppressWarnings("deprecation")
    public WidgetSettingsItem build() {
        assert this.id != null : "id cannot be null";
        assert this.allowedTypes != null : "allowedTypes cannot be null";
        return new WidgetSettingsItem(
            this.id,
            this.name,
            WidgetSettingsItemType.FILE,
            new JsonObject()
                .put("allowed", Rson.DEFAULT.toJson(this.allowedTypes))
        );
    }

    public WidgetSettingsFileBuilder withAllowedTypes(String... allowed) {
        return new WidgetSettingsFileBuilder(this.id, this.name, allowed);
    }
}
