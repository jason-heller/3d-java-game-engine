package map.architecture;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import core.Resources;
import dev.Console;
import geom.AxisAlignedBBox;
import geom.Plane;
import gr.zdimensions.jsquish.Squish;
import io.FileUtils;
import map.architecture.components.ArcClip;
import map.architecture.components.ArcEdge;
import map.architecture.components.ArcFace;
import map.architecture.components.ArcLightCube;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcObjects;
import map.architecture.components.ArcPackedAssets;
import map.architecture.components.ArcPackedTexture;
import map.architecture.components.ArcStaticObject;
import map.architecture.components.ArcTextureData;
import map.architecture.components.ArcTriggerClip;
import map.architecture.components.ClipType;
import map.architecture.functions.commands.CamView;
import map.architecture.functions.commands.PathNode;
import map.architecture.functions.commands.SpawnPoint;
import map.architecture.vis.Bsp;
import map.architecture.vis.BspLeaf;
import map.architecture.vis.BspNode;
import map.architecture.vis.Pvs;
import scene.PlayableScene;
import scene.Scene;
import scene.entity.EntityHandler;
import scene.entity.friendly.BoyNPC;

// Architecture File (for map geom)
public class ArcLoader {

	private static final byte EXPECTED_GAME_ID = 2;
	private static final byte EXPECTED_FILE_VERSION = 3;
	
	private static boolean verbose = false;
	
	public static Architecture load(Scene scene, String mapFileName, Vector3f offset, boolean doRender) {
		DataInputStream in = null;
		String fileName = "maps/" + mapFileName + ".arc";

		Architecture arc = new Architecture(scene);
		Bsp bsp = new Bsp();
		Pvs pvs = new Pvs();
		ArcNavigation nav = new ArcNavigation();
		Vector3f sunVector = new Vector3f(.5f, -.5f, 0);

		ArcPackedAssets packedAssets = new ArcPackedAssets(arc);
		arc.setPackedAssets(packedAssets);
		
		boolean successfulLoad = false;

		try {
			in = new DataInputStream(new FileInputStream(fileName));

			// Header
			String identifier = new String(new byte[] { in.readByte(), in.readByte(), in.readByte() });
			if (!identifier.equals("ARC")) {
				Console.log("Error: Tried to load an " + identifier + " file as an ARC file");
				return null;
			}

			byte versionId = in.readByte();
			byte gameId = in.readByte();
			
			if (versionId != EXPECTED_FILE_VERSION) {
				Console.log("Error: ARC file is version " + versionId + ", expected version " + EXPECTED_FILE_VERSION);
				return null;
			}

			if (gameId != EXPECTED_GAME_ID) {
				Console.log("Error: ARC file is not formatted for this game (game id is " + gameId + ")");
				return null;
			}

			byte mapVer = in.readByte();
			String mapName = FileUtils.readString(in);
			
			// Planes
			verbose("Loading planes");
			Plane[] planes = new Plane[in.readInt()];
			for (int i = 0; i < planes.length; ++i) {
				planes[i] = new Plane();
				planes[i].normal = FileUtils.readVec3(in);
				planes[i].dist = in.readFloat();
			}
			bsp.planes = planes;

			// Verts
			verbose("Loading vertices");
			Vector3f[] verts = new Vector3f[in.readInt()];
			for (int i = 0; i < verts.length; ++i) {
				verts[i] = FileUtils.readVec3(in);
				verts[i].add(offset);
			}
			bsp.vertices = verts;

			// Edges
			verbose("Loading edges");
			ArcEdge[] edges = new ArcEdge[in.readInt()];
			for (int i = 0; i < edges.length; ++i) {
				edges[i] = new ArcEdge();
				edges[i].start = in.readInt();
				edges[i].end = in.readInt();
			}
			bsp.edges = edges;

			// Surfedges
			verbose("Loading surface edges");
			int[] surfEdges = new int[in.readInt()];
			for (int i = 0; i < surfEdges.length; ++i) {
				surfEdges[i] = in.readInt();
			}
			bsp.surfEdges = surfEdges;

			// faces
			verbose("Loading faces");
			ArcFace[] faces = new ArcFace[in.readInt()];
			for (int i = 0; i < faces.length; ++i) {
				faces[i] = new ArcFace();
				faces[i].onNode = in.readByte();
				faces[i].planeId = in.readShort();
				faces[i].firstEdge = in.readInt();
				faces[i].numEdges = in.readShort();
				faces[i].texId = in.readShort();
				faces[i].lmIndex = in.readInt();
				faces[i].lmMins = new float[] {in.readFloat(), in.readFloat()};
				faces[i].lmSizes = new float[] {in.readFloat(), in.readFloat()};
				faces[i].lmStyles = new byte[] {in.readByte(), in.readByte(), in.readByte(), in.readByte()};

			}
			
			// clips
			verbose("Loading clips");
			int[] clipEdges = new int[in.readShort()];
			for (int i = 0; i < clipEdges.length; i++) {
				clipEdges[i] = in.readShort();		// ptr to edge
			}
			bsp.clipPlaneIndices = clipEdges;
			int numClips = in.readShort();
			/*int numTriggerEvents = */in.readShort();	// Unused
			// int triggerIndex = 0;
			ArcClip[] clips = new ArcClip[numClips];
			
			for (int i = 0; i < clips.length; i++) {
				ClipType clipType = ClipType.values()[in.readByte()];
				Vector3f center, bounds;
				
				if (clipType == ClipType.TRIGGER) {
					ArcTriggerClip trigger = new ArcTriggerClip(arc);
					trigger.firstPlane = in.readShort(); // ptr to clipedges
					trigger.numPlanes = in.readShort();
					center = new Vector3f(in.readShort(), in.readShort(), in.readShort());
					bounds = new Vector3f(in.readShort(), in.readShort(), in.readShort());
					
					trigger.commandEnter = FileUtils.readString(in);
					trigger.commandExit = FileUtils.readString(in);
					trigger.style = in.readByte();
					trigger.whitelist = FileUtils.readString(in).split(",");
					
					clips[i] = trigger;
				} else {
					clips[i] = new ArcClip();
					clips[i].firstPlane = in.readShort(); // ptr to clipedges
					clips[i].numPlanes = in.readShort();
					
					center = new Vector3f(in.readShort(), in.readShort(), in.readShort());
					bounds = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				}
				
				clips[i].id = clipType;
				clips[i].bbox = new AxisAlignedBBox(center, bounds);
			}
			bsp.clips = clips;

			// nodes
			verbose("Loading nodes");
			short[] leafIds = new short[in.readInt()];
			for (int i = 0; i < leafIds.length; i++) {
				leafIds[i] = in.readShort();
			}
			bsp.faces = faces;
			bsp.leafFaceIndices = leafIds;

			BspNode[] nodes = new BspNode[in.readInt()];
			for (int i = 0; i < nodes.length; ++i) {
				nodes[i] = new BspNode();
				nodes[i].planeNum = in.readInt();
				nodes[i].childrenId[0] = in.readInt();
				nodes[i].childrenId[1] = in.readInt();
				nodes[i].min = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				nodes[i].max = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				nodes[i].firstFace = in.readShort();
				nodes[i].numFaces = in.readShort();
			}

			bsp.nodes = nodes;

			// leafs
			verbose("Loading leafs");
			BspLeaf[] leaves = new BspLeaf[in.readInt()];
			int numClusterLeaves = 0;
			for (int i = 0; i < leaves.length; ++i) {
				BspLeaf leaf = new BspLeaf();
				leaf.clusterId = in.readShort();
				leaf.min = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				leaf.max = new Vector3f(in.readShort(), in.readShort(), in.readShort());
				leaf.firstFace = in.readShort();
				leaf.numFaces = in.readShort();
				leaf.firstAmbientSample = in.readShort();
				leaf.numAmbientSamples = in.readShort();
				leaf.isUnderwater = in.readByte() != -1;
				
				int numClipsInLeaf = in.readByte();
				leaf.clips = new short[numClipsInLeaf];
				for(int j = 0; j < numClipsInLeaf; j++) {
					leaf.clips[j] = in.readShort();
				}

				if (leaf.clusterId != -1) {
					numClusterLeaves++;
				}
				
				leaves[i] = leaf;
			}

			BspLeaf[] clusters = new BspLeaf[numClusterLeaves];
			int j = 0;
			for (int i = 0; i < leaves.length; ++i) {
				if (leaves[i].clusterId != -1) {
					clusters[j++] = leaves[i];
				}
			}
			bsp.leaves = leaves;

			// face ids for leafs
			short[] faceIds = new short[in.readInt()];

			for (int i = 0; i < faceIds.length; ++i) {
				faceIds[i] = in.readShort();
			}

			// entities
			verbose("Loading entities");
			int numEnts = in.readInt();
			
			for (int i = 0; i < numEnts; i++) {
				String name = FileUtils.readString(in);

				byte numTags = in.readByte();
				Map<String, String> tags = new HashMap<String, String>();
				for (byte k = 0; k < numTags; k++) {
					String key = FileUtils.readString(in);
					String val = FileUtils.readString(in);
					tags.put(key, val);
				}
				
				switch(name) {
				case "spawn_player":
					SpawnPoint spawn = new SpawnPoint(readVec3(tags, "pos"), readVec3(tags, "rot"), tags.get("label"));
					arc.addFunction(spawn);
					break;
				case "npc_boy":
					BoyNPC npc = new BoyNPC(readVec3(tags, "pos"), readInt(tags, "target"), ((PlayableScene)scene).getPlayer());
					EntityHandler.addEntity(npc);
					break;
				case "path_node":
					PathNode pathNode = new PathNode(readVec3(tags, "pos"), readInt(tags, "id"), readInt(tags, "next"), readInt(tags, "prev"), tags.get("cmd"));
					arc.addFunction(pathNode);
					break;
				case "cam_view":
					CamView camView = new CamView(readVec3(tags, "pos"), readVec3(tags, "rot"));
					arc.addFunction(camView);
					break;
				}
			}
			
			// objects
			String[] objModelReference = new String[in.readShort()];
			for(int i = 0; i < objModelReference.length; i++) {
				objModelReference[i] = FileUtils.readString(in);
			}
			
			short[] objLeafResidence = new short[in.readShort()];
			for(int i = 0; i < objLeafResidence.length; i++) {
				objLeafResidence[i] = in.readShort();
			}
			
			ArcStaticObject[] objects = new ArcStaticObject[in.readShort()];
			for(int i = 0; i < objects.length; i++) {
				ArcStaticObject obj = new ArcStaticObject();
				obj.pos = FileUtils.readVec3(in);
				obj.rot = FileUtils.readVec3(in);
				obj.lightingPos = FileUtils.readVec3(in);
				obj.objLeafResIndex = in.readShort();
				obj.numObjLeafRes = in.readByte();
				obj.model = in.readShort();
				obj.solidity = in.readByte();
				obj.visRange = in.readFloat();
				objects[i] = obj;
			}
			
			bsp.objects = new ArcObjects(objModelReference, objLeafResidence, objects);
			
			// vis
			verbose("Loading BSP tree");
			int numClusters = in.readInt();
			pvs.setNumClusters(numClusters);

			int[][] clusterPointers = new int[numClusters][2];
			for (int i = 0; i < numClusters; ++i) {
				clusterPointers[i][0] = in.readInt();
				clusterPointers[i][1] = in.readInt();
			}

			pvs.setClusterPointers(clusterPointers);

			int visLen = in.readInt();
			byte[] visData = new byte[visLen];
			for (int i = 0; i < visLen; ++i) {
				visData[i] = in.readByte();
			}

			pvs.setVisData(visData);
			
			// Navigation
			verbose("Loading navigation data");
			short numNavElements = in.readShort();
			nav.initBroadphase(numNavElements);
			for(int i = 0; i < numNavElements; i++) {
				float x = in.readFloat(), y = in.readFloat(), z = in.readFloat();
				int leaf = in.readInt();
				short[] neighbors = new short[in.readByte()];
				for(int n = 0; n < neighbors.length; n++) {
					neighbors[n] = in.readShort();
				}
				nav.addNode(i, x, y, z, leaf, neighbors);
			}
			
			numNavElements = in.readShort();
			nav.initNarrowphase(numNavElements);
			for(int i = 0; i < numNavElements; i++) {
				float x = in.readFloat(), y = in.readFloat(), z = in.readFloat();
				int leaf = in.readInt();
				short[] neighbors = new short[in.readByte()];
				for(int n = 0; n < neighbors.length; n++) {
					neighbors[n] = in.readShort();
				}
				nav.addFace(i, x, y, z, leaf, neighbors);
			}
			
			// Texture Info
			verbose("Loading textures");
			
			String skybox = FileUtils.readString(in);
			packedAssets.skybox = skybox;
			
			ArcTextureData[] texData = new ArcTextureData[in.readInt()];
			for (int i = 0; i < texData.length; ++i) {
				texData[i] = new ArcTextureData();
				texData[i].textureId = in.readInt();
				texData[i].texels[0][0] = in.readFloat();
				texData[i].texels[0][1] = in.readFloat();
				texData[i].texels[0][2] = in.readFloat();
				texData[i].texels[0][3] = in.readFloat();
				texData[i].texels[1][0] = in.readFloat();
				texData[i].texels[1][1] = in.readFloat();
				texData[i].texels[1][2] = in.readFloat();
				texData[i].texels[1][3] = in.readFloat();
				texData[i].lmVecs[0][0] = in.readFloat();
				texData[i].lmVecs[0][1] = in.readFloat();
				texData[i].lmVecs[0][2] = in.readFloat();
				texData[i].lmVecs[0][3] = in.readFloat() / 6f;
				texData[i].lmVecs[1][0] = in.readFloat();
				texData[i].lmVecs[1][1] = in.readFloat();
				texData[i].lmVecs[1][2] = in.readFloat();
				texData[i].lmVecs[1][3] = in.readFloat() / 6f;
			}

			// Texture list
			String[] textures = new String[in.readInt()];
			for (int i = 0; i < textures.length; ++i) {
				textures[i] = FileUtils.readString(in);
				
				/*
				 * STRING name BYTE compression BYTE material SHORT width SHORT height INT
				 * dataLen BYTE[] data
				 */
				byte compression = in.readByte();
				if (compression != 0) {
					byte material = in.readByte();
					int width = in.readShort();
					int height = in.readShort();
					int dataLen = in.readInt();
					byte[] textureData = new byte[dataLen];
					for (int l = 0; l < dataLen; l++) {
						textureData[l] = in.readByte();
					}
					Squish.CompressionType compType;
					switch (compression) {
					case 1:
						compType = Squish.CompressionType.DXT1;
						break;
					case 3:
						compType = Squish.CompressionType.DXT3;
						break;
					default:
						compType = Squish.CompressionType.DXT5;
					}
					byte[] decompressedData = Squish.decompressImage(null, width, height, textureData, compType);
					ArcPackedTexture t = new ArcPackedTexture(textures[i], material, decompressedData, width, height);
					packedAssets.add(textures[i], t);
				} else {
					packedAssets.numToolTextures++;
				}
			}
			
			// Baked Lighting
			int lmDataLenBytes = in.readInt();
			byte[] rgb = new byte[lmDataLenBytes];
			for(int i = 0; i < lmDataLenBytes; i++) {
				rgb[i] = in.readByte();
			}
			
			// Ambient lighting
			int numLightCubes = in.readInt();
			ArcLightCube[] lightCubes = new ArcLightCube[numLightCubes];
			for(int i = 0; i < numLightCubes; i++) {
				ArcLightCube lightCube = new ArcLightCube();
				//byte[] r = new byte[6], g = new byte[6], b = new byte[6], a = new byte[6];
				int[] colors = new int[6];
				for(int k = 0; k < 6; k++) {
					//lightInfoCompressed[k] = in.readInt() & 0xFFFFFFFF;	// r,g,b,exp
					int ch1 = in.read();
			        int ch2 = in.read();
			        int ch3 = in.read();
			        int ch4 = in.readByte();
					colors[k] = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
				}
				
				lightCube.colors = colors;
				
				lightCube.x = in.read() / 255f;
				lightCube.y = in.read() / 255f;
				lightCube.z = in.read() / 255f;
				
				lightCubes[i] = lightCube;
			}

			packedAssets.add(clusters, planes, edges, surfEdges, verts, faces, leafIds, texData);

			arc.setProperties(mapName, mapVer, gameId);
			arc.setSunVector(sunVector);
			//
			arc.bsp = bsp;
			arc.pvs = pvs;
			arc.faces = faces;
			arc.setNavigation(nav);
			
			arc.createLightmap(rgb, faces);
			arc.ambientLightCubes = lightCubes;
			packedAssets.passToOpenGL();
			
			successfulLoad = true;
			Console.log("Map loaded: " + mapName + " version=" + mapVer);
			
			return arc;

		} catch (FileNotFoundException e) {
			Console.log("Tried to load " + mapFileName + ", failed");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (successfulLoad) {
				// Model loading

				String assetLocation = "maps/" + mapFileName + "/";
				String[] modelFiles = bsp.objects.getModelReference();
				for(String modelFile : modelFiles) {
					String path = assetLocation + modelFile;
					Resources.addModel(modelFile, path);
				}
			}
		}
	}

	private static void verbose(String string) {
		if (verbose) {
			Console.log(string);
		}
	}

	@SuppressWarnings("unused")
	private static float readFloat(Map<String, String> tags, String string) {
		String data = tags.get(string);
		return data.equals("") ? 0f : Float.parseFloat(data);
	}

	private static int readInt(Map<String, String> tags, String string) {
		String data = tags.get(string);

		return data.equals("") ? 0 : Integer.parseInt(data);
	}

	private static Vector3f readVec3(Map<String, String> tags, String string) {
		String[] data = tags.get(string).split(",");
		return new Vector3f(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]));
	}
}