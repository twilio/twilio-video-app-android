package com.tw.conv.testapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tw.conv.testapp.R;
import com.twilio.conversations.RemoteVideoTrackStatsRecord;

import java.util.LinkedHashMap;

public class RemoteVideoTrackStatsAdapter extends RecyclerView.Adapter<RemoteVideoTrackStatsAdapter.ViewHolder> {

    private LinkedHashMap<String, RemoteVideoTrackStatsRecord> statsRecordMap;

    public RemoteVideoTrackStatsAdapter(LinkedHashMap<String, RemoteVideoTrackStatsRecord> statsRecordMap) {
        this.statsRecordMap = statsRecordMap;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int i = 0;
        String name = null;
        RemoteVideoTrackStatsRecord remoteVideoTrackStatsRecord = null;
        for(String participantIdentity : statsRecordMap.keySet() ){
            if(position == i) {
                name = participantIdentity;
                remoteVideoTrackStatsRecord = statsRecordMap.get(participantIdentity);
                break;
            }
            i++;
        }

        holder.title.setText(name + " Remote Video");

        String remoteVideoTrackStats =
                String.format("<b>SID</b> %s<br/>", remoteVideoTrackStatsRecord.getParticipantSid()) +
                        '\n' +
                        String.format("<b>Codec</b> %s<br/>", remoteVideoTrackStatsRecord.getCodecName()) +
                        '\n' +
                        String.format("<b>Dimensions</b> %s<br/>", remoteVideoTrackStatsRecord.getDimensions().toString()) +
                        '\n' +
                        String.format("<b>Fps</b> %d", remoteVideoTrackStatsRecord.getFrameRate());

        holder.stats.setText(remoteVideoTrackStats);
    }

    @Override
    public int getItemCount() {
        return statsRecordMap.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView stats;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.video_track_title_textview);
            stats = (TextView) itemView.findViewById(R.id.video_track_stats_textview);
        }
    }

}
