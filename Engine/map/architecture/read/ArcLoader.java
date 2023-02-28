package map.architecture.read;

import static map.architecture.read.ArcLoadBSP.readBspTree;
import static map.architecture.read.ArcLoadClips.readClips;
import static map.architecture.read.ArcLoadEntities.readEntities;
import static map.architecture.read.ArcLoadGeometry.readGeometry;
import static map.architecture.read.ArcLoadHeightmaps.readHeightmaps;
import static map.architecture.read.ArcLoadLighting.readLighting;
import static map.architecture.read.ArcLoadNavMesh.readNavMesh;
import static map.architecture.read.ArcLoadObjects.readObjects;
import static map.architecture.read.ArcLoadOverlays.readOverlays;
import static map.architecture.read.ArcLoadTextures.readTextureInfo;
import static map.architecture.read.ArcLoadTextures.readTextureList;
import static map.architecture.read.ArcLoadVis.readVis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import core.App;
import dev.cmd.Console;
import gl.Window;
import io.FileUtils;
import map.architecture.Architecture;
import map.architecture.LumpIdentifier;
import map.architecture.components.ArcNavigation;
import map.architecture.components.ArcStaticObject;
import map.architecture.components.ArcTextureData;
import map.architecture.vis.Bsp;
import map.architecture.vis.Pvs;
import scene.Scene;
import scene.entity.EntityHandler;
import ui.UI;
import util.Colors;
import util.CounterInputStream;
import util.Vectors;

public class ArcLoader {

	private static final byte EXPECTED_GAME_ID = 1;
	private static final byte EXPECTED_FILE_VERSION = 2;
	
	public static boolean load(Scene scene, String mapFileName, Architecture arc) {
		CounterInputStream in = null;
		String fileName = "maps/" + mapFileName + ".arc";
		
		Bsp bsp = new Bsp();
		Pvs pvs = new Pvs();
		ArcNavigation nav = new ArcNavigation();
		
		arc.bsp = bsp;
		List<ArcStaticObject> objects = new ArrayList<>();
		
		EntityHandler.link(arc);

		ArcTextureData texData = new ArcTextureData(arc);
		
		try {
			in = new CounterInputStream(new FileInputStream(fileName));

			String identifier = new String(new byte[] { in.readByte(), in.readByte(), in.readByte() });
			if (!identifier.equals("ARC")) {
				Console.severe("Tried to load an " + identifier + " file as an ARC file");
				return false;
			}

			byte versionId = in.readByte();
			byte gameId = in.readByte();
			
			if (versionId != EXPECTED_FILE_VERSION) {
				Console.severe("ARC file is version " + versionId + ", expected version " + EXPECTED_FILE_VERSION);
				return false;
			}

			if (gameId != EXPECTED_GAME_ID) {
				Console.severe("ARC file is not formatted for this game (game id is " + gameId + ")");
				return false;
			}

			byte mapVer = in.readByte();
			String mapName = FileUtils.readString(in);
			boolean hasBakedLighting = (in.readByte() != 0);
			
			final int total = 13;
			
			while(true) {
				int lumpId = in.readShort();
				
				if (lumpId == -1)
					break;
				
				LumpIdentifier id = LumpIdentifier.values()[lumpId];
				int lumpSize = in.readInt();
				in.clearPosition();
				
				switch(id) {
				case GEOMETRY:
					loadInfo("Loading level geometry", total, 1);
					readGeometry(bsp, Vectors.ZERO, in);
					break;
				
				case CLIPS:
					loadInfo("Loading clips", total, 2);
					readClips(arc, in);
					break;
					
				case BSP:
					loadInfo("Loading BSP", total, 4);
					readBspTree(bsp, in);
					break;
					
				case ENTITIES:
					loadInfo("Loading entities", total, 5);
					readEntities(arc, in);
					break;
					
				case OBJECTS:
					loadInfo("Loading objects", total, 6);
					readObjects(bsp, objects, in);
					break;
					
				case VISIBILITY:
					loadInfo("Loading leaves/clusters", total, 7);
					readVis(pvs, in);
					break;
					
				case NAVIGATION:
					loadInfo("Loading navigation mesh", total, 8);
					readNavMesh(nav, in);
					break;
					
				case TEXTURES:
					loadInfo("Loading texture data", total, 9);
					readTextureInfo(bsp, texData, in, hasBakedLighting);
					readTextureList(texData, in);
					break;
					
				case HEIGHTMAP:
					loadInfo("Loading heightmap data", total, 10);
					readHeightmaps(bsp, in);
					break;
					
				case OVERLAYS:
					loadInfo("Loading overlay data", total, 11);
					readOverlays(bsp, texData, in);
					break;
					
				case LIGHTMAP:
					loadInfo("Loading light data", total, 12);
					readLighting(arc, in, hasBakedLighting);
					break;
				}
				
				if (in.getPosition() != lumpSize) {
					
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Console.severe("Map failed to load, lump size mismatch (" +  in.getPosition() + " of " + lumpSize + ")");
					Console.severe("Offending lump: " + id.name());
					return false;
				}
			}
			
			// loadInfo("Loading rooms", total, 3);
			// readRooms(bsp, in);
			
			loadInfo("Finishing up", total, 13);
			
			arc.setProperties(mapName, mapVer, gameId);

			arc.pvs = pvs;
			arc.setNavigation(nav);
			arc.setTextureData(texData);
			
			texData.setSkybox();
			
			// Texture data
			String[] textureNames = texData.getTextureNames();
			
			for(int i = 0; i < bsp.clusters.length; ++i) {
				bsp.clusters[i].buildModel(bsp.planes, bsp.edges, bsp.surfEdges, bsp.vertices, bsp.faces, bsp.leafFaceIndices, bsp.getTextureMappings(), textureNames);
			}
			
			for(int i = 0; i < bsp.heightmaps.length; i++) {
				bsp.heightmaps[i].buildModel(bsp.heightmapVerts, bsp.faces, bsp.planes, bsp.edges, bsp.surfEdges, bsp.vertices, bsp.getTextureMappings(), textureNames);
			}
			
			Console.log("Map loaded: " + mapName + " version=" + mapVer);

		} catch (FileNotFoundException e) {
			Console.severe("Tried to load " + mapFileName + ", failed");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}

	private static void loadInfo(String string, int total, int complete) {
		int cx = (int)UI.width / 2;
		int cy = (int)UI.height / 2;
		UI.drawRect(0, 0, Window.getWidth(), Window.getHeight(), Colors.BLACK).setDepth(Integer.MIN_VALUE + 1);
		UI.drawString("Loading", cx, cy - 48, true).setDepth(Integer.MIN_VALUE);
		
		float percent = (complete / (float)total);
		int width = (int) (UI.width * percent);
		UI.drawRect(cx - (width / 2), cy, width, 3, Colors.WHITE).setDepth(Integer.MIN_VALUE);
		
		String loadStatus = "#S" + string + " (" + complete + " / " + total +") " + (percent*100f) + "%";
		UI.drawString(loadStatus, cx, cy + 128, .2f, true).setDepth(Integer.MIN_VALUE);
		
		UI.render(App.scene);
		UI.update();
		Window.refresh();
	}
}