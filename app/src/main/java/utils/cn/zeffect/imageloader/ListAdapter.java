package utils.cn.zeffect.imageloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import utils.cn.zeffect.loaderlibrary.ImageLoader;

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/04/12
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 *         // TODO 用@see描述一下当前类的方法及简单解释
 */

public class ListAdapter extends BaseAdapter {
    private List<String> mStrings;
    private Context mContext;

    public ListAdapter(Context pContext, List<String> pList) {
        this.mContext = pContext;
        this.mStrings = pList;
    }

    @Override
    public int getCount() {
        return mStrings.size();
    }

    @Override
    public Object getItem(int position) {
        return mStrings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder tempHolder;
        if (convertView == null) {
            tempHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list, null);
            tempHolder.mImageView = (ImageView) convertView.findViewById(R.id.il_img);
            tempHolder.mTextView = (TextView) convertView.findViewById(R.id.il_tv);
            convertView.setTag(tempHolder);
        } else {
            tempHolder = (ViewHolder) convertView.getTag();
        }
        tempHolder.mTextView.setText(mStrings.get(position) + "这个是用来看图标是不是对应的");
        ImageLoader.getInstance().load(mStrings.get(position), tempHolder.mImageView);
        return convertView;
    }

    private static class ViewHolder {
        private TextView mTextView;
        private ImageView mImageView;
    }
}
