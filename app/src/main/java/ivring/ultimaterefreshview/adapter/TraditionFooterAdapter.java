package ivring.ultimaterefreshview.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ivring.refreshlibrary.adapter.BaseFooterAdapter;
import ivring.ultimaterefreshview.R;


/**
 * Created by IVRING on 2017/4/30.
 */

public class TraditionFooterAdapter extends BaseFooterAdapter {
    private ProgressBar pull_to_load_progress;
    private ImageView pull_to_load_image;
    private TextView pull_to_load_text;

    public static final String TAG = "zheng";
    //    用于判断是否进行箭头向下旋转动作
    boolean ifRotateDownFlag = true;

    private RotateAnimation mFlipAnimation;

    public TraditionFooterAdapter(Context context) {
        super(context);
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
    }

    @Override
    public View getFooterView() {
        View mView = mInflater.inflate(R.layout.tradition_footer_refresh_layout, null, false);
        pull_to_load_progress = (ProgressBar) mView.findViewById(R.id.pull_to_load_progress);
        pull_to_load_image = (ImageView) mView.findViewById(R.id.pull_to_load_image);
        pull_to_load_text = (TextView) mView.findViewById(R.id.pull_to_load_text);
        return mView;
    }

    @Override
    public void pullViewToRefreshDeltaY(int deltaY) {
        pull_to_load_text.setText("上拉加载更多…");
    }

    //    用于记录上一次滑动距离
    float lastUpMoveY;

    @Override
    public void pullViewToRefresh(float upMoveY, float footViewHeight) {
        ifRotateDownFlag = true;
//        如果向下滑，就让箭头冲上
        if (upMoveY - lastUpMoveY < 0) {
            pull_to_load_image.clearAnimation();
            pull_to_load_image.setImageResource(R.drawable.ic_pulltorefresh_arrow_up);
        }
        lastUpMoveY = upMoveY;
    }

    @Override
    public void releaseViewToRefresh(int deltaY) {
        Log.i(TAG, "releaseViewToRefresh: ");
        if (ifRotateDownFlag) {
            pull_to_load_image.clearAnimation();
            pull_to_load_image.startAnimation(mFlipAnimation);
            ifRotateDownFlag = false;
        }
        pull_to_load_text.setText("松开后加载");
    }

    @Override
    public void footerRefreshing() {
        ifRotateDownFlag = true;
        Log.i(TAG, "footerRefreshing: ");
        pull_to_load_text.setText("加载中");
        pull_to_load_image.setImageDrawable(null);
        pull_to_load_image.clearAnimation();
        pull_to_load_image.setVisibility(View.GONE);
        pull_to_load_progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void footerRefreshComplete() {
        ifRotateDownFlag = true;
        Log.i(TAG, "footerRefreshComplete: ");
        pull_to_load_progress.setVisibility(View.GONE);
        pull_to_load_image.setImageResource(R.drawable.ic_pulltorefresh_arrow_up);
        pull_to_load_image.setVisibility(View.VISIBLE);
        pull_to_load_text.setVisibility(View.VISIBLE);
        pull_to_load_text.setText("加载完成…");
    }
}
