/*
 * Copyright (C) 2017 Twilio, Inc.
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

package com.twilio.video.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.twilio.video.app.model.TwilioIceServer;

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
