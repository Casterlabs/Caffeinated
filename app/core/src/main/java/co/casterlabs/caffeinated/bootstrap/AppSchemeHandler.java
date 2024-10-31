package co.casterlabs.caffeinated.bootstrap;

import java.net.URI;

import app.saucer.scheme.SaucerSchemeHandler;
import app.saucer.scheme.SaucerSchemeRequest;
import app.saucer.scheme.SaucerSchemeResponse;
import app.saucer.scheme.SaucerSchemeResponse.SaucerRequestError;
import app.saucer.utils.SaucerStash;
import co.casterlabs.caffeinated.app.Resources;
import co.casterlabs.caffeinated.util.MimeTypes;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppSchemeHandler implements SaucerSchemeHandler {
    public static final AppSchemeHandler INSTANCE = new AppSchemeHandler();

    @SneakyThrows
    @Override
    public SaucerSchemeResponse handle(SaucerSchemeRequest request) throws Throwable {
        String path = URI.create(
            request.url()
                .replace('\\', '/')
                .replace("%5c", "/")
                .replace("%5C", "/")
        ).getPath();

        if (path.startsWith("/$caffeinated-sdk-root$")) {
            path = path.substring("/$caffeinated-sdk-root$".length());
        }

        if (path.isEmpty()) {
            path = "/index.html";
        } else {
            // Append `index.html` to the end when required.
            if (!path.contains(".")) {
                if (path.endsWith("/")) {
                    path += "index.html";
                } else {
                    path += ".html";
                }
            }
        }

        try {
            byte[] content = Resources.bytes("co/casterlabs/caffeinated/app/ui/html" + path);
            String mimeType = "application/octet-stream";

            String[] split = path.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", request.url(), path, mimeType);

            return SaucerSchemeResponse.success(SaucerStash.of(content), mimeType);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s\n%s", request.url(), path, e);

            return SaucerSchemeResponse.error(SaucerRequestError.NOT_FOUND);
        }
    }

}
