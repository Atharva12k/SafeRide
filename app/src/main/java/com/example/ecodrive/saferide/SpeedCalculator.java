package com.example.ecodrive.saferide;

import android.location.Location;
import java.math.BigDecimal;

public class SpeedCalculator {
    private float mAcceleration;
    private boolean hasAcceleration=false;
    private long mTime;
    private float mVelocity;
    private boolean hasVelocity = false;

    synchronized public float updateAcceleration(Location location) {
        if (location.hasSpeed()) {
            if (!hasVelocity) {
                mVelocity = location.getSpeed();
                mTime = System.currentTimeMillis();
                hasVelocity = true;
            } else {
                float velocity = location.getSpeed();
                long currentTime = System.currentTimeMillis();
                float acceleration = (velocity - mVelocity) / (currentTime - mTime) * 1000;
                BigDecimal accelerationRounded = new BigDecimal(acceleration).setScale(2, BigDecimal.ROUND_HALF_UP);

                mAcceleration = accelerationRounded.floatValue();
                mVelocity = velocity;
                mTime = currentTime;
                hasAcceleration = true;
            }

        }
        return mAcceleration;
    }
}
