package com.fanfou.app.hd.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.fanfou.app.hd.App;
import com.fanfou.app.hd.R;
import com.fanfou.app.hd.adapter.BaseCursorAdapter;
import com.fanfou.app.hd.controller.PopupController;
import com.fanfou.app.hd.dao.model.StatusModel;
import com.fanfou.app.hd.service.Constants;
import com.fanfou.app.hd.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @author mcxiaoke
 * @version 1.0 2012.02.06
 * @version 1.1 2012.02.08
 * @version 1.2 2012.02.09
 * @version 1.3 2012.02.22
 * @version 1.4 2012.02.24
 * @version 1.5 2012.02.28
 * @version 1.6 2012.03.02
 * 
 */
public abstract class PullToRefreshListFragment extends AbstractListFragment
		implements OnRefreshListener, OnItemLongClickListener,
		LoaderCallbacks<Cursor> {

	private static final int LOADER_ID = 1;

	protected static final String TAG = PullToRefreshListFragment.class
			.getSimpleName();

	protected PullToRefreshListView mPullToRefreshView;
	protected ListView mListView;

	private Parcelable mParcelable;

	private BaseCursorAdapter mAdapter;
	private Cursor mCursor;

	private Handler mHandler = new Handler();

	private boolean busy;
	public PullToRefreshListFragment() {
		super();
		if (App.DEBUG) {
			Log.d(TAG, "PullToRefreshListFragment() id=" + this);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (App.DEBUG) {
			Log.d(TAG, "onAttach() isVisible=" + isVisible());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.DEBUG) {
			Log.d(TAG, "onCreate() isVisible=" + isVisible());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (App.DEBUG) {
			Log.d(TAG, "onCreateView() isVisible=" + isVisible());
		}
		View v = inflater.inflate(R.layout.fm_pull_list, container, false);
		mPullToRefreshView = (PullToRefreshListView) v;
		mPullToRefreshView.setOnRefreshListener(this);
		mListView = mPullToRefreshView.getRefreshableView();
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (App.DEBUG) {
			Log.d(TAG, "onActivityCreated() isVisible=" + isVisible());
		}

		if (savedInstanceState != null) {
			mParcelable = savedInstanceState.getParcelable("state");
		}
		mAdapter = (BaseCursorAdapter) onCreateAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(mAdapter);
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (App.DEBUG) {
			Log.d(TAG, "onHiddenChanged() hidden=" + hidden + " isVisible="
					+ isVisible());
		}
	}

	protected abstract CursorAdapter onCreateAdapter();

	protected abstract void doFetch(boolean doGetMore);

	protected abstract void showToast(int count);

	protected abstract int getType();

	@Override
	public void onRefresh() {
		if (App.DEBUG) {
			Log.d(TAG, "onRefresh() isVisible=" + isVisible());
		}
		doFetch(!mPullToRefreshView.hasPullFromTop());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		return true;
	}

	protected void doRefresh() {
		if (App.DEBUG) {
			Log.d(TAG, "doRefresh()");
		}
		doFetch(false);
	}

	protected void doGetMore() {
		if (App.DEBUG) {
			Log.d(TAG, "doGetMore()");
		}
		doFetch(true);
	}

	public Cursor getCursor() {
		return mCursor;
	}

	@Override
	public CursorAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public ListView getListView() {
		return mListView;
	}

	public void setSelection(int position) {
		mListView.setSelection(position);
	}

	public void setEmptyView(View emptyView) {
		mListView.setEmptyView(emptyView);
	}

	public void setEmptyText(CharSequence text) {
		final TextView tv = new TextView(getActivity());
		tv.setText(text);
		mListView.setEmptyView(tv);
	}

	public void goTop() {
		mListView.setSelection(0);
	}

	@Override
	public void updateUI() {
		if (mCursor != null) {
			mCursor.requery();
		}
	}

	@Override
	public void startRefresh() {
		if (App.DEBUG) {
			Log.d(TAG, "startRefresh() isVisible=" + isVisible());
		}
		if (!busy) {
			busy = true;
			doRefresh();
			mPullToRefreshView.setRefreshing();
		}
	}

	private void onSuccess(Bundle data) {
		if (App.DEBUG) {
			Log.d(TAG, "onSuccess(data)");
		}
		int count = data.getInt("count");
		onSuccess(count);
	}

	private void onSuccess(final int count) {
		if (App.DEBUG) {
			Log.d(TAG, "onSuccess(count)");
		}
		if (count > 0 && mCursor != null) {
			mCursor.requery();
			showToast(count);
		}
	}

	private void onError(Bundle data) {
		if (App.DEBUG) {
			Log.d(TAG, "onSuccess()");
		}
		String errorMessage = data.getString("error_message");
		int errorCode = data.getInt("error_code");
		Utils.notify(getActivity(), errorMessage);
		Utils.checkAuthorization(getActivity(), errorCode);
	}

	private void onRefreshComplete() {
		if (mPullToRefreshView != null) {
			mPullToRefreshView.onRefreshComplete();
		}
	}

	protected static void showPopup(Activity context, final View view,
			final Cursor c) {
		if (c != null) {
			final StatusModel s = StatusModel.from(c);
			if (s != null) {
				PopupController.showPopup(view, s, c);
			}
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (App.DEBUG) {
			Log.d(TAG, "onViewCreated() isVisible=" + isVisible());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (App.DEBUG) {
			Log.d(TAG, "onStart() isVisible=" + isVisible());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mListView != null) {
			mParcelable = mListView.onSaveInstanceState();
			outState.putParcelable("state", mParcelable);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mParcelable != null && mListView != null) {
			mListView.onRestoreInstanceState(mParcelable);
			mParcelable = null;
		}
		if (App.DEBUG) {
			Log.d(TAG, "onResume() isVisible=" + isVisible());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (App.DEBUG) {
			Log.d(TAG, "onPause() isVisible=" + isVisible());
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (App.DEBUG) {
			Log.d(TAG, "onStop() isVisible=" + isVisible());
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (App.DEBUG) {
			Log.d(TAG, "onDestroyView()");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (App.DEBUG) {
			Log.d(TAG, "onDestroy()");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (App.DEBUG) {
			Log.d(TAG, "onDetach() isVisible=" + isVisible());
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		mCursor = newCursor;
		getAdapter().swapCursor(mCursor);
		if (App.DEBUG) {
			Log.d(TAG, "onLoadFinished() adapter=" + mAdapter.getCount()
					+ " class=" + this.getClass().getSimpleName());
		}
//		if (mAdapter.isEmpty()) {
//			startRefresh();
//		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (App.DEBUG) {
			Log.d(TAG, "onLoaderReset()");
		}
		getAdapter().swapCursor(null);
	}

	/**
	 * FetchService返回数据处理 根据resultData里面的type信息分别处理
	 */
	protected static class ResultHandler extends Handler {
		private PullToRefreshListFragment mFragment;

		public ResultHandler(PullToRefreshListFragment fragment) {
			this.mFragment = fragment;
		}

		@Override
		public void handleMessage(Message msg) {
			Bundle data = msg.getData();
			switch (msg.what) {
			case Constants.RESULT_SUCCESS:
				mFragment.onSuccess(data);
				break;
			case Constants.RESULT_ERROR:
				mFragment.onError(data);
				break;
			default:
				break;
			}
			mFragment.onRefreshComplete();
		}

	}

}