package co.casterlabs.caffeinated.app.ui;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class FontProvider {
    private static final List<String> fonts = new LinkedList<>();

    private static final List<Provider> providers = Arrays.asList(
        new JavaFontsProvider(),
        new MacOSFontsProvider(),
//        new WindowsFontsProvider(),
        new GoogleFontsProvider()
    );

    static {
        AsyncTask.create(() -> {
            Set<String> combined = new HashSet<>();
            for (Provider provider : providers) {
                try {
                    FastLogger.logStatic("Loading %s.", provider.getClass().getSimpleName());
                    combined.addAll(provider.listFonts());
                } catch (IOException e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst loading %s:\n%s", provider.getClass().getSimpleName(), e);
                }
            }
            fonts.addAll(combined);
            Collections.sort(fonts);
        });
    }

    public static List<String> listFonts() {
        return Collections.unmodifiableList(fonts);
    }

}

interface Provider {

    public List<String> listFonts() throws IOException;

}

class GoogleFontsProvider implements Provider {
    private static final String GOOGLE_FONTS_API_KEY = "AIzaSyBuFeOYplWvsOlgbPeW8OfPUejzzzTCITM";

    @Override
    public List<String> listFonts() throws IOException {
        List<String> fonts = new LinkedList<>();

        JsonObject response = Rson.DEFAULT.fromJson(
            WebUtil.sendHttpRequest(new Request.Builder().url("https://www.googleapis.com/webfonts/v1/webfonts?sort=popularity&key=" + GOOGLE_FONTS_API_KEY)),
            JsonObject.class
        );

        if (response.containsKey("items")) {
            JsonArray items = response.getArray("items");

            for (JsonElement e : items) {
                JsonObject font = e.getAsObject();

                fonts.add(font.getString("family").trim());
            }
        }

        return fonts;
    }
}

class MacOSFontsProvider implements Provider {
    private static final String macCmd = "system_profiler SPFontsDataType | grep 'Full Name:'";

    @Override
    public List<String> listFonts() throws IOException {
        if (Platform.osDistribution != OSDistribution.MACOS) return Collections.emptyList();

        List<String> fonts = new LinkedList<>();

        String[] list = StreamUtil.toString(
            Runtime
                .getRuntime()
                .exec(new String[] {
                        "bash",
                        "-c",
                        macCmd
                })
                .getInputStream(),
            Charset.defaultCharset()
        )
            .split("\n");

        for (String item : list) {
            String name = item.split("Full Name:")[1].trim();

            fonts.add(name);
        }

        return fonts;
    }
}

//class WindowsFontsProvider implements Provider {
//    private static final String powershellCmd = ""
//        + "[void] [System.Reflection.Assembly]::LoadWithPartialName('System.Drawing'); "
//        + "ConvertTo-Json (New-Object System.Drawing.Text.InstalledFontCollection).Families";
//
//    @Override
//    public List<String> listFonts() throws IOException {
//        List<String> fonts = new LinkedList<>();
//
//        String json = IOUtil.readInputStreamString(
//            Runtime
//                .getRuntime()
//                .exec(new String[] {
//                        "powershell",
//                        "-command",
//                        powershellCmd
//                })
//                .getInputStream(),
//            StandardCharsets.UTF_8
//        );
//
//        JsonArray array = Rson.DEFAULT.fromJson(json, JsonArray.class);
//        for (JsonElement e : array) {
//            String name = e
//                .getAsObject()
//                .getString("Name")
//                .trim();
//
//            fonts.add(name);
//        }
//
//        return fonts;
//    }
//}

class JavaFontsProvider implements Provider {
    @Override
    public List<String> listFonts() {
        return Arrays.asList(
            GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()
        );
    }
}
