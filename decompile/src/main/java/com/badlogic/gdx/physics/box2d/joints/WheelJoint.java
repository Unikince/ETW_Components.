package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/**
 *  A wheel joint. This joint provides two degrees of freedom: translation
 * along an axis fixed in body1 and rotation in the plane. You can use a
 * joint limit to restrict the range of motion and a joint motor to drive
 * the rotation or to model rotational friction.
 * This joint is designed for vehicle suspensions.
 */
public class WheelJoint extends Joint {
	public WheelJoint (World world, long addr) {
		super(world, addr);
	}

	/**
	 *  Get the current joint translation, usually in meters.
	 */
		public float getJointTranslation() {
			return jniGetJointTranslation(addr);
		}
		
		private native float jniGetJointTranslation(long addr);

		/**
		 *  Get the current joint translation speed, usually in meters per second.
		 */
		public float getJointSpeed() {
			return jniGetJointSpeed(addr);
		}
		
		private native float jniGetJointSpeed(long addr);

		/**
		 *  Is the joint motor enabled?
		 */
		private boolean isMotorEnabled() {
			return jniIsMotorEnabled(addr);
		}
		
		private native boolean jniIsMotorEnabled(long addr);

		/**
		 *  Enable/disable the joint motor.
		 */
		public void enableMotor(boolean flag) {
			jniEnableMotor(addr, flag);
		}
		
		private native void jniEnableMotor(long addr, boolean flag);

		/**
		 *  Set the motor speed, usually in radians per second.
		 */
		public void setMotorSpeed(float speed) {
			jniSetMotorSpeed(addr, speed);
		}
		
		private native void jniSetMotorSpeed(long addr, float speed);

		/**
		 *  Get the motor speed, usually in radians per second.
		 */
		public float getMotorSpeed() {
			return jniGetMotorSpeed(addr);
		}
		
		private native float jniGetMotorSpeed(long addr);

		/**
		 *  Set/Get the maximum motor force, usually in N-m.
		 */
		public void setMaxMotorTorque(float torque) {
			jniSetMaxMotorTorque(addr, torque);
		}
		
		private native void jniSetMaxMotorTorque(long addr, float torque);
		
		public float getMaxMotorTorque() {
			return jniGetMaxMotorTorque(addr);
		}
		
		private native float jniGetMaxMotorTorque(long addr);

		/**
		 *  Get the current motor torque given the inverse time step, usually in N-m.
		 */
		public float getMotorTorque(float invDt) {
			return jniGetMotorTorque(addr, invDt);
		}
		
		private native float jniGetMotorTorque(long addr, float invDt);

		/**
		 *  Set/Get the spring frequency in hertz. Setting the frequency to zero disables the spring.
		 */
		public void setSpringFrequencyHz(float hz) {
			jniSetSpringFrequencyHz(addr, hz);
		}
		
		private native void jniSetSpringFrequencyHz(long addr, float hz);
		
		public float getSpringFrequencyHz() {
			return jniGetSpringFrequencyHz(addr);
		}
		
		private native float jniGetSpringFrequencyHz(long addr);

		/** 
		 * Set/Get the spring damping ratio
		 */
		public void setSpringDampingRatio(float ratio) {
			jniSetSpringDampingRatio(addr, ratio);
		}
		
		private native void jniSetSpringDampingRatio(long addr, float ratio);
		
		
		public float getSpringDampingRatio() {
			return jniGetSpringDampingRatio(addr);
		}
		
		private native float jniGetSpringDampingRatio(long addr);
}
