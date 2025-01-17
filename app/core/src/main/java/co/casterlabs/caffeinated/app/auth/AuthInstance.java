package co.casterlabs.caffeinated.app.auth;

import java.io.Closeable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.queue.ExecutionQueue;
import co.casterlabs.commons.async.queue.SyncExecutionQueue;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.KoiConnection;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventUtil;
import co.casterlabs.koi.api.listener.KoiLifeCycleHandler;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.RoomId;
import co.casterlabs.koi.api.types.events.RoomstateEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.events.ViewerListEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AuthInstance implements KoiLifeCycleHandler, Closeable {
    private static ExecutionQueue reconnectQueue = new SyncExecutionQueue();

    private @JsonField @Getter String tokenId;
    private @JsonField @Getter String token;

    private FastLogger logger;
    private KoiConnection koi;

    private boolean disposed = false;
    private boolean isConnecting = false;

    private @JsonField @Getter @Nullable User userData;
    private @JsonField @Getter @Nullable StreamStatusEvent streamData;
    private @JsonField @Getter @Nullable List<User> viewers;
    private @JsonField @Getter @Nullable RoomstateEvent roomstate;

    private @Getter @Nullable List<KoiIntegrationFeatures> features = new ArrayList<>();

    private @Getter boolean isWelcomed = false;

    public AuthInstance(String tokenId) {
        this.tokenId = tokenId;
        this.logger = new FastLogger(String.format("AuthInstance (%d) ?", this.tokenId.hashCode()));

        this.token = CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .getToken("koi", this.tokenId);

        String koiUrl = CaffeinatedApp.getInstance().getKoiUrl();

        FastLogger koiLogger = new FastLogger("AuthInstance Koi (" + tokenId + ")");

        if (FastLoggingFramework.getDefaultLevel() == LogLevel.TRACE) {
            koiLogger.setCurrentLevel(LogLevel.TRACE);
        } else {
            koiLogger.setCurrentLevel(LogLevel.INFO);
        }

        this.koi = new KoiConnection(
            koiUrl,
            koiLogger,
            this,
            CaffeinatedApp.KOI_ID
        );

        this.reconnect();
    }

    @Override
    public void onOpen() {
        this.isWelcomed = true;
    }

    public void invalidate() {
        this.close();

        this.logger.info("I have been invalidate()'d, goodbye.");
        CaffeinatedApp.getInstance().getAuthPreferences().get().removeToken("koi", this.tokenId);
        CaffeinatedApp.getInstance().getAuth().getAuthInstances().remove(this.tokenId);
        CaffeinatedApp.getInstance().getAuth().checkAuth();
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    public void sendChat(@NonNull String message, @NonNull KoiChatterType chatter, @Nullable String replyTarget, boolean isUserGesture) {
        if (this.isConnected()) {
            this.koi.sendChat(message, chatter, replyTarget, isUserGesture);
        }
    }

    public void upvoteChat(@NonNull String messageId) {
        if (this.isConnected()) {
            this.koi.upvoteChat(messageId);
        }
    }

    public void deleteChat(@NonNull String messageId, boolean isUserGesture) {
        if (this.isConnected()) {
            this.koi.deleteChat(messageId, isUserGesture);
        }
    }

    /* ---------------- */
    /* Event Listeners  */
    /* ---------------- */

    @Override
    public void onSupportedFeatures(List<KoiIntegrationFeatures> features) {
        this.features = Collections.unmodifiableList(features);
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

//    @Override
//    public void onPlatformCategories(Map<String, String> categories) {
//        this.streamCategories = Collections.unmodifiableMap(categories);
//        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
//    }
//
//    @Override
//    public void onPlatformTags(Map<String, String> tags) {
//        this.streamTags = Collections.unmodifiableMap(tags);
//        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
//    }
//
//    @Override
//    public void onSupportedStreamConfigurationFeatures(List<KoiStreamConfigurationFeatures> streamConfigFeatures) {
//        if (streamConfigFeatures != null) {
//            this.streamConfigurationFeatures = Collections.unmodifiableList(streamConfigFeatures);
//            CaffeinatedApp.getInstance().getAuth().updateBridgeData();
//        }
//    }

    @SuppressWarnings("deprecation")
    @KoiEventHandler
    public void onUserUpdate(UserUpdateEvent e) {
//        boolean isAuthConfirmation = this.userData == null;

        this.userData = e.streamer;

        if (this.roomstate == null) {
            // TODO get rid of this by broadcasting roomstates across all platforms on
            // connect. (KOI)
            this.roomstate = RoomstateEvent.builder(RoomId.of(e.streamer.toSimpleProfile(), e.streamer.link))
                .streamer(e.streamer.toSimpleProfile())
                .timestamp(Instant.now())
                .build();
            CaffeinatedApp.getInstance().getKoi().broadcastEvent(this.roomstate);
        }

        // TODO
//        if (isAuthConfirmation) {
//            String puppetToken = CaffeinatedApp
//                .getInstance()
//                .getAuthPreferences()
//                .get()
//                .getToken("koiPuppet", this.tokenId);
//
//            if (puppetToken != null) {
//                this.koi.loginPuppet(puppetToken);
//            }
//        }

        CaffeinatedApp.getInstance().getAuth().checkStatus();
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @KoiEventHandler
    public void onStreamStatus(StreamStatusEvent e) {
        this.streamData = e;
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @KoiEventHandler
    public void onViewerList(ViewerListEvent e) {
        this.viewers = e.viewers;
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @KoiEventHandler
    public void onRoomState(RoomstateEvent e) {
        this.roomstate = e;
        CaffeinatedApp.getInstance().getAuth().updateBridgeData();
    }

    @KoiEventHandler
    public void onEvent(KoiEvent e) {
        KoiEventUtil.reflectInvoke(CaffeinatedApp.getInstance().getKoi(), e);
    }

    /* ---------------- */
    /* Connection Stuff */
    /* ---------------- */

    private void reconnect() {
        if (this.disposed || this.isConnecting || this.koi.isConnected()) return;

        this.isWelcomed = false;
        this.isConnecting = true;

        reconnectQueue.execute(() -> {
            try {
                this.userData = null;
                this.koi.login(this.token);
            } catch (Exception e) {
                this.logger.exception(e);
                this.onClose(true);
            }
        });

        this.isConnecting = false;
    }

    @Override
    public void onError(String errorCode) {
        switch (errorCode) {
            case "USER_AUTH_INVALID": {
                CaffeinatedApp.getInstance().notify(
                    "co.casterlabs.caffeinated.app.auth.logged_out",
                    Map.of("platform", this.tokenId),
                    NotificationType.WARNING
                );
                this.invalidate();
                return;
            }
        }
    }

    @SneakyThrows
    @Override
    public void onClose(boolean remote) {
        if (this.disposed) return;

        CaffeinatedApp.getInstance().getAuth().checkStatus();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        this.logger.info("Reconnecting to Koi.");
        AsyncTask.create(this::reconnect);
    }

    @Override
    public void onServerMessage(String message) {
        this.logger.info("Server message: %s", message);
    }

    @Override
    public void onException(Exception e) {
        this.logger.exception(e);
    }

    @Override
    public void close() {
        this.disposed = true;
        this.koi.close();
    }

    public boolean isConnected() {
        return this.koi.isConnected();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.userData);
    }

}
