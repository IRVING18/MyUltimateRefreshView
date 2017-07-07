package ivring.ultimaterefreshview.subfragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ivring.refreshlibrary.interfaces.OnFooterRefreshListener;
import ivring.refreshlibrary.interfaces.OnHeaderRefreshListener;
import ivring.refreshlibrary.view.UltimateRefreshView;
import ivring.ultimaterefreshview.R;
import ivring.ultimaterefreshview.adapter.JDAppFooterAdapter;
import ivring.ultimaterefreshview.adapter.JDAppHeaderAdpater;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class ScrollViewFragment extends Fragment {

    private UltimateRefreshView mUltimateRefreshView;
    private Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getContext();
        View view = inflater.inflate(R.layout.fragment_scroller_view, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mUltimateRefreshView = (UltimateRefreshView) view.findViewById(R.id.refreshView);
        mUltimateRefreshView.setBaseHeaderAdapter(new JDAppHeaderAdpater(mContext));
        mUltimateRefreshView.setBaseFooterAdapter(new JDAppFooterAdapter(mContext));
        mUltimateRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(UltimateRefreshView view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mUltimateRefreshView.onHeaderRefreshComplete();
                    }
                },2000);
            }
        });
        mUltimateRefreshView.setOnFooterRefreshListener(new OnFooterRefreshListener() {
            @Override
            public void onFooterRefresh(UltimateRefreshView view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mUltimateRefreshView.onFooterRefreshComplete();
                    }
                },800);
            }
        });
    }

}
