package com.csbm.atmaroundme.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.csbm.BEException;
import com.csbm.BEFile;
import com.csbm.GetDataCallback;

/**
 * Created by akela on 09/08/2016.
 */

public class BEImageView extends ImageView {
    private BEFile file;
    private Drawable placeholder;
    private boolean isLoaded = false;

    public BEImageView(Context context) {
        super(context);
    }

    public BEImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BEImageView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @SuppressLint("MissingSuperCall")
    protected void onDetachedFromWindow() {
        if(this.file != null) {
            this.file.cancel();
        }

    }

    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        this.isLoaded = true;
    }

    public void setPlaceholder(Drawable placeholder) {
        this.placeholder = placeholder;
        if(!this.isLoaded) {
            this.setImageDrawable(this.placeholder);
        }

    }

    public void setBEFile(BEFile file) {
        if(this.file != null) {
            this.file.cancel();
        }

        this.isLoaded = false;
        this.file = file;
        this.setImageDrawable(this.placeholder);
    }

    public void loadInBackground() {
        this.loadInBackground((GetDataCallback)null);
    }

    public void loadInBackground(final GetDataCallback completionCallback) {
        if(this.file == null) {
            if(completionCallback != null) {
                completionCallback.done((byte[])null, (BEException)null);
            }

        } else {
            final BEFile loadingFile = this.file;
            this.file.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, BEException e) {
                    if(BEImageView.this.file == loadingFile) {
                        if(data != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            if(bitmap != null) {
                                BEImageView.this.setImageBitmap(bitmap);
                            }
                        }

                        if(completionCallback != null) {
                            completionCallback.done(data, e);
                        }

                    }
                }
            });
        }
    }
}
