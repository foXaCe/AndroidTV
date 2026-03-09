package org.jellyfin.androidtv.ui.playback.overlay;

import static org.koin.java.KoinJavaComponent.inject;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwnerKt;

import org.jellyfin.androidtv.preference.UserPreferences;
import org.jellyfin.androidtv.ui.playback.CustomPlaybackOverlayFragment;
import org.jellyfin.androidtv.ui.playback.PlaybackController;
import org.jellyfin.androidtv.ui.playback.PlaybackControllerContainer;
import org.jellyfin.androidtv.ui.playback.overlay.compose.PlayerOverlayAction;
import org.jellyfin.androidtv.ui.playback.overlay.compose.PlayerOverlayComposeHelperKt;
import org.jellyfin.androidtv.ui.playback.overlay.compose.PlayerOverlayViewModel;
import kotlin.Lazy;
import kotlin.Unit;
import kotlinx.coroutines.CoroutineScope;
import timber.log.Timber;

public class LeanbackOverlayFragment extends Fragment {
    private VideoPlayerControllerImpl playerAdapter;
    private PlayerOverlayViewModel overlayViewModel;
    private boolean shouldShowOverlay = true;
    private boolean controlsOverlayVisible = false;
    private View.OnKeyListener keyInterceptListener;
    private ComposeView composeView;

    private Lazy<PlaybackControllerContainer> playbackControllerContainer = inject(PlaybackControllerContainer.class);
    private Lazy<UserPreferences> userPreferences = inject(UserPreferences.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlaybackController playbackController = playbackControllerContainer.getValue().getPlaybackController();
        if (playbackController == null) {
            Timber.w("PlaybackController is null, skipping initialization.");
            return;
        }

        playerAdapter = new VideoPlayerControllerImpl(playbackController, this);
        overlayViewModel = new PlayerOverlayViewModel(
                requireContext(), playerAdapter, playbackController, userPreferences.getValue()
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Custom FrameLayout that intercepts key events before children
        FrameLayout root = new FrameLayout(requireContext()) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (keyInterceptListener != null && keyInterceptListener.onKey(this, event.getKeyCode(), event)) {
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (overlayViewModel != null && view instanceof ViewGroup) {
            ViewGroup root = (ViewGroup) view;

            // Create ComposeView for the player overlay (Kotlin helper for Compose interop)
            composeView = new ComposeView(requireContext());
            PlayerOverlayComposeHelperKt.setupPlayerOverlayComposeView(
                    composeView, overlayViewModel, action -> {
                        handleComposeAction(action);
                        return Unit.INSTANCE;
                    }
            );

            root.addView(composeView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            // Start collecting player state
            CoroutineScope scope = LifecycleOwnerKt.getLifecycleScope(getViewLifecycleOwner());
            overlayViewModel.startCollecting(scope);
        }

        hideControlsOverlay(false);
    }

    private void handleComposeAction(PlayerOverlayAction action) {
        PlaybackController playbackController = playbackControllerContainer.getValue().getPlaybackController();
        if (playbackController == null) return;

        // All actions go through ViewModel (handles transport, popups, selections)
        overlayViewModel.onAction(action);

        // Legacy popup views still handled by the master overlay fragment
        if (action instanceof PlayerOverlayAction.ShowChapters) {
            playerAdapter.hideOverlay();
            playerAdapter.getMasterOverlayFragment().showChapterSelector();
        } else if (action instanceof PlayerOverlayAction.ShowCast) {
            playerAdapter.hideOverlay();
            playerAdapter.getMasterOverlayFragment().showCastSelector();
        } else if (action instanceof PlayerOverlayAction.ShowChannels) {
            playerAdapter.hideOverlay();
            playerAdapter.getMasterOverlayFragment().showQuickChannelChanger();
        } else if (action instanceof PlayerOverlayAction.ShowGuide) {
            playerAdapter.hideOverlay();
            playerAdapter.getMasterOverlayFragment().showGuide();
        } else if (action instanceof PlayerOverlayAction.PreviousChannel) {
            playerAdapter.getMasterOverlayFragment().switchChannel(
                    org.jellyfin.androidtv.ui.livetv.TvManager.getPrevLiveTvChannel()
            );
        }
    }

    public void initFromView(CustomPlaybackOverlayFragment customPlaybackOverlayFragment) {
        playerAdapter.setMasterOverlayFragment(customPlaybackOverlayFragment);
    }

    public void showControlsOverlay(boolean runAnimation) {
        if (shouldShowOverlay) {
            if (overlayViewModel != null) {
                overlayViewModel.showOverlay();
            }
            controlsOverlayVisible = true;
            playerAdapter.getMasterOverlayFragment().show();
        }
    }

    public void hideControlsOverlay(boolean runAnimation) {
        if (overlayViewModel != null) {
            overlayViewModel.hideOverlay();
        }
        controlsOverlayVisible = false;
        if (playerAdapter != null) {
            playerAdapter.getMasterOverlayFragment().hide();
        }
    }

    public boolean isControlsOverlayVisible() {
        return controlsOverlayVisible;
    }

    public void setOnKeyInterceptListener(View.OnKeyListener listener) {
        keyInterceptListener = listener;
    }

    public void updateCurrentPosition() {
        // Now handled by ViewModel polling — no-op
        updatePlayState();
    }

    public void updatePlayState() {
        // Now handled by ViewModel polling — no-op
    }

    public void setShouldShowOverlay(boolean shouldShowOverlay) {
        this.shouldShowOverlay = shouldShowOverlay;
    }

    public void hideOverlay() {
        hideControlsOverlay(true);
    }

    public void setFading(boolean fadingEnabled) {
        playerAdapter.getMasterOverlayFragment().setFadingEnabled(fadingEnabled);
    }

    public void mediaInfoChanged() {
        org.jellyfin.sdk.model.api.BaseItemDto currentlyPlayingItem =
                playbackControllerContainer.getValue().getPlaybackController().getCurrentlyPlayingItem();
        if (currentlyPlayingItem == null) return;

        if (overlayViewModel != null) {
            overlayViewModel.updateMediaInfo();
        }
    }

    public void recordingStateChanged() {
        // Now handled by ViewModel polling — no-op
    }

    // Key handling for subtitle/audio hardware keys
    public boolean onMediaKey(int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) return false;
        if (overlayViewModel == null) return false;

        if (playerAdapter.hasSubs() && keyCode == KeyEvent.KEYCODE_CAPTIONS) {
            overlayViewModel.onAction(PlayerOverlayAction.ShowSubtitles.INSTANCE);
            return true;
        }
        if (playerAdapter.hasMultiAudio() && keyCode == KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK) {
            overlayViewModel.onAction(PlayerOverlayAction.ShowAudio.INSTANCE);
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerAdapter != null) {
            playerAdapter.getMasterOverlayFragment().onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (overlayViewModel != null) {
            overlayViewModel.stopCollecting();
        }
        if (playerAdapter != null) {
            playerAdapter.detach();
        }
        composeView = null;
    }

    public void onFullyInitialized() {
        if (overlayViewModel != null) {
            overlayViewModel.updateMediaInfo();
        }
    }

}
