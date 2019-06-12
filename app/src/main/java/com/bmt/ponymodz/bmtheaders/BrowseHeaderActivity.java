package com.bmt.ponymodz.bmtheaders;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bmt.ponymodz.bmtheaders.HeaderUtils.DaylightHeaderInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowseHeaderActivity extends Activity {
    private static final String TAG = "BrowseHeaderActivity";
    private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";
    private static final String STATUS_BAR_CUSTOM_HEADER_IMAGE = "status_bar_custom_header_image";
    private static final String STATUS_BAR_CUSTOM_HEADER_PROVIDER = "status_bar_custom_header_provider";

    private static final boolean DEBUG = true;
    private List<DaylightHeaderInfo> mHeadersList;
    private Resources mRes;
    private String mPackageName;
    private String mHeaderName;
    private ListView mHeaderListView;
    private Spinner mHeaderSelect;
    private Map<String, String> mHeaderMap;
    private List<String> mLabelList;
    private HeaderListAdapter mHeaderListAdapter;
    private ProgressBar mProgress;
    private TextView mCreatorName;
    private String mCreatorLabel;
    private String mCreator;
    protected boolean mPickerMode;

    public class HeaderListAdapter extends ArrayAdapter<DaylightHeaderInfo> {
        private final LayoutInflater mInflater;

        public HeaderListAdapter(Context context) {
            super(context, R.layout.header_image, mHeadersList);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderImageHolder holder = HeaderImageHolder.createOrRecycle(mInflater, convertView);
            convertView = holder.rootView;
            DaylightHeaderInfo di = mHeadersList.get(position);
            int resId = mRes.getIdentifier(di.mImage, "drawable", mPackageName);
            if (resId != 0) {
                holder.mHeaderImage.setImageDrawable(mRes.getDrawable(resId, null));
            } else {
                holder.mHeaderImage.setImageDrawable(null);
            }
            holder.mHeaderName.setText(di.mName != null ? di.mName : di.mImage);
            return convertView;
        }
    }

    public static class HeaderImageHolder {
        public View rootView;
        public ImageView mHeaderImage;
        public TextView mHeaderName;

        public static HeaderImageHolder createOrRecycle(LayoutInflater inflater, View convertView) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.header_image, null);
                HeaderImageHolder holder = new HeaderImageHolder();
                holder.rootView = convertView;
                holder.mHeaderImage = (ImageView) convertView.findViewById(R.id.header_image);
                holder.mHeaderName = (TextView) convertView.findViewById(R.id.header_name);
                convertView.setTag(holder);
                return holder;
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                return (HeaderImageHolder) convertView.getTag();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_browse);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);

        mProgress = (ProgressBar) findViewById(R.id.browse_progress);
        mHeaderSelect = (Spinner) findViewById(R.id.package_select);
        mCreatorName = (TextView) findViewById(R.id.meta_creator);
        mCreatorLabel = getResources().getString(R.string.header_creator_label);
        mHeaderMap = new HashMap<String, String>();
        mLabelList = new ArrayList<String>();
        getAvailableHeaderPacks(mHeaderMap);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,
                R.layout.spinner_item, mLabelList);
        mHeaderSelect.setAdapter(adapter);

        mHeaderSelect.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String label = mLabelList.get(position);
                loadHeaderPackage(mHeaderMap.get(label));
                mCreatorName.setVisibility(TextUtils.isEmpty(mCreator) ? View.GONE : View.VISIBLE);
                mCreatorName.setText(mCreatorLabel + mCreator);
                mHeaderListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mHeadersList = new ArrayList<DaylightHeaderInfo>();
        mHeaderListView = (ListView) findViewById(R.id.package_images);
        if (mPickerMode) {
            mHeaderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    DaylightHeaderInfo di = mHeadersList.get(i);
                    Settings.System.putString(getContentResolver(), STATUS_BAR_CUSTOM_HEADER_IMAGE, mPackageName + "/" + di.mImage);
                    Settings.System.putString(getContentResolver(), STATUS_BAR_CUSTOM_HEADER_PROVIDER, "static");
                    Toast.makeText(BrowseHeaderActivity.this, R.string.custom_header_image_notice, Toast.LENGTH_LONG).show();
                }
            });
        }
        mHeaderListAdapter = new HeaderListAdapter(this);
        mHeaderListView.setAdapter(mHeaderListAdapter);
        loadHeaderPackage(mHeaderMap.get(mLabelList.get(0)));
        mHeaderListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void getAvailableHeaderPacks(Map<String, String> headerMap) {
        String defaultLabel = null;
        Intent i = new Intent();
        PackageManager packageManager = getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                defaultLabel = label;
            } else {
                headerMap.put(label, packageName);
            }
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getPackageManager()).toString();
            if (label == null) {
                label = packageName;
            }
            headerMap.put(label, packageName + "/" + r.activityInfo.name);
        }
        mLabelList.addAll(headerMap.keySet());
        Collections.sort(mLabelList);
        if (defaultLabel != null) {
            mLabelList.add(0, defaultLabel);
            headerMap.put(defaultLabel, DEFAULT_HEADER_PACKAGE);
        }
    }

    private void loadHeaderPackage(String label) {
        if (DEBUG) Log.i(TAG, "Load header pack " + label);
        mProgress.setVisibility(View.VISIBLE);
        int idx = label.indexOf("/");
        if (idx != -1) {
            String[] parts = label.split("/");
            mPackageName = parts[0];
            mHeaderName = parts[1];
        } else {
            mPackageName = label;
            mHeaderName = null;
        }
        try {
            PackageManager packageManager = getPackageManager();
            mRes = packageManager.getResourcesForApplication(mPackageName);
            mCreator = HeaderUtils.loadHeaders(mRes, mHeaderName, mHeadersList);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load header pack " + mHeaderName, e);
            mRes = null;
        }
        mProgress.setVisibility(View.GONE);
    }
}