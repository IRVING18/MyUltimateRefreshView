package ivring.ultimaterefreshview.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import ivring.refreshlibrary.adapter.BaseHeaderAdapter;
import ivring.refreshlibrary.utils.MeasureTools;
import ivring.ultimaterefreshview.R;

import static android.content.ContentValues.TAG;

/**
 * Created by engineer on 2017/4/30.
 */

public class MeiTuanHeaderAdapter extends BaseHeaderAdapter {

    private ImageView loading;
    private int viewHeight;
    private float pull_distance = 0;
    private boolean isFanGunAnimation;

    public MeiTuanHeaderAdapter(Context context) {
        super(context);
    }

    /**
     * 获取头布局的方法，再UltimateRefreshView中用到了
     *
     * @return
     */
    @Override
    public View getHeaderView() {
        View mView = mInflater.inflate(R.layout.meituan_header_refresh_layout, null, false);
        loading = (ImageView) mView.findViewById(R.id.loading);
        MeasureTools.measureView(mView);
        viewHeight = mView.getMeasuredHeight();
        return mView;
    }

    /**
     * 当头布局从隐藏状态到显示状态的过程中，手势当对于Down点的Y轴坐标变化
     *
     * @param deltaY 下拉的距离
     */
    @Override
    public void pullViewToRefreshDeltaY(int deltaY) {
//        //这里乘以0.3 是因为UltimateRefreshView 源码中对于滑动有0.3的阻尼系数，为了保持一致
//        Log.i(TAG, "pullViewToRefreshDeltaY: "+deltaY);
//        pull_distance=pull_distance+deltaY *0.3f;
//        float scale = pull_distance / viewHeight;
//        Log.i(TAG, "pullViewToRefreshDeltaY: scale== "+scale);
////        pull_distance = 0;
//            loading.setScaleX(scale);
//            loading.setScaleY(scale);

    }

    /**
     * 直接传回根据HeaderView的隐藏到展示过程的缩放比例
     *
     * @param scale
     */
    @Override
    public void pullViewToRefreshScale(float scale) {
        Log.i(TAG, "pullViewToRefreshScale: scale== " + scale);
//        pull_distance = 0;
        if (scale >= 0 && scale <= 1) {
            loading.setScaleX(scale);
            loading.setScaleY(scale);
        }
//        可以设置小孩翻转回圈圈
        if (isFanGunAnimation){
            isFanGunAnimation = !isFanGunAnimation;
            loading.setImageResource(R.drawable.mei_tuan_loading_pre_re);
            AnimationDrawable mAnimationDrawable = (AnimationDrawable) loading.getDrawable();
            mAnimationDrawable.start();
        }
    }


    /**
     * 当HeaderView完全显示的时候执行的方法。
     * <p/>将加载图片设置成从椭圆小孩变成小孩的翻滚动画
     *
     * @param deltaY 下拉的距离
     */
    @Override
    public void releaseViewToRefresh(int deltaY) {
        isFanGunAnimation = true;
//        手势过快的时候会出现，图片没有完全放大就进入这个方法了，所以先设置缩放大小
        loading.setScaleX(1);
        loading.setScaleY(1);
        loading.setImageResource(R.drawable.mei_tuan_loading_pre);
        AnimationDrawable mAnimationDrawable = (AnimationDrawable) loading.getDrawable();
        mAnimationDrawable.start();
    }

    /**
     * 设置正在刷新的时候执行的方法。
     * <p/>设置正在刷新的动画
     */
    @Override
    public void headerRefreshing() {
        isFanGunAnimation = false;
        loading.setImageResource(R.drawable.mei_tuan_loading);
        AnimationDrawable mAnimationDrawable = (AnimationDrawable) loading.getDrawable();
        mAnimationDrawable.start();
    }

    /**
     * 刷新结束后执行的方法。
     * <p/>将加载图片重新变回椭圆形状的
     */
    @Override
    public void headerRefreshComplete() {
        isFanGunAnimation = false;
        loading.setImageResource(R.drawable.pull_image);
        loading.setScaleX(0);
        loading.setScaleY(0);
    }
}
