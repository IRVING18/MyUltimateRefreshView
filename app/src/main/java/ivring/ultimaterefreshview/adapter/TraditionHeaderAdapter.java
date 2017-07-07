package ivring.ultimaterefreshview.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import ivring.refreshlibrary.adapter.BaseHeaderAdapter;
import ivring.ultimaterefreshview.R;


/**
 * Created by IVRING on 2017/4/30.
 */

public class TraditionHeaderAdapter extends BaseHeaderAdapter {
    private ImageView pull_to_refresh_image;
    private ImageView pull_to_refresh_image1;
    private TextView pull_to_refresh_text;
    //    用于判断是否进行箭头向上旋转动作
    boolean ifRotateUpFlag = true;
    //
    private RotateAnimation mFlipAnimation;
//    private RotateAnimation mReverseFlipAnimation;

    public TraditionHeaderAdapter(Context context) {
        super(context);
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
    }

    @Override
    public View getHeaderView() {
        View mView = mInflater.inflate(R.layout.tradition_header_refresh_layout, null, false);
        pull_to_refresh_image = (ImageView) mView.findViewById(R.id.pull_to_refresh_image);
        pull_to_refresh_image1 = (ImageView) mView.findViewById(R.id.pull_to_refresh_image1);
        pull_to_refresh_text = (TextView) mView.findViewById(R.id.pull_to_refresh_text);
        return mView;
    }

    /**
     * 用于下拉时执行的方法，传来的值时手势下拉距离，但是不要用于做缩放相关操作。问题总结在MeiTuan里有。
     * @param deltaY 下拉的距离
     */
    @Override
    public void pullViewToRefreshDeltaY(int deltaY) {
        pull_to_refresh_text.setText("下拉刷新");
//        将是否可旋转标志置为true，以便进入releaseVIewToRefresh时旋转箭头
        ifRotateUpFlag = true;
//        将图片设置为向下箭头，重新设置，是因为如果用户下拉至releaseViewToRefresh不松手拉回来时要将箭头设置回来
        pull_to_refresh_image.clearAnimation();
        pull_to_refresh_image.setImageResource(R.drawable.erg);
    }

    /**
     * 用于做缩放的下拉方法
     * @param scale
     */
    @Override
    public void pullViewToRefreshScale(float scale) {

    }

    /**
     * 下拉至HeadView完全显示时的方法
     * @param deltaY 下拉的距离
     */
    @Override
    public void releaseViewToRefresh(int deltaY) {
//        判断是否旋转图片
        if (ifRotateUpFlag) {
            pull_to_refresh_image.clearAnimation();
            pull_to_refresh_image.startAnimation(mFlipAnimation);
            ifRotateUpFlag = false;
        }
        pull_to_refresh_text.setText("释放立即刷新");
    }

    /**
     * 松手刷新方法
     */
    @Override
    public void headerRefreshing() {
        ifRotateUpFlag = true;

        pull_to_refresh_image.setImageDrawable(null);
        pull_to_refresh_image.clearAnimation();
        pull_to_refresh_image.setVisibility(View.GONE);
        pull_to_refresh_image1.setVisibility(View.VISIBLE);
        pull_to_refresh_image1.setImageResource(R.drawable.simple_loading);
        AnimationDrawable mAnimationDrawable = (AnimationDrawable) pull_to_refresh_image1.getDrawable();
        mAnimationDrawable.start();
        pull_to_refresh_text.setText("正在刷新…");
    }

    /**
     * 刷新完成后的方法。
     */
    @Override
    public void headerRefreshComplete() {
        ifRotateUpFlag = true;

        pull_to_refresh_image.setVisibility(View.VISIBLE);
        pull_to_refresh_image1.setVisibility(View.GONE);
        pull_to_refresh_image1.clearAnimation();
        pull_to_refresh_image.setImageResource(R.drawable.erg);
        pull_to_refresh_text.setVisibility(View.VISIBLE);
        pull_to_refresh_text.setText("");
    }
}
