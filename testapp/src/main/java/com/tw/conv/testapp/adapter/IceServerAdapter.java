package com.tw.conv.testapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tw.conv.testapp.model.TwilioIceServer;

import java.util.List;

import butterknife.ButterKnife;

public class IceServerAdapter extends BaseAdapter {

    private List<TwilioIceServer> iceServers;
    private Context context;

    public IceServerAdapter(Context context, List<TwilioIceServer> iceServers) {
        this.iceServers = iceServers;
        this.context = context;
    }

    @Override
    public int getCount() {
        return iceServers.size();
    }

    @Override
    public Object getItem(int position) {
        return iceServers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView urlTextView;

        if (convertView == null) {
            convertView = LayoutInflater
                    .from(context)
                    .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
        }
        urlTextView = ButterKnife.findById(convertView, android.R.id.text1);
        urlTextView.setText(iceServers.get(position).getUrl());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
