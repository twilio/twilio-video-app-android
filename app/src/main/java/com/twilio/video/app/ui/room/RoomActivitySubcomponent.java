package com.twilio.video.app.ui.room;


import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface RoomActivitySubcomponent extends AndroidInjector<RoomActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<RoomActivity> {}
}
