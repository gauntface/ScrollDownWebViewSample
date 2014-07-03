package hidingtitlebar.android.gauntface.com.hidingtitlebar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import hidingtitlebar.android.gauntface.com.hidingtitlebar.C;
import hidingtitlebar.android.gauntface.com.hidingtitlebar.R;

/**
 * Created by mattgaunt on 7/2/14.
 */
public class ScrollDownView extends FrameLayout {

    private View mTopBarView;
    private View mBackgroundView;

    private int mTouchSlop;
    private boolean mIsScrolling = false;
    private boolean mSkippedTouches = false;

    private int mTopBarTopOffset = 0;
    private int mTopBarHeight = 0;

    private float mInitTouchY = -1;
    private float mLastTouchY = -1;

    public ScrollDownView(Context context) {
        this(context, null);
    }

    public ScrollDownView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollDownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // This should live somewhere else
        mTopBarView = findViewById(R.id.activity_main_top_view);
        mBackgroundView = findViewById(R.id.activity_main_webview);

        if(mTopBarView != null && mBackgroundView != null) {
            mTopBarHeight = mTopBarView.getHeight();
            updateViewPositions();
        }
    }

    private void updateViewPositions() {
        LayoutParams topBarLayoutParams = (LayoutParams) mTopBarView.getLayoutParams();
        topBarLayoutParams.topMargin = mTopBarTopOffset;

        int backgroundTopOffset = mTopBarView.getHeight() + mTopBarTopOffset;
        LayoutParams backgroundLayoutParams = (LayoutParams) mBackgroundView.getLayoutParams();
        backgroundLayoutParams.topMargin = backgroundTopOffset;
        backgroundLayoutParams.height = getMeasuredHeight();

        requestLayout();
    }

    public boolean onInterceptTouchEvent (MotionEvent ev) {
        /**if(!(mTopBarTopOffset == -mTopBarHeight)) {
            // We still have some area to scroll off so intercept
            return true;
        }
        return super.onInterceptTouchEvent(ev);**/
        //return super.onInterceptTouchEvent(ev);
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int newTabBarPosition = mTopBarTopOffset;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mLastTouchY = event.getY();
                mInitTouchY = event.getY();
                mIsScrolling = false;

                mBackgroundView.onTouchEvent(MotionEvent.obtain(event));
                break;
            case MotionEvent.ACTION_MOVE:
                // If difference is negative, scroll the bar off screen
                // If difference is position, scroll onto screen
                float difference = event.getY() - mLastTouchY;
                newTabBarPosition = mTopBarTopOffset + (int) Math.floor(difference);
                if(newTabBarPosition > 0) {
                    newTabBarPosition = 0;
                } else if(newTabBarPosition < -mTopBarHeight) {
                    newTabBarPosition = -mTopBarHeight;
                }

                Log.v(C.TAG, "event.getY(): "+event.getY());
                Log.v(C.TAG, "mLastTouchY: "+mLastTouchY);
                Log.v(C.TAG, "difference: "+difference);
                Log.v(C.TAG, "mTopBarTopOffset: "+mTopBarTopOffset);
                Log.v(C.TAG, "newTabBarPosition: "+newTabBarPosition);
                Log.v(C.TAG, "======================================");

                mLastTouchY = event.getY();

                if(newTabBarPosition != mTopBarTopOffset) {
                    mTopBarTopOffset = newTabBarPosition;
                    updateViewPositions();
                }

                if(Math.abs(mInitTouchY - mLastTouchY) > mTouchSlop) {
                    mIsScrolling = true;
                }

                if(!mIsScrolling || mTopBarTopOffset == 0 || mTopBarTopOffset == -mTopBarHeight) {
                    if(mSkippedTouches) {
                        MotionEvent startEvent = MotionEvent.obtain(event);
                        startEvent.setAction(MotionEvent.ACTION_DOWN);
                        mBackgroundView.onTouchEvent(startEvent);
                        mSkippedTouches = false;
                    }

                    mBackgroundView.onTouchEvent(MotionEvent.obtain(event));
                } else {
                    mSkippedTouches = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                mInitTouchY = -1;
                mLastTouchY = -1;

                mBackgroundView.onTouchEvent(MotionEvent.obtain(event));
                break;
        }

        return true;
    }
}
