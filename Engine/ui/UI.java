package ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import core.Resources;
import gl.Window;
import gl.res.Mesh;
import gl.ui.UIShader;
import gl.ui.UIWorldSpaceShader;
import scene.Scene;
import util.MathUtil;

public class UI {
	private static UIShader shader;
	private static UIWorldSpaceShader worldSpaceShader;
	public static final Mesh quad = Resources.QUAD2D;
	public static final int DEPTH_SEQUENTIAL = 0;
	private static List<Component> components = new ArrayList<>();
	private static Map<Matrix4f, Component> worldSpaceComponents = new LinkedHashMap<>();
	private static float opacity = 1f;
	
	public static final int width = 1280;
	public static final int height = 720;
	
	public static boolean hideUI;

	public static void addComponent(Component component) {
		final int depth = component.getDepth();

		if (depth == UI.DEPTH_SEQUENTIAL) {
			int highestDepth = 0;

			for (int i = components.size() - 1; i >= 0; --i) {
				final int compDepth = components.get(i).getDepth();
				if (compDepth >= 0) {
					if (compDepth > highestDepth) {
						highestDepth = compDepth;
					} else {
						break;
					}
				}
			}
			component.setDepth(highestDepth);
		}

		if (depth < 0) {
			for (int i = components.size() - 1; i >= 0; --i) {
				final int compDepth = components.get(i).getDepth();
				if (compDepth > depth) {
					components.add(i + 1, component);
					return;
				}
			}
		} else {
			for (int i = 0; i < components.size(); ++i) {
				final int compDepth = components.get(i).getDepth();
				if (compDepth < 0 || compDepth > depth) {
					components.add(i, component);
					return;
				}
			}
		}

		components.add(component);
	}
	
	public static void addComponent(Component component, Matrix4f matrix) {
		worldSpaceComponents.put(matrix, component);
	}

	public static void cleanUp() {
		shader.cleanUp();
		worldSpaceShader.cleanUp();
	}
	
	public static void clear() {
		components.clear();
	}

	public static void drawImage(Image image) {
		addComponent(image);
		// image.setOpacity(opacity);
		image.markAsTemporary();
	}

	public static Image drawImage(String texture, float x, float y) {
		final Image img = new Image(texture, x, y);
		img.setOpacity(opacity);
		img.markAsTemporary();
		addComponent(img);
		return img;
	}

	public static Image drawImage(String texture, float x, float y, float w, float h) {
		final Image img = new Image(texture, x, y);
		img.w = w;
		img.h = h;
		img.setOpacity(opacity);
		img.markAsTemporary();
		addComponent(img);
		return img;
	}
	
	public static Image drawImage(String texture, float x, float y, float w, float h, Vector3f color) {
		final Image img = new Image(texture, x, y);
		img.w = w;
		img.h = h;
		img.setColor(color);
		img.setOpacity(opacity);
		img.markAsTemporary();
		addComponent(img);
		return img;
	}

	public static Image drawRect(float x, float y, float width, float height, Vector3f color) {
		return drawImage("none", x, y, width, height, color);
	}

	public static void drawHollowRect(float x, float y, float width, float height, int thickness, Vector3f color) {
		drawImage("none", x, y, width, thickness, color);
		drawImage("none", x, y, thickness, height, color);
		drawImage("none", x, y + (height - thickness), width, thickness, color);
		drawImage("none", x + (width - thickness), y, thickness, height, color);
	}
	
	public static Image drawLine(float x1, float y1, float x2, float y2, float width, Vector3f color) {
		float dx = x2-x1;
		float dy = y2-y1;
		float len = (float)Math.sqrt(dx*dx + dy*dy);
		float dir = MathUtil.pointDirection(x1, y1, x2, y2);
		
		final Image img = new Image("none", x1, y1);
		img.w = len;
		img.h = width;
		img.setColor(color);
		img.setOpacity(opacity);
		img.setRotation(dir);
		img.markAsTemporary();
		img.setCentered(false);
		addComponent(img);
		return img;
	}

	public static void drawCircle(float x, float y, float radius, Vector3f color) {
		drawCircle(x, y, radius, 1, 12, color);
	}
	
	public static void drawCircle(float x, float y, float radius, float width, int partitions, Vector3f color) {
		float prt = (float) (2f * Math.PI) / partitions;
		for (float i = 0; i < 2 * Math.PI; i += prt) {
			float dx = (float) (Math.cos(i) * radius);
			float dy = (float) (Math.sin(i) * radius);
			float nx = (float) (Math.cos(i + prt) * radius);
			float ny = (float) (Math.sin(i + prt) * radius);
			drawLine(x + dx, y + dy, x + nx, y + ny, width, color);
		}
	}

	public static Text drawString(Font font, String text, float x, float y, float fontSize, float lineWidth, boolean centered,
			float... offsets) {
		final Text txt = new Text(font, text, x, y, fontSize, lineWidth, centered, offsets);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}

	public static Text drawString(String text, float x, float y) {
		final Text txt = new Text(text, x, y);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}

	public static Text drawString(String text, float x, float y, boolean centered) {
		final Text txt = new Text(Font.defaultFont, text, x, y, Font.defaultSize, Window.displayWidth / 2 - 40,
				centered);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}

	public static Text drawString(String text, float x, float y, float fontSize, boolean centered) {
		final Text txt = new Text(Font.defaultFont, text, x, y, fontSize, Window.displayWidth / 2 - 40, centered);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}
	
	public static Text drawString(String text, float x, float y, float fontSize, float lineWidth, boolean centered) {
		final Text txt = new Text(Font.defaultFont, text, x, y, fontSize, lineWidth, centered);
		txt.setOpacity(opacity);
		txt.markAsTemporary();
		addComponent(txt);
		return txt;
	}

	public static Text drawString(Text text) {
		addComponent(text);
		text.markAsTemporary();
		return text;
	}
	
	public static Text drawString(String text, float fontSize, boolean centered, Matrix4f worldMatrix) {
		final Text txt = new Text(Font.defaultFont, text, 0, 0, fontSize, Integer.MAX_VALUE, centered);
		txt.setOpacity(opacity);
		worldSpaceComponents.put(worldMatrix, txt);
		return txt;
	}
	
	public static Image drawImage(String texture, float x, float y, float w, float h, Matrix4f worldMatrix) {
		final Image img = new Image(texture, x, y);
		img.w = w;
		img.h = h / Window.getAspectRatio();
		img.setOpacity(opacity);
		worldSpaceComponents.put(worldMatrix, img);
		return img;
	}

	public static float getOpacity() {
		return opacity;
	}

	public static void init() {
		shader = new UIShader();
		worldSpaceShader = new UIWorldSpaceShader();
	}

	public static void removeComponent(Component component) {
		components.remove(component);
	}
	
	public static void update() {
		Iterator<Component> iter = components.iterator();
		while(iter.hasNext()) {
			if (iter.next().isTemporary()) {
				iter.remove();
			}
		}
		
		iter = worldSpaceComponents.values().iterator();
		while(iter.hasNext()) {
			if (iter.next().isTemporary()) {
				iter.remove();
			}
		}
	}

	public static void render(Scene scene) {
		if (hideUI) 
			return;


		GL11.glDisable(GL11.GL_DEPTH_TEST);
		renderWorldSpace(scene);

		prepare();
		
		shader.start();

		quad.bind(0, 1);

		for (final Component component : components) {
			if (component instanceof Image) {
				final Image image = (Image) component;
				image.gfx.bind(0);
				shader.color.loadVec3(image.getColor());
				shader.opacity.loadFloat(image.getOpacity());
				shader.translation.loadVec4(image.getTransform());
				shader.offset.loadVec4(image.getUvOffset());
				shader.centered.loadBoolean(image.isCentered());
				shader.rotation.loadFloat(image.getRotation());
				GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
			} else {
				// GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
				final Text text = (Text) component;
				float dx = text.offsetX / (float)width;
				float dy = text.offsetY / (float)height;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, text.getFont().getTexture().id);
				for (int i = 0; i < text.getLetters().length; i++) {
					final Image image = text.getLetters()[i];
					
					Vector4f transform = new Vector4f(image.getTransform());
					transform.x += dx;
					transform.y += dy;
					
					shader.color.loadVec3(image.getColor());
					shader.opacity.loadFloat(text.getOpacity());
					shader.translation.loadVec4(transform);
					shader.offset.loadVec4(image.getUvOffset());
					shader.centered.loadBoolean(image.isCentered());
					shader.rotation.loadFloat(image.getRotation());
					GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
				} // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
		}

		quad.unbind(0, 1);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		shader.stop();
		finish();
	}
	
	private static void renderWorldSpace(Scene scene) {
		prepare();
		
		worldSpaceShader.start();
		worldSpaceShader.projectionViewMatrix.loadMatrix(scene.getCamera().getProjectionViewMatrix());

		quad.bind(0, 1);
		for (final Matrix4f matrix : worldSpaceComponents.keySet()) {
			final Component component = worldSpaceComponents.get(matrix);
			
			worldSpaceShader.worldMatrix.loadMatrix(matrix);
			
			if (component instanceof Image) {
				//GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				final Image image = (Image) component;
				image.gfx.bind(0);
				GL11.glDisable(GL11.GL_CULL_FACE);	// HACK: Since FBOs render upside down
				worldSpaceShader.color.loadVec3(image.getColor());
				worldSpaceShader.opacity.loadFloat(image.getOpacity());
				worldSpaceShader.translation.loadVec4(image.getTransform());
				worldSpaceShader.offset.loadVec4(image.getUvOffset());
				worldSpaceShader.centered.loadBoolean(image.isCentered());
				worldSpaceShader.rotation.loadFloat(image.getRotation());
				GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
				GL11.glEnable(GL11.GL_CULL_FACE);
			} else {
				// GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
				final Text text = (Text) component;
				float dx = text.offsetX / (float)width;
				float dy = text.offsetY / (float)height;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, text.getFont().getTexture().id);
				for (int i = 0; i < text.getLetters().length; i++) {
					final Image image = text.getLetters()[i];
					
					Vector4f transform = new Vector4f(image.getTransform());
					transform.x += dx;
					transform.y += dy;
					
					worldSpaceShader.color.loadVec3(image.getColor());
					worldSpaceShader.opacity.loadFloat(text.getOpacity());
					worldSpaceShader.translation.loadVec4(transform);
					worldSpaceShader.offset.loadVec4(image.getUvOffset());
					worldSpaceShader.centered.loadBoolean(image.isCentered());
					worldSpaceShader.rotation.loadFloat(image.getRotation());
					GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
					
				}
			}
		}
		quad.unbind(0, 1);

		worldSpaceShader.stop();
		finish();
	}
	
	private static void prepare() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}
	
	private static void finish() {
		GL11.glDisable(GL11.GL_BLEND);
	}

	public static void setOpacity(float newOpacity) {
		opacity = newOpacity;
	}

	public static void updateDepth(Component component) {
		if (components.contains(component)) {
			components.remove(component);
			addComponent(component);
		}
	}

	
}
