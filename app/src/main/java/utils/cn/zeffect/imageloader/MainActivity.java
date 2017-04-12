package utils.cn.zeffect.imageloader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.container.DefaultHeader;
import com.liaoinstan.springview.widget.SpringView;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {
    private SpringView mSpringView;
    private ListView mListView;
    private ListAdapter mAdapter;
    private List<String> mApps = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSpringView = (SpringView) findViewById(R.id.refresh);
        mSpringView.setFooter(new DefaultFooter(this));
        mListView = (ListView) findViewById(R.id.lv);
        mAdapter = new ListAdapter(this, mApps);
        mApps.addAll(Arrays.asList(new File(getCacheDirs()).list()));
        mListView.setAdapter(mAdapter);
        mSpringView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadmore() {
                mApps.addAll(mApps);
                mAdapter.notifyDataSetChanged();
                mSpringView.onFinishFreshAndLoad();
            }
        });
    }

    /***
     * 返回缓存目录
     * @return context==null返回SD卡根目录，否则返回qimon/student/cache/包名/
     */
    public static String getCacheDirs() {
        File tempFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "qimon" + File.separator + "student" + File.separator + "cache" + File.separator + "org.qimon.launcher6" + File.separator);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        return tempFile.getAbsolutePath();
    }
}
