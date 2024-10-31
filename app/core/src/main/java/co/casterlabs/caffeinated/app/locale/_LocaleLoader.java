package co.casterlabs.caffeinated.app.locale;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.localization.LocaleProvider;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class _LocaleLoader {
    private static final LocaleProvider FALLBACK = new en_US().get();

    public static LocaleProvider load(String locale) {
        LocaleProvider provider;

        String[] parts = locale.replace('-', '_').split("_");
        String fileToLoad = parts[0].toLowerCase() + "_" + parts[1].toUpperCase();

        if (fileToLoad.equals("en_US")) {
            return FALLBACK;
        }

        // Load using reflection.
        try {
            @SuppressWarnings("unchecked")
            Class<Supplier<LocaleProvider>> providerClass = (Class<Supplier<LocaleProvider>>) Class.forName("co.casterlabs.caffeinated.app.locale." + fileToLoad);
            provider = providerClass.getConstructor().newInstance().get();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return FALLBACK;
        }

        return new LocaleProvider() {
            @Override
            protected @Nullable String process0(@NonNull String key, @NonNull LocaleProvider externalLookup, @NonNull Map<String, String> knownPlaceholders, @NonNull List<String> knownComponents) {
                String result = provider.process(key, externalLookup, knownPlaceholders, knownComponents);

                if (result == null) {
                    FastLogger.logStatic(LogLevel.WARNING, "Unable to find locale key: %s", key);
                    return FALLBACK.process(key, externalLookup, knownPlaceholders, knownComponents);
                } else {
                    return result;
                }
            }
        };
    }

}
