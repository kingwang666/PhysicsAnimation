package android.support.animation;

import android.support.annotation.FloatRange;

/**
 * Author: wangxiaojie6
 * Date: 2017/12/20
 */

public class FlingBoundAnimation extends DynamicAnimation<FlingBoundAnimation> {

    private final DragForce mFlingForce = new DragForce();


    public FlingBoundAnimation(FloatValueHolder floatValueHolder) {
        super(floatValueHolder);
        mFlingForce.setValueThreshold(getValueThreshold());
    }

    public <K> FlingBoundAnimation(K object, FloatPropertyCompat<K> property) {
        super(object, property);
        mFlingForce.setValueThreshold(getValueThreshold());
    }

    public FlingBoundAnimation setFriction(@FloatRange(from = 0.0) float friction) {
        if (friction < 0) {
            throw new IllegalArgumentException("Friction must be positive");
        }
        mFlingForce.setFrictionScalar(friction);
        return this;
    }

    public float getFriction() {
        return mFlingForce.getFrictionScalar();
    }

    @Override
    boolean updateValueAndVelocity(long deltaT) {
        MassState state = mFlingForce.updateValueAndVelocity(mValue, mVelocity, deltaT);
        mValue = state.mValue;
        mVelocity = state.mVelocity;

        if (mValue < mMinValue) {
            mValue = 2 * mMinValue - mValue;
            mVelocity = -mVelocity;
        } else if (mValue > mMaxValue) {
            mValue = 2 * mMaxValue - mValue;
            mVelocity = -mVelocity;
        }

        return mFlingForce.mFriction != 0 && isAtEquilibrium(mValue, mVelocity);
    }


    @Override
    float getAcceleration(float value, float velocity) {
        return mFlingForce.getAcceleration(value, velocity);
    }

    @Override
    boolean isAtEquilibrium(float value, float velocity) {
        return value >= mMaxValue
                || value <= mMinValue
                || mFlingForce.isAtEquilibrium(value, velocity);
    }

    @Override
    void setValueThreshold(float threshold) {
        mFlingForce.setValueThreshold(threshold);
    }

    private static final class DragForce implements Force {

        private static final float DEFAULT_FRICTION = -4.2f;

        // This multiplier is used to calculate the velocity threshold given a certain value
        // threshold. The idea is that if it takes >= 1 frame to move the value threshold amount,
        // then the velocity is a reasonable threshold.
        private static final float VELOCITY_THRESHOLD_MULTIPLIER = 1000f / 16f;
        private float mFriction = DEFAULT_FRICTION;
        private float mVelocityThreshold;

        // Internal state to hold a value/velocity pair.
        private final DynamicAnimation.MassState mMassState = new DynamicAnimation.MassState();

        void setFrictionScalar(float frictionScalar) {
            mFriction = frictionScalar * DEFAULT_FRICTION;
        }

        float getFrictionScalar() {
            return mFriction / DEFAULT_FRICTION;
        }

        MassState updateValueAndVelocity(float value, float velocity, long deltaT) {
            if (mFriction != 0) {
                mMassState.mVelocity = (float) (velocity * Math.exp((deltaT / 1000f) * mFriction));
                mMassState.mValue = (float) (value - velocity / mFriction
                        + velocity / mFriction * Math.exp(mFriction * deltaT / 1000f));
                if (isAtEquilibrium(mMassState.mValue, mMassState.mVelocity)) {
                    mMassState.mVelocity = 0f;
                }
            } else {
                mMassState.mVelocity = velocity;
                mMassState.mValue = value + velocity * (deltaT / 1000f);
            }
            return mMassState;
        }

        @Override
        public float getAcceleration(float position, float velocity) {
            return velocity * mFriction;
        }

        @Override
        public boolean isAtEquilibrium(float value, float velocity) {
            return Math.abs(velocity) < mVelocityThreshold;
        }

        void setValueThreshold(float threshold) {
            mVelocityThreshold = threshold * VELOCITY_THRESHOLD_MULTIPLIER;
        }
    }
}
