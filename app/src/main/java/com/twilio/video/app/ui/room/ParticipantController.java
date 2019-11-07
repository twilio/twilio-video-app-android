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
import androidx.annotation.Nullable;
import com.twilio.video.VideoTrack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private Map<Item, ParticipantView> thumbs = new HashMap<>();

    /** Each participant thumb click listener. */
    private ItemClickListener listener;

    ParticipantController(ViewGroup thumbsViewContainer, ParticipantPrimaryView primaryVideoView) {

        this.thumbsViewContainer = thumbsViewContainer;
        this.primaryView = primaryVideoView;
    }

    private void addThumb(String sid, String identity) {
        addThumb(sid, identity, null, true, false, false);
    }

    void addThumb(Item item) {
        addThumb(
                item.sid,
                item.identity,
                item.videoTrack,
                item.muted,
                item.mirror,
                item.showNetworkQualityLevel);
    }

    private void addThumb(String sid, String identity, VideoTrack videoTrack) {
        addThumb(sid, identity, videoTrack, true, false, false);
    }

    /**
     * Create new participant thumb from data.
     *
     * @param sid unique participant identifier.
     * @param identity participant name to display.
     * @param videoTrack participant video to display or NULL for empty thumbs.
     * @param muted participant audio state.
     */
    void addThumb(
            String sid,
            String identity,
            VideoTrack videoTrack,
            boolean muted,
            boolean mirror,
            boolean showNetworkQualityLevel) {

        Item item = new Item(sid, identity, videoTrack, muted, mirror, showNetworkQualityLevel);
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

    /**
     * Update participant thumb with video track.
     *
     * @param sid unique participant identifier.
     * @param oldVideo video track to replace.
     * @param newVideo new video track to insert.
     */
    void updateThumb(String sid, VideoTrack oldVideo, VideoTrack newVideo) {
        Item target = findItem(sid, oldVideo);
        if (target != null) {
            ParticipantView view = getThumb(sid, oldVideo);

            removeRender(target.videoTrack, view);

            target.videoTrack = newVideo;

            if (target.videoTrack != null) {
                view.setState(ParticipantView.State.VIDEO);
                target.videoTrack.addRenderer(view);
            } else {
                view.setState(ParticipantView.State.NO_VIDEO);
            }
        }
    }

    /**
     * Update participant video track thumb with state.
     *
     * @param sid unique participant identifier.
     * @param videoTrack target video track.
     * @param state new thumb state.
     */
    void updateThumb(String sid, VideoTrack videoTrack, @ParticipantView.State int state) {
        Item target = findItem(sid, videoTrack);
        if (target != null) {
            ParticipantThumbView view = (ParticipantThumbView) getThumb(sid, videoTrack);

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
     * Update participant video track thumb with mirroring.
     *
     * @param sid unique participant identifier.
     * @param videoTrack target video track.
     * @param mirror enable/disable mirror.
     */
    void updateThumb(String sid, VideoTrack videoTrack, boolean mirror) {
        Item target = findItem(sid, videoTrack);
        if (target != null) {
            ParticipantThumbView view = (ParticipantThumbView) getThumb(sid, videoTrack);

            target.mirror = mirror;
            view.setMirror(target.mirror);
        }
    }

    /**
     * Update all participant thumbs with audio state.
     *
     * @param sid unique participant identifier.
     * @param muted new audio state.
     */
    void updateThumbs(String sid, boolean muted) {
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey().sid.equals(sid)) {
                entry.getKey().muted = muted;
                entry.getValue().setMuted(muted);
            }
        }
    }

    /**
     * Add new participant thumb or update old instance.
     *
     * @param sid unique participant identifier.
     * @param identity participant name to display.
     * @param oldVideo video track to replace.
     * @param newVideo new video track to insert.
     */
    void addOrUpdateThumb(String sid, String identity, VideoTrack oldVideo, VideoTrack newVideo) {

        if (hasThumb(sid, oldVideo)) {
            updateThumb(sid, oldVideo, newVideo);
        } else {
            addThumb(sid, identity, newVideo);
        }
    }

    void removeThumb(Item item) {
        removeThumb(item.sid, item.videoTrack);
    }

    /**
     * Remove participant video track thumb.
     *
     * @param sid unique participant identifier.
     * @param videoTrack target video track.
     */
    void removeThumb(String sid, VideoTrack videoTrack) {
        Item target = findItem(sid, videoTrack);
        if (target != null) {
            ParticipantView view = getThumb(sid, videoTrack);

            removeRender(target.videoTrack, view);

            thumbsViewContainer.removeView(view);
            thumbs.remove(target);
        }
    }

    /**
     * Remove all participant thumbs.
     *
     * @param sid unique participant identifier.
     */
    void removeThumbs(String sid) {
        ArrayList<Item> deleteKeys = new ArrayList<>();
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey().sid.equals(sid)) {
                deleteKeys.add(entry.getKey());
                thumbsViewContainer.removeView(entry.getValue());
                VideoTrack remoteVideoTrack = entry.getKey().videoTrack;
                if (remoteVideoTrack != null) {
                    remoteVideoTrack.removeRenderer(entry.getValue());
                }
            }
        }

        for (Item deleteKey : deleteKeys) {
            thumbs.remove(deleteKey);
        }
    }

    /**
     * Remove participant thumb or leave empty (no video) thumb if nothing left.
     *
     * @param sid unique participant identifier.
     * @param identity participant name to display.
     * @param videoTrack target video track.
     */
    void removeOrEmptyThumb(String sid, String identity, VideoTrack videoTrack) {
        int thumbsCount = getThumbs(sid).size();
        if (thumbsCount > 1 || (thumbsCount == 1 && primaryItem.sid.equals(sid))) {
            removeThumb(sid, videoTrack);
        } else if (thumbsCount == 0) {
            addThumb(sid, identity);
        } else {
            updateThumb(sid, videoTrack, null);
        }
    }

    /**
     * Get participant video track thumb instance.
     *
     * @param sid unique participant identifier.
     * @param videoTrack target video track.
     * @return participant thumb instance.
     */
    ParticipantView getThumb(String sid, VideoTrack videoTrack) {
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey() != null
                    && entry.getKey().sid.equals(sid)
                    && entry.getKey().videoTrack == videoTrack) {
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

    void renderAsPrimary(Item item) {
        renderAsPrimary(item.sid, item.identity, item.videoTrack, item.muted, item.mirror);
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

    /** Remove primary participant. */
    void removePrimary() {
        removeRender(primaryItem.videoTrack, primaryView);
        // TODO: temp state
        primaryView.setState(ParticipantView.State.NO_VIDEO);
        primaryItem = null;
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

    private void clearDominantSpeaker() {
        getPrimaryView().dominantSpeakerImg.setVisibility(View.GONE);
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            entry.getValue().dominantSpeakerImg.setVisibility(View.GONE);
        }
    }

    private boolean hasThumb(String sid, VideoTrack videoTrack) {
        return getThumb(sid, videoTrack) != null;
    }

    private Item findItem(String sid, VideoTrack videoTrack) {
        for (Item item : thumbs.keySet()) {
            if (item.sid.equals(sid) && item.videoTrack == videoTrack) {
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
        view.showNetworkQualityLevel(item.showNetworkQualityLevel);

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

    private ArrayList<ParticipantView> getThumbs(String sid) {
        ArrayList<ParticipantView> views = new ArrayList<>();
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey().sid.equals(sid)) {
                views.add(entry.getValue());
            }
        }
        return views;
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

        boolean showNetworkQualityLevel;

        Item(String sid, String identity, VideoTrack videoTrack, boolean muted, boolean mirror) {

            this.sid = sid;
            this.identity = identity;
            this.videoTrack = videoTrack;
            this.muted = muted;
            this.mirror = mirror;
        }

        Item(
                String sid,
                String identity,
                VideoTrack videoTrack,
                boolean muted,
                boolean mirror,
                boolean showNetworkQualityLevel) {

            this(sid, identity, videoTrack, muted, mirror);
            this.showNetworkQualityLevel = showNetworkQualityLevel;
        }
    }

    public interface ItemClickListener {
        void onThumbClick(Item item);
    }
}
