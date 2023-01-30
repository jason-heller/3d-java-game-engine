package util;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;

public class Matrices {
	public static Matrix4f mul(Matrix4f left, Matrix4f right) {
		Matrix4f result = new Matrix4f();
		left.mul(right, result);
		return result;
	}
	
	public static void mul(Matrix4f left, Matrix4f right, Matrix4f dest) {
		dest.set(left).mul(right);
	}
}
