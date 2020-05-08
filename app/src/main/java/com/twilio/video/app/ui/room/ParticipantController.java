/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.app.ui.room;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.twilio.video.NetworkQualityLevel;
import com.twilio.video.VideoTrack;
import com.twilio.video.app.R;
import com.twilio.video.app.participant.ParticipantViewState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import timber.log.Timber;

// TODO Replace with RecyclerView / DiffUtil implementation
/** ParticipantController is main controlling party for rendering participants. */
class ParticipantController {

    /**
     * Data container about primary participant - sid, identity, video track, audio state and
     * mirroring state.
     */
    private Item primaryItem;

    /** Primary video track. */
    private ParticipantPrimaryView primaryView;

    /** RemoteParticipant thumb view group, where participants are added or removed from. */
    private ViewGroup thumbsViewContainer;

    /** Relationship collection - item (data) -> thumb. */
    ConcurrentMap<Item, ParticipantView> thumbs = new ConcurrentHashMap<>();

    /** Each participant thumb click listener. */
    private ItemClickListener listener;

    ParticipantController(ViewGroup thumbsViewContainer, ParticipantPrimaryView primaryVideoView) {

        this.thumbsViewContainer = thumbsViewContainer;
        this.primaryView = primaryVideoView;
    }

    void addThumb(ParticipantViewState participantViewState) {
        Item item =
                new Item(
                        participantViewState.getSid(),
                        participantViewState.getIdentity(),
                        participantViewState.getVideoTrack(),
                        participantViewState.getMuted(),
                        participantViewState.getMirror());
        ParticipantView view = createThumb(item);
        thumbs.put(item, view);
        thumbsViewContainer.addView(view);
    }

    /**
     * Update primary participant thumb with mirroring.
     *
     * @param mirror enable/disable video track mirroring.
     */
    void updatePrimaryThumb(boolean mirror) {
        Item target = getPrimaryItem();
        if (target != null) {
            ParticipantView view = getPrimaryView();

            target.mirror = mirror;
            view.setMirror(target.mirror);
        }
    }

    void updateThumb(ParticipantViewState participantViewState) {
        Timber.d("updateThumb: %s", participantViewState);
        Item target = findItem(participantViewState.getSid());
        if (target != null) {
            ParticipantView view = getThumb(participantViewState.getSid());

            removeRender(target.videoTrack, view);

            target.videoTrack = participantViewState.getVideoTrack();
            view.setMuted(participantViewState.getMuted());

            if (target.videoTrack != null) {
                view.setState(ParticipantView.State.VIDEO);
                target.videoTrack.addRenderer(view);
            } else {
                view.setState(ParticipantView.State.NO_VIDEO);
            }

            setNetworkQualityLevelImage(
                    view.networkQualityLevelImg, participantViewState.getNetworkQualityLevel());
        }
    }

    /**
     * Update participant video track thumb with state.
     *
     * @param sid unique participant identifier.
     * @param state new thumb state.
     */
    void updateThumb(String sid, @ParticipantView.State int state) {
        Item target = findItem(sid);
        if (target != null) {
            ParticipantThumbView view = (ParticipantThumbView) getThumb(sid);

            view.setState(state);
            switch (state) {
                case ParticipantView.State.NO_VIDEO:
                case ParticipantView.State.SELECTED:
                    removeRender(target.videoTrack, view);
                    break;
                case ParticipantView.State.VIDEO:
                    target.videoTrack.addRenderer(view);
                    break;
            }
        }
    }

    /**
     * Remove participant video track thumb.
     *
     * @param sid unique participant identifier.
     */
    void removeThumb(String sid) {
        Item target = findItem(sid);
        if (target != null) {
            ParticipantView view = getThumb(sid);

            removeRender(target.videoTrack, view);

            thumbsViewContainer.removeView(view);
            thumbs.remove(target);
        }
    }

    /**
     * Get participant video track thumb instance.
     *
     * @param sid unique participant identifier.
     * @return participant thumb instance.
     */
    ParticipantView getThumb(String sid) {
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey() != null && entry.getKey().sid.equals(sid)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** Remove all thumbs for all participants. */
    void removeAllThumbs() {
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            thumbsViewContainer.removeView(entry.getValue());
            if (entry.getKey() != null) {
                removeRender(entry.getKey().videoTrack, entry.getValue());
            }
        }
        thumbs.clear();
    }

    /**
     * Render participant as primary participant from data.
     *
     * @param sid unique participant identifier.
     * @param identity participant name to display.
     * @param videoTrack participant video to display or NULL for empty thumbs.
     * @param muted participant audio state.
     * @param mirror enable/disable mirroring for video track.
     */
    void renderAsPrimary(
            String sid, String identity, VideoTrack videoTrack, boolean muted, boolean mirror) {

        Item old = primaryItem;
        Item newItem = new Item(sid, identity, videoTrack, muted, mirror);

        // clean old primary video renderings
        if (old != null) {
            removeRender(old.videoTrack, primaryView);
        }

        primaryItem = newItem;
        primaryView.setIdentity(primaryItem.identity);
        primaryView.showIdentityBadge(true);
        primaryView.setMuted(primaryItem.muted);
        primaryView.setMirror(mirror);

        if (primaryItem.videoTrack != null) {
            removeRender(primaryItem.videoTrack, primaryView);
            primaryView.setState(ParticipantView.State.VIDEO);
            primaryItem.videoTrack.addRenderer(primaryView);
        } else {
            primaryView.setState(ParticipantView.State.NO_VIDEO);
        }
    }

    /**
     * Get data about primary participant.
     *
     * @return participant item data.
     */
    Item getPrimaryItem() {
        return primaryItem;
    }

    /**
     * Get primary participant view.
     *
     * @return primary participant view instance.
     */
    ParticipantPrimaryView getPrimaryView() {
        return primaryView;
    }

    void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    void setDominantSpeaker(@Nullable ParticipantView participantView) {
        clearDominantSpeaker();
        if (participantView != null) {
            participantView.dominantSpeakerImg.setVisibility(View.VISIBLE);
        }
    }

    private void setNetworkQualityLevelImage(
            ImageView networkQualityImage, NetworkQualityLevel networkQualityLevel) {

        if (networkQualityLevel == null
                || networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN) {
            networkQualityImage.setVisibility(View.GONE);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ZERO) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_0);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_ONE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_1);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_TWO) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_2);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_THREE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_3);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FOUR) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_4);
        } else if (networkQualityLevel == NetworkQualityLevel.NETWORK_QUALITY_LEVEL_FIVE) {
            networkQualityImage.setVisibility(View.VISIBLE);
            networkQualityImage.setImageResource(R.drawable.network_quality_level_5);
        }
    }

    private void clearDominantSpeaker() {
        getPrimaryView().dominantSpeakerImg.setVisibility(View.GONE);
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            entry.getValue().dominantSpeakerImg.setVisibility(View.GONE);
        }
    }

    private Item findItem(String sid) {
        for (Item item : thumbs.keySet()) {
            if (item.sid.equals(sid)) {
                return item;
            }
        }
        return null;
    }

    private ParticipantView createThumb(final Item item) {
        final ParticipantView view = new ParticipantThumbView(thumbsViewContainer.getContext());

        view.setIdentity(item.identity);
        view.setMuted(item.muted);
        view.setMirror(item.mirror);

        view.setOnClickListener(
                participantView -> {
                    if (listener != null) {
                        listener.onThumbClick(item);
                    }
                });

        if (item.videoTrack != null) {
            item.videoTrack.addRenderer(view);
            view.setState(ParticipantView.State.VIDEO);
        } else {
            view.setState(ParticipantView.State.NO_VIDEO);
        }

        return view;
    }

    private void removeRender(VideoTrack videoTrack, ParticipantView view) {
        if (videoTrack == null || !videoTrack.getRenderers().contains(view)) return;
        videoTrack.removeRenderer(view);
    }

    /** RemoteParticipant information data holder. */
    static class Item {

        /** RemoteParticipant unique identifier. */
        String sid;

        /** RemoteParticipant name. */
        String identity;

        /** RemoteParticipant video track. */
        VideoTrack videoTrack;

        /** RemoteParticipant audio state. */
        boolean muted;

        /** Video track mirroring enabled/disabled. */
        boolean mirror;

        Item(String sid, String identity, VideoTrack videoTrack, boolean muted, boolean mirror) {

            this.sid = sid;
            this.identity = identity;
            this.videoTrack = videoTrack;
            this.muted = muted;
            this.mirror = mirror;
        }
    }

    public interface ItemClickListener {
        void onThumbClick(Item item);
    }
}
