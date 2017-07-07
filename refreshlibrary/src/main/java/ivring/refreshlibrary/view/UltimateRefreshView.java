package ivring.refreshlibrary.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import ivring.refreshlibrary.adapter.BaseFooterAdapter;
import ivring.refreshlibrary.adapter.BaseHeaderAdapter;
import ivring.refreshlibrary.adapter.InitFooterAdapter;
import ivring.refreshlibrary.adapter.InitHeaderAdapter;
import ivring.refreshlibrary.interfaces.OnFooterRefreshListener;
import ivring.refreshlibrary.interfaces.OnHeaderRefreshListener;
import ivring.refreshlibrary.utils.MeasureTools;


/**
 * Created by IVRING on 2017/4/19.
 */

public class UltimateRefreshView extends LinearLayout {

    private static final String TAG = UltimateRefreshView.class.getSimpleName();
    // 刷新时状态
//    下拉过程
    private static final int PULL_TO_REFRESH = 2;
    //    返回过程
    private static final int RELEASE_TO_REFRESH = 3;
    //    刷新中
    private static final int REFRESHING = 4;
    // pull state
//    上拉加载状态
    private static final int PULL_UP_STATE = 0;
    //    下拉刷新状态
    private static final int PULL_DOWN_STATE = 1;

    private int mPullState;

    private int animDuration = 300;//头、尾 部回弹动画执行时间
    //    阻尼系数，可以用来控制下拉刷新动作的灵敏度
    public static final float DAMPING_FACTOR = 0.3f;
    /**
     * list or grid
     */
    private AdapterView<?> mAdapterView;
    /**
     * RecyclerView
     */
    private RecyclerView mRecyclerView;
    /**
     * ScrollView
     */
    private ScrollView mScrollView;
    /**
     * WebView
     */
    private WebView mWebView;


    //Header
    private int mHeaderState;
    private View mHeaderView;
    private int mHeadViewHeight;
    //Footer
    private int mFooterState;
    private View mFooterView;
    private int mFooterViewHeight;
    //action
    private int lastY;

    private BaseHeaderAdapter mBaseHeaderAdapter;
    private BaseFooterAdapter mBaseFooterAdapter;
    private OnHeaderRefreshListener mOnHeaderRefreshListener;
    private OnFooterRefreshListener mOnFooterRefreshListener;

    private Context mContext;


    public UltimateRefreshView(Context context) {
        super(context);
        init(context);
    }

    public UltimateRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UltimateRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //设置为垂直布局，避免每次在xml中修改(LinearLayout 默认为horizontal)
        setOrientation(VERTICAL);
        mContext = context;

    }

    ///////////////////////////////////////////////////////////////////////////
    // 设置手势
    ///////////////////////////////////////////////////////////////////////////

    /**
     * viewGroup中用于拦截手势事件的方法。
     *
     * @param ev
     * @return 返回true时就拦截，及会触发viewgroup中的onTouchEvent方法。
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        getRawY获取点击点到屏幕最顶的距离，getY获取的是view内部的位置。
        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                保存按下时的y的坐标
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
//                用当前y的坐标减去down时的坐标得到y轴上移动的距离
                int deltaY = y - lastY;
//                判断是否消费
                if (isParentViewScroll(deltaY)) {
                    Log.e(TAG, "onInterceptTouchEvent: belong to ParentView");
                    return true; //此时,触发onTouchEvent事件
                }
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * 当onInterceptTouchEvent返回值为true时触发本方法
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
//                lastY是在onInterceptTouchEvent中DOWN状态下获取的
                int deltaY = y - lastY;
//                当pull状态是下拉刷新时
                if (mPullState == PULL_DOWN_STATE) {
                    Log.e(TAG, "onTouchEvent: pull down begin-->" + deltaY);
                    initHeaderViewToRefresh(deltaY);
//                    当上拉加载时
                } else if (mPullState == PULL_UP_STATE) {
                    initFooterViewToRefresh(deltaY);
                }
                lastY = y;
                break;
//            当手势抬起，或者手势滑出view时
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                获取当前头布局的顶部坐标
                int topMargin = getHeaderTopMargin();
                Log.e(TAG, "onTouchEvent: topMargin==" + topMargin);
//                下拉刷新时
                if (mPullState == PULL_DOWN_STATE) {
//                    当头布局的顶部坐标》=0，及头布局完全显示时
                    if (topMargin >= 0) {
//                        设置头布局全部显示，并设置回调刷新的接口
                        headerRefreshing();
//                        如果头布局的顶部坐标小与0，及未完全显示出来，就不刷新而是返回隐藏状态。
                    } else {
                        reSetHeaderTopMargin(-mHeadViewHeight);
                    }
//                    上拉加载
                } else if (mPullState == PULL_UP_STATE) {
                    if (Math.abs(topMargin) >= mHeadViewHeight
                            + mFooterViewHeight) {
                        // 开始执行footer 刷新
                        footerRefreshing();
                    } else {
                        // 还没有执行刷新，重新隐藏？？？？不是我写注释，估计是错了，先往下看了，以后再看
                        reSetHeaderTopMargin(-mHeadViewHeight);
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }
    /**
     * 滑动由父View（当前View）处理，
     * <p/>用于判断是否拦截事件，是否触发viewgroup（及本自定义view）中的onTouchEvent事件
     *
     * @param deltaY
     * @return
     */
    private boolean isParentViewScroll(int deltaY) {
        boolean belongToParentView = false;
//        当正在刷新状态时不消费
        if (mHeaderState == REFRESHING) {
            belongToParentView = false;
        }
        //list or grid的处理##############################################################
        if (mAdapterView != null) {

            if (deltaY > 0) {
                View child = mAdapterView.getChildAt(0);
                if (child == null) {
                    belongToParentView = false;
                } else if (mAdapterView.getFirstVisiblePosition() == 0 && child.getTop() == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
            } else if (deltaY < 0) {
                View lastChild = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
                if (lastChild == null) {
                    // 如果mAdapterView中没有数据,不拦截
                    belongToParentView = false;
                }
                // 最后一个子view的Bottom小于父View的高度说明mAdapterView的数据没有填满父view,
                // 等于父View的高度说明mAdapterView已经滑动到最后
                else if (lastChild.getBottom() <= getHeight()
                        && mAdapterView.getLastVisiblePosition() == mAdapterView
                        .getCount() - 1) {
                    mPullState = PULL_UP_STATE;
                    belongToParentView = true;
                }
            }
        }
        //RecyclerView时的处理##############################################################
        else if (mRecyclerView != null) {
//            如果y轴上的移动距离大于0
            if (deltaY > 0) {
//                获取RecyclerView第一个子view
                View child = mRecyclerView.getChildAt(0);
                if (child == null) {
                    belongToParentView = false;
                }
                LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
//                获取最上层完全显示的item的坐标
                int firstPosition = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
//                如果最上层完全显示的item的potion是0，就消费并将状态设置成下拉刷新状态。
                if (firstPosition == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
//                如果移动距离是负数，及向上移动中,可能是上拉加载
            } else if (deltaY < 0) {
                View child = mRecyclerView.getChildAt(0);
                if (child == null) {
                    belongToParentView = false;
                }
//                computeVerticalScrollExtent()是当前屏幕显示的区域高度，
//                computeVerticalScrollOffset() 是当前屏幕之前滑过的距离，
//                而computeVerticalScrollRange()是整个View控件的高度。
                if (mRecyclerView.computeVerticalScrollExtent() + mRecyclerView.computeVerticalScrollOffset()
                        >= mRecyclerView.computeVerticalScrollRange()) {
                    belongToParentView = true;
                    mPullState = PULL_UP_STATE;
                } else {
                    belongToParentView = false;
                }
            }
        }
        //##############################################################
        else if (mScrollView != null) {
            View child = mScrollView.getChildAt(0);
            if (deltaY > 0) {

                if (child == null) {
                    belongToParentView = false;
                }

                int distance = mScrollView.getScrollY();
                if (distance == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
            } else if (deltaY < 0
                    && child.getMeasuredHeight() <= getHeight()
                    + mScrollView.getScrollY()) {
                mPullState = PULL_UP_STATE;
                belongToParentView = true;

            }
        }
        //##############################################################
        else if (mWebView != null) {
            View child = mWebView.getChildAt(0);
            if (deltaY > 0) {

                if (child == null) {
                    belongToParentView = false;
                }

                int distance = mWebView.getScrollY();
                if (distance == 0) {
                    mPullState = PULL_DOWN_STATE;
                    belongToParentView = true;
                }
            }
        }


        return belongToParentView;
    }
    ///////////////////////////////////////////////////////////////////////////
    // 设置头部下拉刷新

    ///////////////////////////////////////////////////////////////////////////

    /**
     * 对外暴露的设置头部adapter的方法
     *
     * @param baseHeaderAdapter
     */
    public void setBaseHeaderAdapter(BaseHeaderAdapter baseHeaderAdapter) {
        mBaseHeaderAdapter = baseHeaderAdapter;
//        初始化头布局
        initHeaderView();
//        获取嵌套的布局，RecyclerView？webView？ScrollView？ListView GridView？
        initSubViewType();
    }

    /**
     * 对外暴露设置默认头部adapter的方法
     */
    public void setBaseHeaderAdapter() {
        mBaseHeaderAdapter = new InitHeaderAdapter(mContext);
        initHeaderView();
        initSubViewType();
    }


    /**
     * 计算顶部view 高度，将其隐藏
     */
    private void initHeaderView() {
//        从adapter中获取头布局
        mHeaderView = mBaseHeaderAdapter.getHeaderView();
//        设置头部布局的高宽。
        MeasureTools.measureView(mHeaderView);
//        获取头布局的高度
        mHeadViewHeight = mHeaderView.getMeasuredHeight();
//        创建一个布局对象
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mHeadViewHeight);
//        设置对象的顶部坐标，设置为头布局的高度的负数，达到隐藏的目的。
        params.topMargin = -mHeadViewHeight;
//        添加到设置头部布局的params，并添加到顶部，-1时添加到最后
        addView(mHeaderView, 0, params);

    }


    /**
     * 确定UltimateRefreshView 内部子视图类型
     */
    private void initSubViewType() {
        int count = getChildCount();
        if (count < 2) {
            return;
        }

        View view = getChildAt(1);

        if (view instanceof AdapterView<?>) {
            mAdapterView = (AdapterView<?>) view;
        }

        if (view instanceof RecyclerView) {
            mRecyclerView = (RecyclerView) view;
        }

        if (view instanceof ScrollView) {
            mScrollView = (ScrollView) view;
        }

        if (view instanceof WebView) {
            mWebView = (WebView) view;
        }

    }

    /**
     * 计算下拉刷新相关
     * <p/>计算当头布局的顶部坐标从负数（隐藏），到0（显示完全）过程中，给adapter的回调方法。
     *
     * @param deltaY
     */
    private void initHeaderViewToRefresh(int deltaY) {
        if (mBaseHeaderAdapter == null) {
            return;
        }
//        获取当前头部下拉布局的顶部坐标
        int topDistance = UpdateHeadViewMarginTop(deltaY);
//        如果顶部坐标在负的自身高度---》0时，默认状态下头布局是隐藏，及顶部坐标为自身高度的负数
        if (topDistance <= 0 && topDistance >= -mHeadViewHeight) {
//            用于传回scale缩放比例，
//            将topDistance切换成正数，这样-mHeadViewHeight的时候就相当于0,为了方便计算scale
            float i1 = (float) topDistance + (float) mHeadViewHeight;
//            计算scale
            float scale = i1 / mHeadViewHeight;
            Log.i(TAG, "pullViewToRefreshScale: " + i1);
//            设置给adapter中方法。
            mBaseHeaderAdapter.pullViewToRefreshScale(scale);
//            传回手势Y坐标，使用时注意，这个坐标传回的不能用来做缩放类似的操作，因为他不准确，详情见笔记分析。
            mBaseHeaderAdapter.pullViewToRefreshDeltaY(deltaY);
//            设置状态为下拉状态
            mHeaderState = PULL_TO_REFRESH;
//            如果头布局顶部坐标》0了，就说明头布局已经完全显示出来了，
        } else if (topDistance > 0 && mHeaderState != RELEASE_TO_REFRESH) {
//            设置给adapter中头布局重回隐藏状态时的方法
            mBaseHeaderAdapter.releaseViewToRefresh(deltaY);
            Log.i(TAG, "initHeaderViewToRefresh: +zheng22+topdistance>0" + topDistance);
            mHeaderState = RELEASE_TO_REFRESH;
        }

    }


    /**
     * 根据y上的滑动距离（deltaY）设置头布局的顶部坐标，并将设置完成后的头布局顶部坐标返回
     *
     * @param deltaY
     * @return 返回当前头部下拉布局的顶部坐标
     */
    private int UpdateHeadViewMarginTop(int deltaY) {
//        设置头部刷新布局的顶部坐标，默认隐藏状态是负的自己的高度。
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
//        设置顶部坐标，*0.3是阻尼系数，及下拉1px时，让顶部布局下移0.3px
        float topMargin = params.topMargin + deltaY * DAMPING_FACTOR;
        params.topMargin = (int) topMargin;
        mHeaderView.setLayoutParams(params);
//        调用重绘方法
        invalidate();
        return params.topMargin;
    }
    /**
     * 用于FootVIew设置时，y上的滑动距离（deltaY）设置头布局的顶部坐标，并将设置完成后的头布局顶部坐标返回
     *因为如果topMargin大于-mHeadViewHeight了，HeadView会显示出来,所以另起方法
     * @param deltaY
     * @return 返回当前头部下拉布局的顶部坐标
     */
    private int FootViewControlUpdateHeadViewMarginTop(int deltaY) {
//        设置头部刷新布局的顶部坐标，默认隐藏状态是负的自己的高度。
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
//        设置顶部坐标，*0.3是阻尼系数，及下拉1px时，让顶部布局下移0.3px
        float topMargin = params.topMargin + deltaY * DAMPING_FACTOR;
//        因为如果topMargin大于-mHeadViewHeight了，HeadView会显示出来
        Log.i(TAG, "FootViewControlUpdateHeadViewMarginTop:zhengzheng "+topMargin);
        if (topMargin <= -mHeadViewHeight) {
            params.topMargin = (int) topMargin;
            mHeaderView.setLayoutParams(params);
//        调用重绘方法
            invalidate();
        }
        return params.topMargin;
    }


    /**
     * 头布局正在刷新状态
     */
    private void headerRefreshing() {
        if (mBaseHeaderAdapter == null) {
            return;
        }
//      设置状态为正在刷新
        mHeaderState = REFRESHING;
//        设置头布局的顶部坐标为0，及让头布局完全显示出来
        setHeaderTopMargin(0);
//        调用adapter的headerRefreshing方法。
        mBaseHeaderAdapter.headerRefreshing();
//        设置回调接口，下拉刷新
        if (mOnHeaderRefreshListener != null) {
            mOnHeaderRefreshListener.onHeaderRefresh(this);
        }
    }

    /**
     * 头布局刷新完成时调用方法
     */
    public void onHeaderRefreshComplete() {
        if (mBaseHeaderAdapter == null) {
            return;
        }
        setHeaderTopMargin(-mHeadViewHeight);
        mBaseHeaderAdapter.headerRefreshComplete();
        mHeaderState = PULL_TO_REFRESH;
    }


    /**
     * 获取当前header view 的topMargin
     *
     * @return
     * @description
     */

    private int getHeaderTopMargin() {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        return params.topMargin;
    }


    /**
     * 设置header view 的topMargin的值
     *
     * @param topMargin ，为0时，说明header view 刚好完全显示出来； 为-mHeaderViewHeight时，说明完全隐藏了
     * @description
     */
    private void setHeaderTopMargin(int topMargin) {

        smoothMargin(topMargin);
    }

    /**
     * 上拉或下拉至一半时，放弃下来，视为完成一次下拉统一处理，初始化所有内容
     *
     * @param topMargin
     */
    private void reSetHeaderTopMargin(int topMargin) {

        if (mBaseHeaderAdapter != null) {
            mBaseHeaderAdapter.headerRefreshComplete();
        }

        if (mBaseFooterAdapter != null) {
            mBaseFooterAdapter.footerRefreshComplete();
        }

        smoothMargin(topMargin);
    }

    /**
     * 平滑设置header view 的margin
     *
     * @param topMargin
     */
    private void smoothMargin(int topMargin) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, topMargin);
        animator.setDuration(animDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeadViewHeight);
                lp.topMargin = (int) animation.getAnimatedValue();
//                设置头布局的大小
                mHeaderView.setLayoutParams(lp);
            }
        });
        animator.start();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 用于设置上拉刷新的Footer部分
    //////////////////////////////////////////////////////////////////////////

    /**
     * 设置上拉加载的adapter
     */
    public void setBaseFooterAdapter() {
        mBaseFooterAdapter = new InitFooterAdapter(mContext);
        initFooterView();
    }

    public void setBaseFooterAdapter(BaseFooterAdapter baseFooterAdapter) {
        mBaseFooterAdapter = baseFooterAdapter;
        initFooterView();
    }

    /**
     * 初始化底部布局
     */
    private void initFooterView() {
        mFooterView = mBaseFooterAdapter.getFooterView();
        MeasureTools.measureView(mFooterView);
        mFooterViewHeight = mFooterView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                mFooterViewHeight);
//        相当于addView(mFooterView,-1, params);就是讲view加到最底部
        addView(mFooterView, params);
    }


    /**
     * 初始化上拉加载
     * @param deltaY
     */
    private void initFooterViewToRefresh(int deltaY) {
        if (mBaseFooterAdapter == null) {
            return;
        }
//      原理：设置HeadView的高度，是因为这层嵌套就是将头部隐藏，底部设置addview为-1（实际也是隐藏效果），
//      然后监听onInterceptTouchEvent事件，判断是否拦截事件，拦截之后相当于消费了RecyclerView的上下滑动事件，
//      然后RecyclerView显示的部分就是现在显示高度了，然后通过设置头部的位置来实现将footerView和headview的显示隐藏
//      说明：设置顶部不要超过0，不然会显示出来headerView。
//        if (deltaY)
        int topDistance = UpdateHeadViewMarginTop(deltaY);

        Log.e("zzz", "the distance  is " + topDistance + "     "+mFooterViewHeight + "     "+mHeadViewHeight);

        // 如果header view topMargin 的绝对值大于或等于footerview的高度，
        // 说明footer view 完全显示出来了，修改footer view 的提示状态

        if (Math.abs(topDistance) <= (mHeadViewHeight + mFooterViewHeight)) {
            mBaseFooterAdapter.pullViewToRefreshDeltaY(deltaY);
            float upMove = Math.abs(topDistance) - mHeadViewHeight;
            mBaseFooterAdapter.pullViewToRefresh(upMove,mFooterViewHeight);
            mFooterState = PULL_TO_REFRESH;
        } else if (Math.abs(topDistance) > (mHeadViewHeight + mFooterViewHeight)) {
            mBaseFooterAdapter.releaseViewToRefresh(deltaY);
            mFooterState = RELEASE_TO_REFRESH;
        }
    }


    private void footerRefreshing() {
        if (mBaseFooterAdapter == null) {
            return;
        }

        mFooterState = REFRESHING;
        int top = mHeadViewHeight + mFooterViewHeight;
//        设置HeadView的顶坐标为top时FootVIew时完全显示的
        setHeaderTopMargin(-top);
        mBaseFooterAdapter.footerRefreshing();
        if (mOnFooterRefreshListener != null) {
            mOnFooterRefreshListener.onFooterRefresh(this);
        }
    }

    public void onFooterRefreshComplete() {
        if (mBaseFooterAdapter == null) {
            return;
        }
        setHeaderTopMargin(-mHeadViewHeight);
        mBaseFooterAdapter.footerRefreshComplete();
        mFooterState = PULL_TO_REFRESH;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 回调接口
    ///////////////////////////////////////////////////////////////////////////
    public void setOnHeaderRefreshListener(OnHeaderRefreshListener onHeaderRefreshListener) {
        mOnHeaderRefreshListener = onHeaderRefreshListener;
    }

    public void setOnFooterRefreshListener(OnFooterRefreshListener onFooterRefreshListener) {
        mOnFooterRefreshListener = onFooterRefreshListener;
    }
}
