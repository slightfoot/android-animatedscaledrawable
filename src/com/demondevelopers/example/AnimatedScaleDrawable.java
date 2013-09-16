/*
	Copyright 2013 © Demon Developers Ltd
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
		http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.demondevelopers.example;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;


/**
 * Animated Scale Drawable
 * 
 * https://github.com/slightfoot/android-animatedscaledrawable for details of use.
 * 
 * @version 1.0
 * @author Simon Lightfoot, Demon Developers Ltd <simon@demondevelopers.com>
 * 
 * 
 * Known Issues:
 * 
 *     Drawable mutation is not functioning correct. Will be fixed in 1.1
 * 
 */

public class AnimatedScaleDrawable extends Drawable implements Drawable.Callback, Animatable
{
	public static final String  TAG = AnimatedScaleDrawable.class.getSimpleName();
	
	private AnimationScaleState mState;
	private boolean             mMutated;
	
	private final Rect mTmpRect = new Rect();
	
	
	public AnimatedScaleDrawable()
	{
		this(null, null);
	}
	
	public AnimatedScaleDrawable(Drawable drawable)
	{
		this(null, null);
		setDrawable(drawable);
	}
	
	private AnimatedScaleDrawable(AnimationScaleState pulsingState, Resources res)
	{
		mState = new AnimationScaleState(pulsingState, this, res);
	}
	
	public void setDrawable(Drawable drawable)
	{
		if(mState.mDrawable != drawable){
			if(mState.mDrawable != null){
				mState.mDrawable.setCallback(null);
			}
			mState.mDrawable = drawable;
			if(drawable != null){
				drawable.setCallback(this);
			}
		}
	}
	
	public void setInterpolator(Context context, int resId)
	{
		setInterpolator(AnimationUtils.loadInterpolator(context, resId));
	}
	
	public void setInterpolator(Interpolator interpolator)
	{
		mState.mInterpolator = interpolator;
	}
	
	public void setDuration(int duration)
	{
		mState.mDuration = duration;
	}
	
	public void setFromScale(float fromScale)
	{
		mState.mMinScale = fromScale;
	}
	
	public void setToScale(float toScale)
	{
		mState.mMaxScale = toScale;
	}
	
	public void setUseBounds(boolean useBounds)
	{
		mState.mUseBounds = useBounds;
		onBoundsChange(getBounds());
	}
	
	public void setInvertTransformation(boolean invert)
	{
		mState.mInvert = invert;
	}
	
	public Interpolator getInterpolator()
	{
		return mState.mInterpolator;
	}
	
	public int getDuration()
	{
		return mState.mDuration;
	}
	
	public float getFromScale()
	{
		return mState.mMinScale;
	}
	
	public float getToScale()
	{
		return mState.mMaxScale;
	}
	
	public boolean isUsingBounds()
	{
		return mState.mUseBounds;
	}
	
	public boolean isInvertingTransformation()
	{
		return mState.mInvert;
	}
	
	@Override
	public void start()
	{
		if(mState.mAnimating){
			return;
		}
		
		if(mState.mInterpolator == null){
			mState.mInterpolator = new LinearInterpolator();
		}
		
		if(mState.mTransformation == null){
			mState.mTransformation = new Transformation();
		}
		else{
			mState.mTransformation.clear();
		}
		
		if(mState.mAnimation == null){
			mState.mAnimation = new AlphaAnimation(0.0f, 1.0f);
		}
		else{
			mState.mAnimation.reset();
		}
		
		mState.mAnimation.setRepeatMode(Animation.REVERSE);
		mState.mAnimation.setRepeatCount(Animation.INFINITE);
		mState.mAnimation.setDuration(mState.mDuration);
		mState.mAnimation.setInterpolator(mState.mInterpolator);
		mState.mAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
		mState.mAnimating = true;
		
		invalidateSelf();
	}
	
	@Override
	public boolean isRunning()
	{
		return mState.mAnimating;
	}
	
	@Override
	public void stop()
	{
		mState.mAnimating = false;
	}
	
	public void draw(Canvas canvas)
	{
		final AnimationScaleState st = mState;
		
		if(st.mDrawable == null){
			return;
		}
		
		final Rect bounds = (st.mUseBounds ? getBounds() : mTmpRect);
		
		int saveCount = canvas.save();
		canvas.scale(st.mScale, st.mScale, 
				bounds.left + bounds.width()  / 2, 
				bounds.top  + bounds.height() / 2);
		st.mDrawable.draw(canvas);
		canvas.restoreToCount(saveCount);
		
		if(st.mAnimating){
			long animTime = AnimationUtils.currentAnimationTimeMillis();
			st.mAnimation.getTransformation(animTime, st.mTransformation);
			float transformation = st.mTransformation.getAlpha();
			st.mScale = (st.mMinScale + (st.mMaxScale - st.mMinScale) 
				* (st.mInvert ? (1.0f - transformation) : transformation));
			invalidateSelf();
		}
	}
	
	@Override
	protected boolean onStateChange(int[] state)
	{
		boolean changed = false;
		if(mState.mDrawable != null){
			changed |= mState.mDrawable.setState(state);
		}
		onBoundsChange(getBounds());
		return changed;
	}
	
	@Override
	protected boolean onLevelChange(int level)
	{
		if(mState.mDrawable != null){
			mState.mDrawable.setLevel(level);
		}
		onBoundsChange(getBounds());
		return true;
	}
	
	@Override
	protected void onBoundsChange(Rect bounds)
	{
		if(mState.mDrawable != null){
			if(mState.mUseBounds){
				mState.mDrawable.setBounds(bounds);
			}
			else{
				Gravity.apply(Gravity.CENTER, getIntrinsicWidth(), 
					getIntrinsicHeight(), bounds, mTmpRect);
				mState.mDrawable.setBounds(mTmpRect);
			}
		}
	}
	
	@Override
	public int getIntrinsicWidth()
	{
		if(mState.mDrawable != null){
			return mState.mDrawable.getIntrinsicWidth();
		}else{
			return -1;
		}
	}
	
	@Override
	public int getIntrinsicHeight()
	{
		if(mState.mDrawable != null){
			return mState.mDrawable.getIntrinsicHeight();
		}
		else{
			return -1;
		}
	}
	
	public Drawable getDrawable()
	{
		return mState.mDrawable;
	}
	
	@Override
	public int getChangingConfigurations()
	{
		int changing = super.getChangingConfigurations() 
			| mState.mChangingConfigurations;
		if(mState.mDrawable != null){
			changing |= mState.mDrawable.getChangingConfigurations();
		}
		return changing;
	}
	
	public void setAlpha(int alpha)
	{
		if(mState.mDrawable != null){
			mState.mDrawable.setAlpha(alpha);
		}
	}
	
	public void setColorFilter(ColorFilter cf)
	{
		if(mState.mDrawable != null){
			mState.mDrawable.setColorFilter(cf);
		}
	}
	
	public int getOpacity()
	{
		if(mState.mDrawable != null){
			return mState.mDrawable.getOpacity();
		}
		else{
			return PixelFormat.TRANSLUCENT;
		}
	}
	
	@Override
	public void invalidateDrawable(Drawable who)
	{
		final Callback callback = getCallbackCompat();
		if(callback != null){
			callback.invalidateDrawable(this);
		}
	}
	
	@Override
	public void scheduleDrawable(Drawable who, Runnable what, long when)
	{
		final Callback callback = getCallbackCompat();
		if(callback != null){
			callback.scheduleDrawable(this, what, when);
		}
	}
	
	@Override
	public void unscheduleDrawable(Drawable who, Runnable what)
	{
		final Callback callback = getCallbackCompat();
		if(callback != null){
			callback.unscheduleDrawable(this, what);
		}
	}
	
	@SuppressLint("NewApi")
	private Callback getCallbackCompat()
	{
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			try{
				Field mCallback = getClass().getField("mCallback");
				mCallback.setAccessible(true);
				return (Callback)mCallback.get(this);
			}
			catch(IllegalArgumentException e){
				return null;
			}
			catch(IllegalAccessException e){
				return null;
			}
			catch(NoSuchFieldException e){
				return null;
			}
		}
		else{
			return getCallback();
		}
	}
	
	@Override
	public boolean getPadding(Rect padding)
	{
		if(mState.mDrawable != null){
			return mState.mDrawable.getPadding(padding);
		}
		else{
			padding.set(0, 0, 0, 0);
			return false;
		}
	}
	
	@Override
	public boolean setVisible(boolean visible, boolean restart)
	{
		if(mState.mDrawable != null){
			mState.mDrawable.setVisible(visible, restart);
		}
		return super.setVisible(visible, restart);
	}
	
	@Override
	public boolean isStateful()
	{
		if(mState.mDrawable != null){
			return mState.mDrawable.isStateful();
		}
		else{
			return false;
		}
	}
	
	@Override
	public ConstantState getConstantState()
	{
		if(mState.canConstantState()){
			mState.mChangingConfigurations = super.getChangingConfigurations();
			return mState;
		}
		return null;
	}
	
	@Override
	public AnimatedScaleDrawable mutate()
	{
		if(!mMutated && super.mutate() == this){
			mState = new AnimationScaleState(mState, this, null);
			mMutated = true;
		}
		return this;
	}
	
	
	final static class AnimationScaleState extends Drawable.ConstantState
	{
		Drawable mDrawable;
		
		int mChangingConfigurations;
		
		float   mMinScale  = 0.5f;
		float   mMaxScale  = 1.0f;
		float   mScale     = 0.0f;
		int     mDuration  = 1000;
		boolean mUseBounds = true;
		boolean mInvert    = false;
		boolean mAnimating = false;
		
		Interpolator   mInterpolator;
		Transformation mTransformation;
		AlphaAnimation mAnimation;
		
		private boolean mCanConstantState;
		private boolean mCheckedConstantState;
		
		
		public AnimationScaleState(AnimationScaleState source, AnimatedScaleDrawable owner, Resources res)
		{
			if(source != null){
				if(res != null){
					mDrawable = source.mDrawable.getConstantState().newDrawable(res);
				}
				else{
					mDrawable = source.mDrawable.getConstantState().newDrawable();
				}
				mDrawable.setCallback(owner);
				mMinScale  = mScale = source.mMinScale;
				mMaxScale  = source.mMaxScale;
				mDuration  = source.mDuration;
				mUseBounds = source.mUseBounds;
				mInvert    = source.mInvert;
				mAnimating = false;
				mCanConstantState = mCheckedConstantState = true;
			}
		}
		
		@Override
		public Drawable newDrawable()
		{
			return new AnimatedScaleDrawable(this, null);
		}
		
		@Override
		public Drawable newDrawable(Resources res)
		{
			return new AnimatedScaleDrawable(this, res);
		}
		
		@Override
		public int getChangingConfigurations()
		{
			return mChangingConfigurations;
		}
		
		public boolean canConstantState()
		{
			if(!mCheckedConstantState){
				mCanConstantState = (mDrawable.getConstantState() != null);
				mCheckedConstantState = true;
			}
			
			return mCanConstantState;
		}
	}
}
