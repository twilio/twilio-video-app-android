package com.twilio.conversations.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;

import static junit.framework.Assert.fail;

public class CameraCapturerTestActivity extends Activity {
    public static final String CONTENT_DESCRIPTION_CREATE_CAPTURER = "Creates camera capturer";
    public static final String CONTENT_DESCRIPTION_START_PREVIEW = "Starts preview of capturer";
    public static final String CONTENT_DESCRIPTION_STOP_PREVIEW = "Stops preview of capturer";

    private LinearLayout baseLayout;
    private LinearLayout buttonToolBar;
    private FrameLayout previewFrameLayout;
    private Button createCapturerButton;
    private Button startPreviewButton;
    private Button stopPreviewButton;

    public CameraCapturer cameraCapturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout();
        setContentView(baseLayout);
    }

    private void setupLayout() {
        // Create a base layout with a toolbar for buttons and the preview container below
        baseLayout = new LinearLayout(this);
        baseLayout.setOrientation(LinearLayout.VERTICAL);
        buttonToolBar = new LinearLayout(this);
        previewFrameLayout = new FrameLayout(this);

        // Setup each button
        setupCapturerButton();
        setupPreviewButton();
        setupStopPreviewButton();

        // Add everything to the base layout
        buttonToolBar.addView(createCapturerButton);
        buttonToolBar.addView(startPreviewButton);
        buttonToolBar.addView(stopPreviewButton);
        baseLayout.addView(buttonToolBar);
        baseLayout.addView(previewFrameLayout);
    }

    private void setupStopPreviewButton() {
        stopPreviewButton = new Button(this);
        stopPreviewButton.setContentDescription(CONTENT_DESCRIPTION_STOP_PREVIEW);
        stopPreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraCapturer != null) {
                    cameraCapturer.stopPreview();
                }
            }
        });
    }

    private void setupPreviewButton() {
        startPreviewButton = new Button(this);
        startPreviewButton
                .setContentDescription(CONTENT_DESCRIPTION_START_PREVIEW);
        startPreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCapturer.startPreview(previewFrameLayout);
            }
        });
    }

    private void setupCapturerButton() {
        createCapturerButton = new Button(this);
        createCapturerButton
                .setContentDescription(CONTENT_DESCRIPTION_CREATE_CAPTURER);
        createCapturerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCapturer = CameraCapturerFactory
                        .createCameraCapturer(CameraCapturerTestActivity.this,
                                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                                new CapturerErrorListener() {
                                    @Override
                                    public void onError(CapturerException e) {
                                        fail(e.getMessage());
                                    }
                                });

            }
        });
    }
}
