package com.cabe.app.novel.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cabe.app.novel.R;
import com.cabe.app.novel.domain.NovelContentUseCase;
import com.cabe.app.novel.model.NovelContent;
import com.cabe.app.novel.utils.DiskUtils;
import com.cabe.lib.cache.CacheSource;
import com.cabe.lib.cache.interactor.ViewPresenter;
import com.google.gson.reflect.TypeToken;

public class NovelContentActivity extends BaseActivity {
    private SwipeRefreshLayout swipeLayout;
    private View btnPre;
    private View btnNext;
    private TextView tvTitle;
    private TextView tvContent;

    private NovelContent curContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel_content);
        initView();

        NovelContent novelContent = getExtraGson(new TypeToken<NovelContent>(){});
        if(novelContent != null && !TextUtils.isEmpty(novelContent.url)) {
            loadContent(novelContent.url);
        }
    }

    private void initView() {
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.activity_novel_content_swipe);
        btnPre = findViewById(R.id.activity_novel_content_preview);
        btnNext = findViewById(R.id.activity_novel_content_next);
        tvTitle = (TextView) findViewById(R.id.activity_novel_content_title);
        tvContent = (TextView) findViewById(R.id.activity_novel_content_info);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(curContent != null && !TextUtils.isEmpty(curContent.url)) {
                    loadContent(curContent.url);
                }
            }
        });
    }

    private void updateView(NovelContent content) {
        if(content == null) return;

        btnPre.setVisibility(content.preUrl.endsWith("html") ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(content.nextUrl.endsWith("html") ? View.VISIBLE : View.INVISIBLE);
        tvTitle.setText(content.title);
        tvContent.setText(Html.fromHtml(content.content));
    }

    private void loadContent(String url) {
        swipeLayout.setRefreshing(true);
        NovelContentUseCase useCase = new NovelContentUseCase(url);
        useCase.execute(new ViewPresenter<NovelContent>() {
            @Override
            public void error(CacheSource from, int code, String info) {
                toast(info);
            }
            @Override
            public void load(CacheSource from, NovelContent content) {
                NovelContent cacheContent = new NovelContent();
                cacheContent.title = content.title;
                cacheContent.url = content.url;
                cacheContent.preUrl = content.preUrl;
                cacheContent.nextUrl = content.nextUrl;
                DiskUtils.saveData(NovelDetailActivity.CUR_NOVEL_DETAIL_KEY, cacheContent.toGson());
                curContent = content;
                updateView(content);
            }
            @Override
            public void complete(CacheSource from) {
                swipeLayout.setRefreshing(false);
            }
        });
    }

    public void actionPreview(View view) {
        if(curContent == null) return;

        loadContent(curContent.preUrl);
    }

    public void actionNext(View view) {
        if(curContent == null) return;

        loadContent(curContent.nextUrl);
    }

    public static Intent create(Context context, NovelContent novelContent) {
        Intent intent = new Intent(context, NovelContentActivity.class);
        if(novelContent != null) {
            intent.putExtra(KEY_EXTRA_GSON, novelContent.toGson());
        }
        return intent;
    }
}