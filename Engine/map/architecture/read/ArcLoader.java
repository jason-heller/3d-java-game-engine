package map.architecture.read;

import static map.architecture.read.ArcLoadBSP.readBspTree;
import static map.architecture.read.ArcLoadClips.readClips;
import static map.architecture.read.ArcLoadEntities.readEntities;
import static map.architecture.read.ArcLoadGeometry.readGeometry;
import static map.architecture.read.ArcLoadHeightmaps.readHeightmaps;
import static map.architecture.read.ArcLoadLighting.readLighting;
import static map.architecture.read.ArcLoadNavMesh.readNavMesh;
import static map.architecture.read.ArcLoadObjects.readObjects;
import static map.architecture.read.ArcLoadRooms.readRooms;
import static map.architecture.read.ArcLoadTextures.readTextureInfo;
import static map.architecture.read.ArcLoadTextures.readTextureList;
import static map.architecture.read.ArcLoadVis.readVis;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.joml.Vector3f;

import core.Resources;
import dev.Console;
import io.FileUtils;
import map.architecture.Architecture;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcPackedAssets;
import map.architecture.vis.Bsp;
import map.architecture.vis.Pvs;
import scene.Scene;
import scene.entity.EntityHandler;

public class ArcLoader {

	private static final byte EXPECTED_GAME_ID = 3;
	private static final byte EXPECTED_FILE_VERSION = 4;
	
	public static Architecture load(Scene scene, String mapFileName, Vector3f offset, boolean doRender) {
		DataInputStream in = null;
		String fileName = "maps/" + mapFileName + ".arc";

		Architecture arc = new Architecture(scene);
		Bsp bsp = new Bsp();
		Pvs pvs = new Pvs();
		ArcNavigation nav = new ArcNavigation();
		Vector3f sunVector = new Vector3f(.5f, -.5f, 0);
		
		arc.bsp = bsp;
		
		EntityHandler.link(arc);

		ArcPackedAssets packedAssets = new ArcPackedAssets(arc);
		arc.setPackedAssets(packedAssets);
		
		boolean successfulLoad = false;

		try {
			in = new DataInputStream(new FileInputStream(fileName));

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
			boolean hasBakedLighting = (in.readByte() != 0);
			
			readGeometry(bsp, offset, in);
			readClips(arc, in);
			readRooms(bsp, in);
			readBspTree(bsp, in);
			readEntities(arc, in);
			readObjects(bsp, in);
			readVis(pvs, in);
			readNavMesh(nav, in);
			readTextureInfo(packedAssets, in, hasBakedLighting);
			readTextureList(packedAssets, in);
			readHeightmaps(bsp, in);
			readLighting(arc, in, hasBakedLighting);

			arc.setProperties(mapName, mapVer, gameId);
			arc.setSunVector(sunVector);		// TODO: Deprecated, remove this from file

			arc.pvs = pvs;
			arc.faces = bsp.faces;		// Redundant
			arc.setNavigation(nav);
			
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

				String assetLocation = "maps/" + mapFileName + "/";
				String[] modelFiles = bsp.objects.getModelReference();
				for(String modelFile : modelFiles) {
					String path = assetLocation + modelFile;
					Resources.addModel(modelFile, path);
				}
			}
		}
	}
}