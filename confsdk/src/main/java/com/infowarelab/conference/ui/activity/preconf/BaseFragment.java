package com.infowarelab.conference.ui.activity.preconf;


import android.content.Context;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class BaseFragment extends Fragment {
    private BaseFragmentActivity baseFragmentActivity;


    public void setBaseFragmentActivity(BaseFragmentActivity activity) {
        this.baseFragmentActivity = activity;
    }

    protected void hideInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getActivity().getCurrentFocus().getWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getActivity()
                            .getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected void hideInput(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != getActivity().getCurrentFocus() && null != getActivity().getCurrentFocus().getApplicationWindowToken()) {
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                            .getApplicationWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showInput(View v) {
        ((InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE)).showSoftInput(v, 0);
    }

    public void showLongToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    protected void showLoading() {
        if (baseFragmentActivity != null) {
            baseFragmentActivity.showLoading();
        }
    }

    protected void hideLoading() {
        if (baseFragmentActivity != null) {
            baseFragmentActivity.hideLoading();
        }
    }

    protected void logout() {
        if (baseFragmentActivity != null) {
            baseFragmentActivity.logout();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        if (baseFragmentActivity != null) {
            baseFragmentActivity.hideLoading();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        if (baseFragmentActivity != null) {
            baseFragmentActivity.hideLoading();
        }
        super.onDestroyView();
    }


//	protected void showToast(String content) {
//		LayoutInflater inflater = getActivity().getLayoutInflater();
//		View layout = inflater.inflate(R.layout.toast_singleline,
//				(ViewGroup) getActivity().findViewById(R.id.toast_ll));
//		TextView title = (TextView) layout.findViewById(R.id.toast_item_tv);
//		title.setText(content);
//		Toast toast = new Toast(getActivity().getApplicationContext());
////		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
//		toast.setDuration(Toast.LENGTH_SHORT);
//		toast.setView(layout);
//		toast.show();
//	}
//	protected void showLongToast(String content) {
//		LayoutInflater inflater = getActivity().getLayoutInflater();
//		View layout = inflater.inflate(R.layout.toast_singleline,
//				(ViewGroup) getActivity().findViewById(R.id.toast_ll));
//		TextView title = (TextView) layout.findViewById(R.id.toast_item_tv);
//		title.setText(content);
//		Toast toast = new Toast(getActivity().getApplicationContext());
////		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
//		toast.setDuration(Toast.LENGTH_LONG);
//		toast.setView(layout);
//		toast.show();
//	}
}
