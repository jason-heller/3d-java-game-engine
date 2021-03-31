package dev;

import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.glGetError;

import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL15;

import core.Resources;
import geom.CollideUtils;
import gl.Camera;
import gl.Window;
import gl.line.LineRender;
import gl.res.Model;
import gl.res.Texture;
import gl.res.Vbo;
import map.architecture.Architecture;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcTextureData;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.Scene;
import scene.entity.utility.PlayerEntity;
import ui.UI;
import util.Colors;

public class Debug {
	public static boolean debugMode = false;
	public static boolean wireframeMode = false;
	public static boolean fullbright = false;
	public static boolean viewNavMesh = false, viewNavPath = false;
	public static boolean viewCollide = false;
	public static boolean showHitboxes = false;
	public static boolean showLeafs = false;
	public static boolean showClips = false;
	public static boolean showAmbient = false;
	public static boolean allowConsole;
	public static boolean ambientOnly, viewLightmapTexture;
	public static boolean faceInfo;
	public static boolean god;

	public static void checkVbo(Vbo vbo) {
		vbo.bind();
		int id = vbo.getId();
		int size = GL15.glGetBufferParameteri(GL15.GL_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
		Console.log("VBO { id=" + id, "size=" + size + "}");
		vbo.unbind();
	}

	public static void checkVao(Model model) {
		int id = model.id;
		int numVbos = model.getNumVbos();

		Console.log("");
		Console.log("VAO { id=" + id + "}");

		for (int i = 0; i < numVbos; i++) {
			checkVbo(model.getVbo(i));
		}

		Console.log("");
	}

	public static void uiDebugInfo(Scene scene) {
		Camera camera = scene.getCamera();
		PlayableScene playScene = ((PlayableScene) scene);
		PlayerEntity player = playScene.getPlayer();
		String cx = String.format("%.1f", camera.getPosition().x);
		String cy = String.format("%.1f", camera.getPosition().y);
		String cz = String.format("%.1f", camera.getPosition().z);
		String vx = String.format("%.1f", player.vel.x);
		String vy = String.format("%.1f", player.vel.y);
		String vz = String.format("%.1f", player.vel.z);

		UI.drawString(
				 "\n#rX: " + cx + " #gY: " + cy + " #bZ: " + cz
				+ "\n" + "#wFPS: " + (int) Window.framerate + "/" + Window.maxFramerate 
				+ "\n" + "\nVX: " + vx + " VY: " + vy + " VZ: " + vz 
				+ "\ngrounded:" + player.isGrounded() + ", prev: " + player.previouslyGrounded 
				+ "\n",
				5, 5, .25f, false);

		if (viewLightmapTexture) {
			UI.drawImage("lightmap", 1280 - 512, 720 - 512, 512, 512, Colors.WHITE);
		}

		if (faceInfo) {
			int faceId = -1;
			Architecture arc = playScene.getArcHandler().getArchitecture();
			Bsp bsp = arc.bsp;
			List<BspLeaf> allVisible = arc.getRenderedLeaves();

			Vector3f camPos = camera.getPosition();
			Vector3f camDir = camera.getDirectionVector();

			float nearest = Float.POSITIVE_INFINITY;
			ArcFace nearestFace = null;

			for (BspLeaf leaf : allVisible) {
				for (int i = 0; i < leaf.numFaces; i++) {
					ArcFace face = bsp.faces[bsp.leafFaceIndices[leaf.firstFace + i]];
					float dist = CollideUtils.convexPolygonRay(bsp.vertices, bsp.edges, bsp.surfEdges, bsp.planes, face,
							camPos, camDir);

					if (!Float.isNaN(dist) && dist < nearest) {
						dist = nearest;
						nearestFace = face;

						faceId = leaf.firstFace + i;
					}
				}
			}

			if (nearestFace != null) {
				
				for (int i = nearestFace.firstEdge; i < nearestFace.numEdges + nearestFace.firstEdge; i++) {
					ArcEdge edge = bsp.edges[Math.abs(bsp.surfEdges[i])];
					Vector3f A = bsp.vertices[edge.start];
					Vector3f B = bsp.vertices[edge.end];
					
					ArcTextureData texData = arc.getPackedAssets().getTextureData()[nearestFace.texId];
					Texture tex = null;
					String texName = "N/A", texMat = "N/A";
					if (texData.textureId != -1 && texData.textureId-3 < arc.getReferencedTextures().length) {
						tex = arc.getReferencedTextures()[texData.textureId-3];
						texName = arc.getReferencedTexNames()[texData.textureId];
						texMat = tex.getMaterial().name();
					}
					
					// Behold! Hell itself
					float[][] tv = texData.lmVecs;
					String info = "Face #" + faceId
					+ "\nedges [" + nearestFace.firstEdge + "-" + (nearestFace.firstEdge+nearestFace.numEdges) +"]"
					+ "\ntexmap info:" + nearestFace.texId
					+ "\nlm index:" + nearestFace.lmIndex
					+ "\ntexture: " + texName + " (id=" + texData.textureId + ")"
					+ "\nmaterial: " + texMat
					+ "\nlightmap:\n    mins: (" +nearestFace.lmMins[0] + ", " + nearestFace.lmMins[1] + ")"
					+ "\n    sizes: (" +nearestFace.lmSizes[0] + ", " + nearestFace.lmSizes[1] + ")"
					+ "\n    styles: (" +nearestFace.lmStyles[0] + ", " + nearestFace.lmStyles[1] + ", " + nearestFace.lmStyles[2] + ", " + nearestFace.lmStyles[3] + ")"
					+ "\n    tex vec (U): [" + tv[0][0] + ", " + tv[0][1] + ", " + tv[0][2] + ", " + tv[0][3] + "]"
					+ "\n    tex vec (V): [" + tv[1][0] + ", " + tv[1][1] + ", " + tv[1][2] + ", " + tv[1][3] + "]"
					+ "\n\" texture:\n    lm scales: (" +nearestFace.lightmapScaleX + ", " + nearestFace.lightmapScaleY + ")"
					+ "\n    lm offsets: (" +nearestFace.lightmapOffsetX + ", " + nearestFace.lightmapOffsetY + ")"
					;
					
					UI.drawString(info, 640, 376, .175f, false);
					
					if (Debug.viewLightmapTexture) {
						float u1, u2, v1, v2;
						u1 = (tv[0][0] * A.x + tv[0][1] * A.y + tv[0][2] * A.z + tv[0][3] - nearestFace.lmMins[0]) / (nearestFace.lmSizes[0] + 1);
						v1 = (tv[1][0] * A.x + tv[1][1] * A.y + tv[1][2] * A.z + tv[1][3] - nearestFace.lmMins[1]) / (nearestFace.lmSizes[1] + 1);
						u2 = (tv[0][0] * B.x + tv[0][1] * B.y + tv[0][2] * B.z + tv[0][3] - nearestFace.lmMins[0]) / (nearestFace.lmSizes[0] + 1);
						v2 = (tv[1][0] * B.x + tv[1][1] * B.y + tv[1][2] * B.z + tv[1][3] - nearestFace.lmMins[1]) / (nearestFace.lmSizes[1] + 1);
						u1 = u1 * nearestFace.lightmapScaleX + nearestFace.lightmapOffsetX;
						v1 = v1 * nearestFace.lightmapScaleY + nearestFace.lightmapOffsetY;
						u2 = u2 * nearestFace.lightmapScaleX + nearestFace.lightmapOffsetX;
						v2 = v2 * nearestFace.lightmapScaleY + nearestFace.lightmapOffsetY;

						u1 = (1280 - 512) + (u1 * 512);
						u2 = (1280 - 512) + (u2 * 512);
						v1 = (720 - 512) + (v1 * 512);
						v2 = (720 - 512) + (v2 * 512);
						UI.drawLine((int)u1, (int)v1, (int)u2, (int)v2, 1, Colors.RED);
					}
					
					LineRender.drawLine(A, B, Colors.PINK);
				}
			}
		}
	}

	public static void testGLError() {
		try {
			if (!Display.isCurrent())
				return;
		} catch (LWJGLException e) {
			e.printStackTrace();
			return;
		}
		String AllErrors = "";
		int GL_Error;
		do {
			GL_Error = glGetError();
			switch (GL_Error) {
			case GL_NO_ERROR:
				break;
			case GL_INVALID_ENUM:
				AllErrors += "GL_INVALID_ENUM";
				break;
			case GL_INVALID_VALUE:
				AllErrors += "GL_INVALID_VALUE";
				break;
			case GL_INVALID_OPERATION:
				AllErrors += "GL_INVALID_OPERATION";
				break;
			case GL_STACK_OVERFLOW:
				AllErrors += "GL_STACK_OVERFLOW";
				break;
			case GL_STACK_UNDERFLOW:
				AllErrors += "GL_STACK_UNDERFLOW";
				break;
			case GL_OUT_OF_MEMORY:
				AllErrors += "GL_OUT_OF_MEMORY";
				break;
			default:
				AllErrors += "unknown gl error";
				break;
			}
		} while (GL_Error != GL_NO_ERROR);

		System.err.println(AllErrors);
	}
}
