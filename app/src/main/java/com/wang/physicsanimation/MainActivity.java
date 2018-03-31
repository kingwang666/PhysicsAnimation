package com.wang.physicsanimation;

import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.FlingBoundAnimation;
import android.support.animation.SpringAnimation;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    @BindView(R.id.bound_view)
    TextView mBoundView;
    @BindView(R.id.fling_view)
    TextView mFlingView;
    @BindView(R.id.lead_view)
    TextView mLeadView;
    @BindView(R.id.child_1_view)
    ImageView mChild1View;
    @BindView(R.id.child_2_view)
    ImageView mChild2View;
    @BindView(R.id.container)
    FrameLayout mContainer;

    private SpringAnimation mSpring1X;
    private SpringAnimation mSpring1Y;
    private SpringAnimation mSpring2X;
    private SpringAnimation mSpring2Y;

    private FlingBoundAnimation mBoundLeadX;
    private FlingBoundAnimation mBoundLeadY;
    private FlingBoundAnimation mBoundX;
    private FlingBoundAnimation mBoundY;

    private FlingAnimation mFlingX;
    private FlingAnimation mFlingY;

    private float[] mMinX = new float[3];
    private float[] mMaxX = new float[3];
    private float[] mMinY = new float[3];
    private float[] mMaxY = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContainer.setOnTouchListener(this);
        initAnimation();
    }

    private void initAnimation() {
        mSpring1X = new SpringAnimation(mChild1View, DynamicAnimation.TRANSLATION_X, 0);
        mSpring1Y = new SpringAnimation(mChild1View, DynamicAnimation.TRANSLATION_Y, 0);
        mSpring2X = new SpringAnimation(mChild2View, DynamicAnimation.TRANSLATION_X, 0);
        mSpring2Y = new SpringAnimation(mChild2View, DynamicAnimation.TRANSLATION_Y, 0);
        mSpring1X.getSpring().setDampingRatio(1.0f).setStiffness(50.0f);
        mSpring1Y.getSpring().setDampingRatio(1.0f).setStiffness(50.0f);
        mSpring2X.getSpring().setDampingRatio(1.0f).setStiffness(50.0f);
        mSpring2Y.getSpring().setDampingRatio(1.0f).setStiffness(50.0f);

        mBoundLeadX = new FlingBoundAnimation(mLeadView, DynamicAnimation.TRANSLATION_X);
        mBoundLeadY = new FlingBoundAnimation(mLeadView, DynamicAnimation.TRANSLATION_Y);
        mBoundLeadX.setFriction(0.5f);
        mBoundLeadY.setFriction(0.5f);

        mBoundLeadX.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                mSpring1X.animateToFinalPosition(value);
            }
        });
        mBoundLeadY.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                mSpring1Y.animateToFinalPosition(value);
            }
        });
        mSpring1X.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation dynamicAnimation, float value, float velocity) {
                mSpring2X.animateToFinalPosition(value);
            }
        });
        mSpring1Y.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation dynamicAnimation, float value, float velocity) {
                mSpring2Y.animateToFinalPosition(value);
            }
        });

        mBoundX = new FlingBoundAnimation(mBoundView, DynamicAnimation.TRANSLATION_X);
        mBoundY = new FlingBoundAnimation(mBoundView, DynamicAnimation.TRANSLATION_Y);
        mBoundX.setFriction(0.5f);
        mBoundY.setFriction(0.5f);

        mFlingX = new FlingAnimation(mFlingView, DynamicAnimation.TRANSLATION_X);
        mFlingY = new FlingAnimation(mFlingView, DynamicAnimation.TRANSLATION_Y);
        mFlingX.setFriction(0.5f);
        mFlingY.setFriction(0.5f);


        mLeadView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mLeadView.getViewTreeObserver().removeOnPreDrawListener(this);
                initMaxAndMin(mLeadView, 0, mBoundLeadX, mBoundLeadY);
                initMaxAndMin(mBoundView, 1, mBoundX, mBoundY);
                initMaxAndMin(mFlingView, 2, mFlingX, mFlingY);
                return true;
            }
        });

    }
    
    private void initMaxAndMin(View view , int position, DynamicAnimation animationX, DynamicAnimation animationY){
        float minX = mContainer.getLeft()- view.getLeft();
        animationX.setMinValue(minX);
        mMinX[position] = minX;
        float maxX = mContainer.getRight() - view.getRight();
        animationX.setMaxValue(maxX);
        mMaxX[position] = maxX;
        float minY = mContainer.getTop() - view.getTop();
        animationY.setMinValue(minY);
        mMinY[position] = minY;
        float maxY = mContainer.getBottom() - view.getBottom();
        animationY.setMaxValue(maxY);
        mMaxY[position] = maxY;
    }

    private float mFirstDownX = 0;
    private float mFirstDownY = 0;
    private View mChooseView;
    private DynamicAnimation mAnimationX;
    private DynamicAnimation mAnimationY;
    private int mPosition; 

    private VelocityTracker mVelocityTracker;
    
    private boolean down(MotionEvent event){
        if (event.getX() >= mLeadView.getX()
                && event.getX() <= mLeadView.getX() + mLeadView.getWidth()
                && event.getY() >= mLeadView.getY()
                && event.getY() <= mLeadView.getY() + mLeadView.getHeight()) {
            mChooseView = mLeadView;
            mAnimationX = mBoundLeadX;
            mAnimationY = mBoundLeadY;
            mPosition = 0;
            return true;
        }
        if (event.getX() >= mBoundView.getX()
                && event.getX() <= mBoundView.getX() + mBoundView.getWidth()
                && event.getY() >= mBoundView.getY()
                && event.getY() <= mBoundView.getY() + mBoundView.getHeight()) {
            mChooseView = mBoundView;
            mAnimationX = mBoundX;
            mAnimationY = mBoundY;
            mPosition = 1;
            return true;
        }
        if (event.getX() >= mFlingView.getX()
                && event.getX() <= mFlingView.getX() + mFlingView.getWidth()
                && event.getY() >= mFlingView.getY()
                && event.getY() <= mFlingView.getY() + mFlingView.getHeight()) {
            mChooseView = mFlingView;
            mAnimationX = mFlingX;
            mAnimationY = mFlingY;
            mPosition = 2;
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (down(event)) {
                if (mAnimationX.isRunning()) {
                    mAnimationX.cancel();
                }
                if (mAnimationY.isRunning()) {
                    mAnimationY.cancel();
                }
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);


                mFirstDownX = event.getX() - mChooseView.getTranslationX();
                mFirstDownY = event.getY() - mChooseView.getTranslationY();
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float deltaX = event.getX() - mFirstDownX;
            float deltaY = event.getY() - mFirstDownY;

            if (deltaX > mMaxX[mPosition] || deltaX < mMinX[mPosition] || deltaY > mMaxY[mPosition] || deltaY < mMinY[mPosition]){
                return true;
            }

            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);

                mChooseView.setTranslationX(deltaX);
                mChooseView.setTranslationY(deltaY);

                if (mChooseView == mLeadView) {
                    mSpring1X.animateToFinalPosition(deltaX);
                    mSpring1Y.animateToFinalPosition(deltaY);
                }
            }



        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (mVelocityTracker != null) {
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                mAnimationX.setStartVelocity(mVelocityTracker.getXVelocity(event.getActionIndex()));
                mAnimationY.setStartVelocity(mVelocityTracker.getYVelocity(event.getActionIndex()));
                mAnimationX.start();
                mAnimationY.start();
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }
        else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL){
            if (mVelocityTracker != null){
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }
        return true;
    }
}
