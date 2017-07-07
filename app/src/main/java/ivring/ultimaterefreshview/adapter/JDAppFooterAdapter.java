package ivring.ultimaterefreshview.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import ivring.refreshlibrary.adapter.BaseFooterAdapter;
import ivring.ultimaterefreshview.R;


/**
 * Created by IVRING on 2017/4/30.
 */

public class JDAppFooterAdapter extends BaseFooterAdapter {
    private ImageView loading;
    private Context mContext;

    public JDAppFooterAdapter(Context context) {
        super(context);
        mContext=context;
    }

    @Override
    public View getFooterView() {
        View footerView = mInflater.inflate(R.layout.jd_footer_refresh_layout, null, false);
        loading = (ImageView) footerView.findViewById(R.id.loading);
        return footerView;
    }

    @Override
    public void pullViewToRefreshDeltaY(int deltaY) {
        Glide.with(mContext).load(R.drawable.jd_loading_logo).into(loading);
    }

    @Override
    public void pullViewToRefresh(float upMoveY, float footViewHeight) {

    }

    @Override
    public void releaseViewToRefresh(int deltaY) {

    }

    @Override
    public void footerRefreshing() {

    }

    @Override
    public void footerRefreshComplete() {
        loading.setImageDrawable(null);
    }
}
