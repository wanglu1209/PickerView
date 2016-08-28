# PickerView

前段时间在公司总是有需求『滚动的选择器』，那时候总是用别人写好的来用，感觉心里不是很舒服，最近有时间了自己来写一写，上图：

![](/Users/WangLu/Desktop/gif5.gif)

首先分析一下需求：

1. 可以滑动
2. 滑到两条线里的数据变色
3. 滑动完成后必须要把选中的数据放到中间


### 滑动

首先说到滑动而且还是自定义的View，我们就会想到各种『scrollBy/scrollTo』等，但是这里有个更简单的方法就是**继承自ScrollView**，这样就简单了不少，并且还有滚动的监听『只不过少了一个停止滑动的状态』，然而对于这个需求，我们只需要创建一个ViewGroup来放入数据（也就是很多TextView），然后addView()就OK，代码如下：

    /**
     * 初始化数据,首先添加Group
     */
    mTextGroup = new LinearLayout(context);
    mTextGroup.setOrientation(LinearLayout.VERTICAL);
    mTextGroup.setGravity(Gravity.CENTER);
    addView(mTextGroup);


    /**
     * 由于我们需要给自身的数据在选中的框里显示
     * 所以这里需要添加前面和后面的空数据
     */
    mTextGroup.addView(createTextView(""));
    for (int i = 0; i < mData.size(); i++) {
        mTextGroup.addView(createTextView("" + mData.get(i)));
    }
    mTextGroup.addView(createTextView(""));
	
	------------------------------------------------------------
	
	/**
     * 动态创建TextView
     */
    private TextView createTextView(String s) {
        TextView tv = new TextView(context);
        tv.setText(s);
        tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tv.setTextSize(mTextSize);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mTextHeight);

        params.bottomMargin = mTextHeight / 2;
        params.topMargin = mTextHeight / 2;
        tv.setLayoutParams(params);
        return tv;
    }


可以看到我们在数据的前后都加了两条空数据，因为这里需要在滑动到底部或者顶部的时候确保第一条或者最后一条数据是在我们的两条线之内，这样运行一下程序就会发现我们的数据已经可以滑动了，只不过这个时候还没有中间的两条线。这里需要思考一下，如果我们添加两条线上去的话，滑动的时候线也会跟着滑动，所以这个时候就想到了背景，我们自己来画一个背景。代码如下：

	/**
     * 设置背景,这里选择画一个,两条线
     */
    setBackground(new Drawable() {
        @Override
        public void draw(Canvas canvas) {
            /**
             * 这里把两条线之间的距离设置为了两个TextView的高度
             */
            canvas.drawLine(
                    mWidth * 0.1f,
                    mHeight / 2 - mTextHeight,
                    mWidth * 0.9f,
                    mHeight / 2 - mTextHeight,
                    mLinePaint
            );
            canvas.drawLine(
                    mWidth * 0.1f,
                    mHeight / 2 + mTextHeight,
                    mWidth * 0.9f,
                    mHeight / 2 + mTextHeight,
                    mLinePaint
            );
        }

        @Override
        public void setAlpha(int i) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    });


这时候我们的背景和数据都弄好了


### 在线内的数据变颜色

先考虑一下，因为我们使用的是TextView，所以我们直接就可以设置TextView的字体颜色。很简单，我们在onScrollChanged()方法中用小学除法来算一下当前滚动到了哪个TextView『也就是position』，我们就把哪个改变颜色，代码如下：

    /**
     * ScrollView的滑动事件监听
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        /**
         * 计算出当前在两条线里的position
         */
        position = (t + mTextHeight) / (mTextHeight * 2);
        /**
         * 因为此方法会在滑动的时候不停的调用,所以这里设置一个临时的变量来控制
         */
        if (tempPosition != position) {
            int size = mTextGroup.getChildCount();
            for (int i = 0; i < size; i++) {
                TextView tv = (TextView) mTextGroup.getChildAt(i);
                /**
                 * 因为我们在数据开头添加了一个空的数据,所以这里position要+1
                 */
                if (position + 1 == i) {
                    tv.setTextColor(getResources().
                            getColor(android.R.color.holo_blue_dark));
                } else {
                    tv.setTextColor(getResources().
                            getColor(android.R.color.darker_gray));
                }
            }
        }
        tempPosition = position;
    }

### 滑动结束后把当前数据放在中间（其实我不知道怎么表达了）


ScrollView没有滑动结束的监听，所以我们只好取巧来弄一个。我们在手指离开屏幕的30ms后来判断当前的scrollY和离开屏幕时候的scrollY是否一样，一样则认定是滑动结束了，如果不一样，我们强行给弄成一样的来结束滑动

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /**
         * 因为ScrollView没有停止滑动的监听,所以这里取巧
         * 在手指离开屏幕的30ms后判断是否和原来的scrollY一样
         * 如果一样则进入,如果不一样则设置为一样
         */
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            scrollY = getScrollY();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (scrollY == getScrollY()) {
                        /**
                         * 获得每次松手后scrollY相对于TextView高度的偏移量
                         */
                        int offset = scrollY % (mTextHeight * 2);

                        /**
                         * 如果偏移量大于TextView高度的一半
                         * 则进入到下一个
                         */
                        if (offset > mTextHeight) {
                            smoothScrollTo(0, scrollY - offset + (mTextHeight * 2));
                        } else {
                            smoothScrollTo(0, scrollY - offset);
                        }
                    } else {
                        scrollY = getScrollY();
                        post(this);
                    }
                }
            }, 30);
        }
        return super.onTouchEvent(ev);
    }


到这里就结束了，其实还是蛮简单的，只不过功能甚少，只能实现基本的选择数据，所以我起名为PickerView，当然以后还会对这个项目进行维护，欢迎大家来star或者提bug

---

#### 感谢

感谢wangjiegulu的WheelView给的启发，放上地址

[WheelView](https://github.com/wangjiegulu/WheelView)

---

## 最后

爱生活，爱小丽，爱Android









