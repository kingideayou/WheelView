package me.next.wheelview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.next.wheelview.utils.ScreenUtils;
import me.next.wheelview.utils.ViewUtils;

/**
 * Created by NeXT on 15/9/21.
 */
public class WheelView extends ScrollView {

    private Context context;
    private Runnable scrollTask;
    private LinearLayout itemViews;
    private OnViewWheelListener onViewWheelListener;

    private int initialY;
    private int itemHeight;
    private int selectedIndex = 1;

    private int displayItemCount = 0;

    public static final int OFF_SET_DEFAULT = 1;
    private static final int CHECK_DELAY = 50;
    int offset = OFF_SET_DEFAULT; // 偏移量（需要在最前面和最后面补全）

    private List<String> items;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        this.setVerticalScrollBarEnabled(false);

        itemViews = new LinearLayout(context);
        itemViews.setOrientation(LinearLayout.VERTICAL);
        this.addView(itemViews);

        scrollTask = new Runnable() {
            @Override
            public void run() {
                int newY = getScrollY();
                if (initialY - newY == 0) {
                    final int remainder = initialY % itemHeight;
                    final int divided = initialY / itemHeight;
                    if (remainder == 0) {
                        selectedIndex = divided + offset;
                        onSelectedCallBack();
                    } else {
                        if (remainder > itemHeight / 2) {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    WheelView.this.smoothScrollTo(0, initialY - remainder + itemHeight);
                                    selectedIndex = divided + offset + 1;
                                    onSelectedCallBack();
                                }
                            });
                        } else {
                            WheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    WheelView.this.smoothScrollTo(0, initialY - remainder);
                                    selectedIndex = divided + offset;
                                    onSelectedCallBack();
                                }
                            });
                        }
                    }
                } else {
                    initialY = getScrollY();
                    WheelView.this.postDelayed(scrollTask, CHECK_DELAY);
                }
            }
        };
    }

    private TextView createItemView(String itemStr) {
        TextView item = new TextView(context);
        item.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        item.setSingleLine(true);
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        item.setText(itemStr);
        item.setGravity(Gravity.CENTER);
        //TODO 动态设置 Padding
        int padding = (int) ScreenUtils.dpToPx(context, 15);
        item.setPadding(padding, padding, padding, padding);
        if (0 == itemHeight) {
            itemHeight = ViewUtils.getViewMeasuredHeight(item);
            itemViews.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * displayItemCount));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getLayoutParams();
            this.setLayoutParams(new LinearLayout.LayoutParams(lp.width, itemHeight * displayItemCount));
        }
        return item;
    }

    private void refreshItemView(int y) {
        

    }

    private void initData() {
        displayItemCount = offset * 2 + 1;
        for (String item : items) {
            itemViews.addView(createItemView(item));
        }
        refreshItemView(0);
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        if (items == null) {
            items = new ArrayList<>();
        }
        this.items = items;
        for (int i = 0; i < offset; i++) {
            items.add(0, "");
            items.add("");
        }
        initData();
    }

    private void onSelectedCallBack() {
        if (onViewWheelListener != null) {
            onViewWheelListener.onItemSelected(selectedIndex, items.get(selectedIndex));
        }
    }

    private interface OnViewWheelListener {
        void onItemSelected(int selectedIndex, String item);
    }

    public void setOnViewWheelListener(OnViewWheelListener onViewWheelListener) {
        this.onViewWheelListener = onViewWheelListener;
    }
}
