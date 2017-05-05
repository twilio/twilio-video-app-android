package com.twilio.video.app.ui.room;

import android.view.View;
import android.view.ViewGroup;

import com.twilio.video.Participant;
import com.twilio.video.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ParticipantController is main controlling party for rendering participants.
 */
public class ParticipantController {

    /**
     * Data container about primary participant -
     * sid, identity, video track, audio state and mirroring state.
     */
    private Item primaryItem;

    /**
     * Primary video track.
     */
    private ParticipantPrimaryView primaryView;

    /**
     * Participant thumb view group, where participants are added or removed from.
     */
    private ViewGroup thumbsViewContainer;

    /**
     * Relationship collection - item (data) -> thumb.
     */
    private Map<Item, ParticipantView> thumbs = new HashMap<>();

    /**
     * Each participant thumb click listener.
     */
    private ItemClickListener listener;

    public ParticipantController(ViewGroup thumbsViewContainer,
                                 ParticipantPrimaryView primaryVideoView) {

        this.thumbsViewContainer = thumbsViewContainer;
        this.primaryView = primaryVideoView;
    }

    public void addThumb(String sid, String identity) {
        addThumb(sid, identity, null, true, false);
    }

    public void addThumb(Item item) {
        addThumb(item.sid, item.identity, item.videoTrack, item.muted, item.mirror);
    }

    public void addThumb(String sid, String identity, VideoTrack videoTrack) {
        addThumb(sid, identity, videoTrack, true, false);
    }

    /**
     * Create new participant thumb from data.
     *
     * @param sid        unique participant identifier.
     * @param identity   participant name to display.
     * @param videoTrack participant video to display or NULL for empty thumbs.
     * @param muted      participant audio state.
     */
    public void addThumb(String sid,
                         String identity,
                         VideoTrack videoTrack,
                         boolean muted,
                         boolean mirror) {

        Item item = new Item(sid, identity, videoTrack, muted, mirror);
        ParticipantView view = createThumb(item);
        thumbs.put(item, view);
        thumbsViewContainer.addView(view);
    }

    /**
     * Update primary participant thumb with mirroring.
     *
     * @param mirror enable/disable video track mirroring.
     */
    public void updatePrimaryThumb(boolean mirror) {
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
     * @param sid      unique participant identifier.
     * @param oldVideo video track to replace.
     * @param newVideo new video track to insert.
     */
    public void updateThumb(String sid, VideoTrack oldVideo, VideoTrack newVideo) {
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
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     * @param state      new thumb state.
     */
    public void updateThumb(String sid, VideoTrack videoTrack, @ParticipantView.State int state) {
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
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     * @param mirror     enable/disable mirror.
     */
    public void updateThumb(String sid, VideoTrack videoTrack, boolean mirror) {
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
     * @param sid   unique participant identifier.
     * @param muted new audio state.
     */
    public void updateThumbs(String sid, boolean muted) {
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
     * @param sid      unique participant identifier.
     * @param identity participant name to display.
     * @param oldVideo video track to replace.
     * @param newVideo new video track to insert.
     */
    public void addOrUpdateThumb(String sid,
                                 String identity,
                                 VideoTrack oldVideo,
                                 VideoTrack newVideo) {

        if (hasThumb(sid, oldVideo)) {
            updateThumb(sid, oldVideo, newVideo);
        } else {
            addThumb(sid, identity, newVideo);
        }
    }

    public void removeThumb(Item item) {
        removeThumb(item.sid, item.videoTrack);
    }

    /**
     * Remove participant video track thumb.
     *
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     */
    public void removeThumb(String sid, VideoTrack videoTrack) {
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
    public void removeThumbs(String sid) {
        ArrayList<Item> deleteKeys = new ArrayList<>();
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey().sid.equals(sid)) {
                deleteKeys.add(entry.getKey());
                thumbsViewContainer.removeView(entry.getValue());
                VideoTrack videoTrack = entry.getKey().videoTrack;
                if (videoTrack != null) {
                    videoTrack.removeRenderer(entry.getValue());
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
     * @param sid        unique participant identifier.
     * @param identity   participant name to display.
     * @param videoTrack target video track.
     */
    public void removeOrEmptyThumb(String sid, String identity, VideoTrack videoTrack) {
        if (getThumbs(sid).size() > 1) {
            removeThumb(sid, videoTrack);
        } else if (getThumbsCount(sid) == 0) {
            addThumb(sid, identity);
        } else {
            updateThumb(sid, videoTrack, null);
        }
    }

    /**
     * Get participant video track thumb instance.
     *
     * @param sid        unique participant identifier.
     * @param videoTrack target video track.
     * @return participant thumb instance.
     */
    public ParticipantView getThumb(String sid, VideoTrack videoTrack) {
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            if (entry.getKey().sid.equals(sid) &&
                    entry.getKey().videoTrack == videoTrack) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Remove all thumbs for all participants.
     */
    public void removeAllThumbs() {
        for (Map.Entry<Item, ParticipantView> entry : thumbs.entrySet()) {
            thumbsViewContainer.removeView(entry.getValue());
            removeRender(entry.getKey().videoTrack, entry.getValue());
        }
        thumbs.clear();
    }

    public void renderAsPrimary(Item item) {
        renderAsPrimary(item.sid, item.identity, item.videoTrack, item.muted, item.mirror);
    }

    /**
     * Render participant as primary participant from data.
     *
     * @param sid        unique participant identifier.
     * @param identity   participant name to display.
     * @param videoTrack participant video to display or NULL for empty thumbs.
     * @param muted      participant audio state.
     * @param mirror     enable/disable mirroring for video track.
     */
    public void renderAsPrimary(String sid,
                                String identity,
                                VideoTrack videoTrack,
                                boolean muted,
                                boolean mirror) {

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
     * Remove primary participant.
     */
    public void removePrimary() {
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
    public Item getPrimaryItem() {
        return primaryItem;
    }

    /**
     * Get primary participant view.
     *
     * @return primary participant view instance.
     */
    public ParticipantPrimaryView getPrimaryView() {
        return primaryView;
    }

    public void setListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private boolean hasThumb(String sid, VideoTrack videoTrack) {
        return getThumb(sid, videoTrack) != null;
    }

    private int getThumbsCount(String sid) {
        return getThumbs(sid).size();
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onThumbClick(item);
                }
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

    /**
     * Participant information data holder.
     */
    public static class Item {

        /**
         * Participant unique identifier.
         */
        String sid;

        /**
         * Participant name.
         */
        String identity;

        /**
         * Participant video track.
         */
        VideoTrack videoTrack;

        /**
         * Participant audio state.
         */
        boolean muted;

        /**
         * Video track mirroring enabled/disabled.
         */
        boolean mirror;

        public Item(String sid,
                    String identity,
                    VideoTrack videoTrack,
                    boolean muted,
                    boolean mirror) {

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
