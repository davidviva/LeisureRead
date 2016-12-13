package com.hotbitmapgg.eyepetizer.view.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hotbitmapgg.eyepetizer.base.BaseActivity;
import com.hotbitmapgg.eyepetizer.model.entity.Editors;
import com.hotbitmapgg.eyepetizer.model.entity.Stories;
import com.hotbitmapgg.eyepetizer.model.entity.ThemesDetails;
import com.hotbitmapgg.eyepetizer.network.RetrofitHelper;
import com.hotbitmapgg.eyepetizer.utils.LogUtil;
import com.hotbitmapgg.eyepetizer.view.adapters.ThemesDetailsHeadAdapter;
import com.hotbitmapgg.eyepetizer.view.adapters.ThemesDetailsStoriesAdapter;
import com.hotbitmapgg.eyepetizer.widget.CircleProgressView;
import com.hotbitmapgg.eyepetizer.widget.recycler.helper.HeaderViewRecyclerAdapter;
import com.hotbitmapgg.rxzhihu.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hcc on 16/4/4.
 */
public class ThemesDailyDetailsActivity extends BaseActivity
{

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.circle_progress)
    CircleProgressView mCircleProgressView;

    @Bind(R.id.recycle)
    RecyclerView mRecyclerView;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    //主题日报故事列表
    private List<Stories> stories = new ArrayList<>();

    //主题日报主编列表
    private List<Editors> editors = new ArrayList<>();

    private static final String EXTRA_TYPE = "extra_type";

    private int id;

    private HeaderViewRecyclerAdapter mHeaderViewRecyclerAdapter;


    @Override
    public int getLayoutId()
    {

        return R.layout.activity_type_daily;
    }

    @Override
    public void initViews(Bundle savedInstanceState)
    {
        Intent intent = getIntent();
        if (intent != null)
        {
            id = intent.getIntExtra(EXTRA_TYPE, -1);
        }


        startGetThemesDetails();
    }

    private void startGetThemesDetails()
    {

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mSwipeRefreshLayout.setRefreshing(false));
        mCircleProgressView.setVisibility(View.VISIBLE);
        mCircleProgressView.spin();
        mRecyclerView.setVisibility(View.GONE);
        getThemesDetails();
    }

    private void getThemesDetails()
    {

        RetrofitHelper.builder().getThemesDetailsById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(themesDetails -> {

                    if (themesDetails != null)
                    {
                        finishGetThemesDetails(themesDetails);
                    }
                }, throwable -> {

                    LogUtil.all("加载数据失败");
                });
    }

    private void finishGetThemesDetails(ThemesDetails themesDetails)
    {

        stories.addAll(themesDetails.getStories());
        editors.addAll(themesDetails.getEditors());

        mToolbar.setTitle(themesDetails.getName());

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(ThemesDailyDetailsActivity.this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        ThemesDetailsStoriesAdapter mAdapter = new ThemesDetailsStoriesAdapter(mRecyclerView, stories);
        mHeaderViewRecyclerAdapter = new HeaderViewRecyclerAdapter(mAdapter);
        addHeadView(themesDetails);
        mRecyclerView.setAdapter(mHeaderViewRecyclerAdapter);
        mAdapter.setOnItemClickListener((position, holder) -> {

            Stories stories1 = ThemesDailyDetailsActivity.this.stories.get(position);
            DailyDetailActivity.lanuch(ThemesDailyDetailsActivity.this, stories1.getId());
        });

        new Handler().postDelayed(() -> {

            mCircleProgressView.setVisibility(View.GONE);
            mCircleProgressView.stopSpinning();
            mRecyclerView.setVisibility(View.VISIBLE);
        }, 3000);
    }

    private void addHeadView(ThemesDetails themesDetails)
    {

        View headView = LayoutInflater.from(ThemesDailyDetailsActivity.this).inflate(R.layout.layout_themes_details_head, mRecyclerView, false);
        ImageView mThemesBg = (ImageView) headView.findViewById(R.id.type_image);
        TextView mThemesTitle = (TextView) headView.findViewById(R.id.type_title);
        Glide.with(ThemesDailyDetailsActivity.this).load(themesDetails.getBackground()).placeholder(R.drawable.account_avatar).into(mThemesBg);
        mThemesTitle.setText(themesDetails.getDescription());
        View editorsHeadView = LayoutInflater.from(ThemesDailyDetailsActivity.this).inflate(R.layout.layout_themes_details_head_2, mRecyclerView, false);
        RecyclerView mHeadRecycle = (RecyclerView) editorsHeadView.findViewById(R.id.head_recycle);
        mHeadRecycle.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(ThemesDailyDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mHeadRecycle.setLayoutManager(mLinearLayoutManager);
        ThemesDetailsHeadAdapter mHeadAdapter = new ThemesDetailsHeadAdapter(mHeadRecycle, editors);
        mHeadRecycle.setAdapter(mHeadAdapter);
        mHeadAdapter.setOnItemClickListener((position, holder) -> {

            Editors editor = ThemesDailyDetailsActivity.this.editors.get(position);
            int id1 = editor.getId();
            String name = editor.getName();
            EditorInfoActivity.luancher(ThemesDailyDetailsActivity.this, id1, name);
        });
        mHeaderViewRecyclerAdapter.addHeaderView(headView);
        mHeaderViewRecyclerAdapter.addHeaderView(editorsHeadView);
        mHeaderViewRecyclerAdapter.notifyDataSetChanged();
        mHeadAdapter.notifyDataSetChanged();
    }

    @Override
    public void initToolBar()
    {
        mToolbar.setNavigationIcon(R.drawable.ic_action_back);
        mToolbar.setNavigationOnClickListener(view -> onBackPressed());
        mToolbar.setTitleTextColor(getResources().getColor(R.color.black_90));
    }



    public static void Luanch(Activity activity, int id)
    {

        Intent mIntent = new Intent(activity, ThemesDailyDetailsActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.putExtra(EXTRA_TYPE, id);
        activity.startActivity(mIntent);
    }
}