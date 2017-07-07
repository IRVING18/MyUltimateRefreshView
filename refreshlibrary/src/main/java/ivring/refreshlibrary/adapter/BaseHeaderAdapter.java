package ivring.refreshlibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by engineer on 2017/4/26.
 */

public abstract class BaseHeaderAdapter {

    protected LayoutInflater mInflater;


    public BaseHeaderAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 获取headerView
     *
     * @return
     */
    public abstract View getHeaderView();

    /**
     * <p/>顶部headView 被下拉时此事件发生
     * <p/>(传回手势下拉距离，不要用于缩放，有问题，
     * <p/>因为在头布局下拉过程不设限制，头布局顶部坐标可以超出顶部，
     * <p/>而在返回这个值时限制是头布局定坐标要小于0，而满足小于0时，
     * <p/>delayY可能已经变化了很多，返回时会出现缩放问题。如果没想明白不要纠结，
     * <p/>因为这个问题想明白花费了我4个小时左右，不要强求，2017.7.5号IVRING明白过就OK了)
     *
     * @param deltaY 下拉的距离
     */
    public abstract void pullViewToRefreshDeltaY(int deltaY);

    /**
     * 直接传回缩放比例(按照头部布局的缩放传回)
     *
     * @author IVRING
     * @time 2017/7/6 10:21
     */
    public abstract void pullViewToRefreshScale(float scale);

    /**
     * 顶部headView 下拉后，完全显示时 此事件发生
     *
     * @param deltaY 下拉的距离
     */
    public abstract void releaseViewToRefresh(int deltaY);

    /**
     * 顶部headView 正在刷新
     */
    public abstract void headerRefreshing();

    /**
     * 顶部headView 完成刷新
     */
    public abstract void headerRefreshComplete();


}
