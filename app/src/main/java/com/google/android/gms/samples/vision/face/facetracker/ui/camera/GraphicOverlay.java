/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker.ui.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.samples.vision.face.facetracker.Global;
import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.Set;

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview).  The creator can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.<p>
 *
 * Supports scaling and mirroring of the graphics relative the camera's preview properties.  The
 * idea is that detection items are expressed in terms of a preview size, but need to be scaled up
 * to the full view size, and also mirrored in the case of the front-facing camera.<p>
 *
 * Associated {@link Graphic} items should use the following methods to convert to view coordinates
 * for the graphics that are drawn:
 * <ol>
 * <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of the
 * supplied value from the preview scale to the view scale.</li>
 * <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the coordinate
 * from the preview's coordinate system to the view coordinate system.</li>
 * </ol>
 */
public class GraphicOverlay extends View {
    private final Object mLock = new Object();
    private int mPreviewWidth;
    public float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight;
    public float mHeightScaleFactor = 1.0f;
    public int mFacing = CameraSource.CAMERA_FACING_BACK;
    private Set<Graphic> mGraphics = new HashSet<>();

    /**
     * Base class for a custom graphics object to be rendered within the graphic overlay.  Subclass
     * this and implement the {@link Graphic#draw(Canvas)} method to define the
     * graphics element.  Add instances to the overlay using {@link GraphicOverlay#add(Graphic)}.
     */
    public static abstract class Graphic {
        private GraphicOverlay mOverlay;

        public Graphic(GraphicOverlay overlay) {
            mOverlay = overlay;
        }

        /**
         * Draw the graphic on the supplied canvas.  Drawing should use the following methods to
         * convert to view coordinates for the graphics that are drawn:
         * <ol>
         * <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of
         * the supplied value from the preview scale to the view scale.</li>
         * <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
         * coordinate from the preview's coordinate system to the view coordinate system.</li>
         * </ol>
         *
         * @param canvas drawing canvas
         */
        public abstract void draw(Canvas canvas);

        /**
         * Adjusts a horizontal value of the supplied value from the preview scale to the view
         * scale.
         */
        public float scaleX(float horizontal) {
            return horizontal * mOverlay.mWidthScaleFactor;
        }

        /**
         * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
         */
        public float scaleY(float vertical) {
            return vertical * mOverlay.mHeightScaleFactor;
        }

        /**
         * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
         * system.
         */
        public float translateX(float x) {
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        /**
         * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
         * system.
         */
        public float translateY(float y) {
            return scaleY(y);
        }

        public void postInvalidate() {
            mOverlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Removes all graphics from the overlay.
     */
    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
        }
        postInvalidate();
    }

    /**
     * Adds a graphic to the overlay.
     */
    public void add(Graphic graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
        }
        postInvalidate();
    }

    /**
     * Removes a graphic from the overlay.
     */
    public void remove(Graphic graphic) {
        synchronized (mLock) {
            mGraphics.remove(graphic);
        }
        postInvalidate();
    }

    /**
     * Sets the camera attributes for size and facing direction, which informs how to transform
     * image coordinates later.
     */
    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mFacing = facing;
        }
        postInvalidate();
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Ellip dimension
        Global.coordEllip[0] = Global.widthScreen/4;//left
        Global.coordEllip[1] = Global.widthScreen*2/15;//top
        Global.coordEllip[2] = Global.widthScreen*3/4;//right
        Global.coordEllip[3] = Global.widthScreen*11/15;//bottom

        // Draw a circle in
        Paint mCirIn = new Paint();
        mCirIn.setColor(Color.WHITE);
        mCirIn.setStyle(Paint.Style.STROKE);
        mCirIn.setStrokeWidth(2);
        int pad = 40;
        RectF oval2 = new RectF(Global.coordEllip[0]+pad, Global.coordEllip[1]+pad, Global.coordEllip[2]-pad, Global.coordEllip[3]-pad);
        canvas.drawOval(oval2, mCirIn);

        // Draw a circle out
        Paint mBox;
        mBox = new Paint();
        mBox.setColor(Color.WHITE);
        mBox.setStyle(Paint.Style.STROKE);
        mBox.setStrokeWidth(3);

        RectF oval1 = new RectF(Global.coordEllip[0], Global.coordEllip[1], Global.coordEllip[2], Global.coordEllip[3]);
        canvas.drawOval(oval1, mBox);



//
//        Paint mBoxx;
//        mBoxx = new Paint();
//        mBoxx.setColor(Color.WHITE);
//        mBoxx.setStyle(Paint.Style.FILL_AND_STROKE);
//        mBoxx.setStrokeWidth(2);
//
//        float i=50,j=8;
//        //Top_Left
//        canvas.drawLine(150,150-i,150,200-i, mBox);
//        canvas.drawLine(150,150-i,200,150-i, mBox);
//
//        canvas.drawLine(150+j,150-i+j,150+j,200-i-j, mBoxx);
//        canvas.drawLine(150+j,150-i+j,200-j,150-i+j, mBoxx);
//
//        //Top_Right
//        canvas.drawLine(450,150-i,450,200-i, mBoxx);
//        canvas.drawLine(450,150-i,400,150-i, mBoxx);
//
//        canvas.drawLine(450-j,150-i+j,450-j,200-i-j, mBoxx);
//        canvas.drawLine(450-j,150-i+j,400+j,150-i+j, mBoxx);
//
//        //Bot_Left
//        canvas.drawLine(150,450+i,150,400+i, mBox);
//        canvas.drawLine(150,450+i,200,450+i, mBox);
//
//        canvas.drawLine(150+j,450+i-j,150+j,400+i+j, mBoxx);
//        canvas.drawLine(150+j,450+i-j,200-j,450+i-j, mBoxx);
//
//        //Bot_Right
//        canvas.drawLine(450,450+i,450,400+i, mBox);
//        canvas.drawLine(450,450+i,400,450+i, mBox);
//
//        canvas.drawLine(450-j,450+i-j,450-j,400+i+j, mBoxx);
//        canvas.drawLine(450-j,450+i-j,400+j,450+i-j, mBoxx);

        synchronized (mLock) {
            if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
                mWidthScaleFactor = (float) getWidth() / (float) mPreviewWidth;
                mHeightScaleFactor = (float) getHeight() / (float) mPreviewHeight;
            }

            for (Graphic graphic : mGraphics) {
                graphic.draw(canvas);
            }
        }



    }
}
