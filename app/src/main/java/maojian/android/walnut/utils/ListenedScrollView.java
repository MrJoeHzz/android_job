package maojian.android.walnut.utils;

/**
 * Created by joe on 2017/4/12.
 */
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 带有滚动监听的ScrollView
 *
 * @author 拉风的道长（OSChina.net）
 *
 */
public class ListenedScrollView extends ScrollView {
    private static final String TAG = ListenedScrollView.class.getSimpleName();
    // 检查ScrollView的最终状态
    private static final int CHECK_STATE = 0;
    // 外部设置的监听方法
    private OnScrollListener onScrollListener;
    // 是否在触摸状态
    private boolean inTouch = false;

    public void setInTouch(boolean inTouch) {
        this.inTouch = inTouch;
    }

    // 上次滑动的最后位置
    private int lastT = 0;

    /**
     * 构造方法
     *
     * @param context
     */
    public ListenedScrollView(Context context) {
        super(context);
    }

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     */
    public ListenedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public ListenedScrollView(Context context, AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                inTouch = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                inTouch = false;

                lastT = getScrollY();
                checkStateHandler.removeMessages(CHECK_STATE);// 确保只在最后一次做这个check
                checkStateHandler.sendEmptyMessageDelayed(CHECK_STATE, 5);// 5毫秒检查一下

                break;
            default:
                break;
        }
        Log.d(TAG, "inTouch = " + inTouch);
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollListener == null) {
            return;
        }

        Log.d(TAG, "t = " + t + ", oldt = " + oldt);

        if (inTouch) {
            if (t != oldt) {
                // 有手指触摸，并且位置有滚动
                Log.i(TAG, "SCROLL_STATE_TOUCH_SCROLL");
                onScrollListener.onScrollStateChanged(this,
                        OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }
        } else {
            if (t != oldt) {
                // 没有手指触摸，并且位置有滚动，就可以简单的认为是在fling
                Log.w(TAG, "SCROLL_STATE_FLING");
                onScrollListener.onScrollStateChanged(this,
                        OnScrollListener.SCROLL_STATE_FLING);
                // 记住上次滑动的最后位置
                lastT = t;
                checkStateHandler.removeMessages(CHECK_STATE);// 确保只在最后一次做这个check
                checkStateHandler.sendEmptyMessageDelayed(CHECK_STATE, 5);// 5毫秒检查一下
            }
        }
        onScrollListener.onScrollChanged(l, t, oldl, oldt);
    }

    private Handler checkStateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (lastT == getScrollY()) {
                // 如果上次的位置和当前的位置相同，可认为是在空闲状态
                Log.e(TAG, "SCROLL_STATE_IDLE");
                onScrollListener.onScrollStateChanged(ListenedScrollView.this,
                        OnScrollListener.SCROLL_STATE_IDLE);
                // from http://blog.chinaunix.net/uid-20782417-id-1645164.html
                if (getScrollY() + getHeight() >= computeVerticalScrollRange()) {
                    onScrollListener.onBottomArrived();
                } else {
                    Log.d(TAG, "没有到最下方");
                }
            }
        }
    };

    /**
     * 设置滚动监听事件
     *
     * @param onScrollListener
     *            {@link OnScrollListener} 滚动监听事件（注意类的不同，虽然名字相同）
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }


}
