package shader;

import org.lwjgl.opengl.GL20;

import dev.cmd.Console;

public abstract class Uniform {

	private static final int NOT_FOUND = -1;

	private final String uniformName;
	private int location;

	protected Uniform(String name) {
		this.uniformName = name;
	}

	public int getLocation() {
		return location;
	}

	protected void storeUniformLocation(String shaderName, int programID) {
		location = GL20.glGetUniformLocation(programID, uniformName);
		
		if (location == NOT_FOUND) {
			Console.warning("No uniform variable \'" + uniformName + "\' found in the " + shaderName + " shader");
		}
	}

}
