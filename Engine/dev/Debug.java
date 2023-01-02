package dev;

import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.glGetError;

import org.joml.Vector3f;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL15;

import dev.cmd.Console;
import geom.CollideUtils;
import gl.Camera;
import gl.Render;
import gl.Window;
import gl.line.LineRender;
import gl.res.Mesh;
import gl.res.Texture;
import gl.res.Vbo;
import map.architecture.ActiveLeaves;
import map.architecture.Architecture;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcRoom;
import map.architecture.components.ArcTextureMapping;
import map.architecture.util.ArcUtil;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import scene.PlayableScene;
import scene.Scene;
import scene.entity.util.PlayerEntity;
import ui.UI;
import util.Colors;

public class Debug {
	public static boolean debugMode;
	public static boolean wireframeMode;
	public static boolean fullbright;
	public static boolean viewNavMesh, viewNavPath, viewNavNode, viewNavPois;
	public static boolean viewCollide;
	public static boolean showHitboxes;
	public static boolean showLeafs;
	public static boolean showClips;
	public static boolean showAmbient;
	public static boolean showCurrentRoom;
	public static boolean allowConsole;
	public static boolean ambientOnly, viewLightmapTexture, viewShadowTexture0;
	public static boolean faceInfo;
	public static boolean god;
	public static boolean velocityVectors;
	
	public static void checkVbo(Vbo vbo) {
		vbo.bind();
		int id = vbo.getId();
		int size = GL15.glGetBufferParameteri(GL15.GL_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
		Console.log("VBO { id=" + id, "size=" + size + "}");
		vbo.unbind();
	}

	public static void checkVao(Mesh model) {
		int id = model.id;
		int numVbos = model.getNumVbos();

		Console.log("");
		Console.log("VAO { id=" + id + "}");

		for (int i = 0; i < numVbos; i++) {
			checkVbo(model.getVbo(i));
		}

		Console.log("");
	}

	public static void update(Scene scene) {
		Camera camera = scene.getCamera();
		PlayableScene playScene = ((PlayableScene) scene);
		PlayerEntity player = playScene.getPlayer();
		Architecture arc = playScene.getArchitecture();
		Bsp bsp = arc.bsp;
		
		String cx = String.format("%.1f", camera.getPosition().x);
		String cy = String.format("%.1f", camera.getPosition().y);
		String cz = String.format("%.1f", camera.getPosition().z);
		String vx = String.format("%.1f", player.vel.x);
		String vy = String.format("%.1f", player.vel.y);
		String vz = String.format("%.1f", player.vel.z);
		String spd = String.format("%.1f", new Vector3f(player.vel.x, player.vel.z, 0f).length());

		String debugData = "\n#wFPS: " + (int) Window.framerate + "/" + Window.maxFramerate
				+ "\npos (#rX: " + cx + " #gY: " + cy + " #bZ: " + cz + "#w)"
				+ "\nvel (#rX: " + vx + " #gY: " + vy + " #bZ: " + vz + "#w)"
				+ "\n" + "spd: " + spd
				+ "\n" + "draw calls: " + Render.drawCalls
				+ "\n" + "tex swaps: " + Render.textureSwaps;
		//		+ "\n" + MemDebug.memoryInfo();
		
		UI.drawString(debugData, 5, 5, .15f, false);
		
		// MemDebug.visualizeInfo();
		
		//UI.drawImage("reflection", 0,0,512,512, Colors.WHITE);
		
		if (showCurrentRoom) {
			ArcRoom room = bsp.rooms[player.getLeaf().room];
			String name = "Undefined";
			if (room != null) name = room.getName();
			UI.drawString(name, 5, 675);
		}

		if (viewLightmapTexture) {
			UI.drawImage("lightmap", UI.width - 512, 720 - 512, 512, 512, Colors.WHITE);
		}
		if (viewShadowTexture0) {
			UI.drawImage("shadow0", UI.width - 512, 720, 512, -512, Colors.WHITE);
		}
		
		if (faceInfo) {
			int faceId = -1;

			Vector3f camPos = camera.getPosition();
			Vector3f camDir = camera.getDirectionVector();

			float nearest = Float.POSITIVE_INFINITY;
			ArcFace nearestFace = null;

			ActiveLeaves activeLeaves = arc.getActiveLeaves();
			while(activeLeaves.hasNext()) {
				BspLeaf leaf = activeLeaves.next();
				for (int i = 0; i < leaf.numFaces; i++) {
					ArcFace face = bsp.faces[bsp.leafFaceIndices[leaf.firstFace + i]];
					float dist = CollideUtils.convexPolygonRay(bsp, face, camPos, camDir);

					if (!Float.isNaN(dist) && dist < nearest) {
						dist = nearest;
						nearestFace = face;

						faceId = bsp.leafFaceIndices[leaf.firstFace + i];
					}
				}
			}

			if (nearestFace != null) {
				
				int surfEdge = bsp.surfEdges[bsp.faces[faceId].firstEdge];
				ArcEdge oedge = bsp.edges[Math.abs(surfEdge)];
				int vertId = (surfEdge > 0) ? oedge.start : oedge.end;
				LineRender.drawBox(bsp.vertices[vertId], new Vector3f(1,1,1), Colors.VIOLET);
				
				for (int i = nearestFace.firstEdge; i < nearestFace.numEdges + nearestFace.firstEdge; i++) {
					ArcEdge edge = bsp.edges[Math.abs(bsp.surfEdges[i])];
					Vector3f A = bsp.vertices[edge.start];
					Vector3f B = bsp.vertices[edge.end];
					Vector3f N = bsp.planes[nearestFace.planeId].normal;
					
					if (nearestFace.texMapping == -1)
						continue;
					
					ArcTextureMapping texInfo = bsp.getTextureMappings()[nearestFace.texMapping];
					Texture tex = null;
					String texName = "N/A", texMat = "N/A";
					String[] textureNames = arc.getTextureNames();
					Texture[] textures = arc.getTextures();
					if (texInfo.textureId != -1 && texInfo.textureId < textureNames.length) {
						tex = textures[texInfo.textureId];
						texName = textureNames[texInfo.textureId];
						texMat = tex.getMaterial().name();
					}
					
					Vector3f tangent = ArcUtil.getFaceTangent(bsp.vertices, bsp.edges, bsp.surfEdges, bsp.planes, bsp.getTextureMappings(), nearestFace);
					
					// Behold! Hell itself
					float[][] tv = texInfo.lmVecs;
					String info = "Face " + faceId + " " + nearestFace.texMapping  + "/" + texInfo.textureId
					+ "\nedges [" + nearestFace.firstEdge + "-" + (nearestFace.firstEdge+nearestFace.numEdges) +"]"
					+ "\ntexmap info:" + nearestFace.texMapping
					+ "\nlm index:" + nearestFace.lmIndex
					+ "\ntexture: " + texName + " (id=" + texInfo.textureId + ")"
					+ "\nmaterial: " + texMat
					+ "\nlightmap:\n    mins: (" +nearestFace.lmMins[0] + ", " + nearestFace.lmMins[1] + ")"
					+ "\n    sizes: (" +nearestFace.lmSizes[0] + ", " + nearestFace.lmSizes[1] + ")"
					+ "\n    styles: (" +nearestFace.lmStyles[0] + ", " + nearestFace.lmStyles[1] + ", " + nearestFace.lmStyles[2] + ", " + nearestFace.lmStyles[3] + ")"
					+ "\n    tex vec (U): [" + tv[0][0] + ", " + tv[0][1] + ", " + tv[0][2] + ", " + tv[0][3] + "]"
					+ "\n    tex vec (V): [" + tv[1][0] + ", " + tv[1][1] + ", " + tv[1][2] + ", " + tv[1][3] + "]"
					+ "\n\" texture:\n    lm scales: (" +nearestFace.lightmapScaleX + ", " + nearestFace.lightmapScaleY + ")"
					+ "\n    lm offsets: (" +nearestFace.lightmapOffsetX + ", " + nearestFace.lightmapOffsetY + ")"
					+ "\ntangentt: " + tangent
					;
					
					UI.drawString(info, 640, 376, .175f, false);
					
					if (Debug.viewLightmapTexture) {
						for(int j = 0; j < nearestFace.lightmapOffsetX.length; j++) {
							float u1, u2, v1, v2;
							u1 = (tv[0][0] * A.x + tv[0][1] * A.y + tv[0][2] * A.z + tv[0][3] - nearestFace.lmMins[0]) / (nearestFace.lmSizes[0] + 1);
							v1 = (tv[1][0] * A.x + tv[1][1] * A.y + tv[1][2] * A.z + tv[1][3] - nearestFace.lmMins[1]) / (nearestFace.lmSizes[1] + 1);
							u2 = (tv[0][0] * B.x + tv[0][1] * B.y + tv[0][2] * B.z + tv[0][3] - nearestFace.lmMins[0]) / (nearestFace.lmSizes[0] + 1);
							v2 = (tv[1][0] * B.x + tv[1][1] * B.y + tv[1][2] * B.z + tv[1][3] - nearestFace.lmMins[1]) / (nearestFace.lmSizes[1] + 1);
							u1 = u1 * nearestFace.lightmapScaleX + nearestFace.lightmapOffsetX[j];
							v1 = v1 * nearestFace.lightmapScaleY + nearestFace.lightmapOffsetY[j];
							u2 = u2 * nearestFace.lightmapScaleX + nearestFace.lightmapOffsetX[j];
							v2 = v2 * nearestFace.lightmapScaleY + nearestFace.lightmapOffsetY[j];

							u1 = (UI.width - 512) + (u1 * 512);
							u2 = (UI.width - 512) + (u2 * 512);
							v1 = (UI.height - 512) + (v1 * 512);
							v2 = (UI.height - 512) + (v2 * 512);
							UI.drawLine((int)u1, (int)v1, (int)u2, (int)v2, 1, Colors.RED);
						}
					}
					
					Vector3f center1 = Vector3f.add(A, B).mul(.5f);
					Vector3f bounds1 = Vector3f.sub(B, A).mul(.5f);
					
					
					final float R = 2f;//MapFileBuilder.HMR_ARC_SCALE_DIFF*3f;
					final float dR = R*2f;
					if (bounds1.x <= dR) bounds1.x = Math.max(Math.abs(bounds1.x), dR);
					if (bounds1.y <= dR) bounds1.y = Math.max(Math.abs(bounds1.y), dR);
					if (bounds1.z <= dR) bounds1.z = Math.max(Math.abs(bounds1.z), dR);
					
					Vector3f trim = new Vector3f(R,R,R);
					bounds1 = bounds1.sub(trim);
					LineRender.drawLine(Vector3f.add(A, N), Vector3f.add(B, N), (i % 2 == 0) ? Colors.PINK : Colors.PURPLE);
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
