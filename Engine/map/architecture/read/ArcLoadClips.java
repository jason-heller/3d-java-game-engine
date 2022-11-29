package map.architecture.read;

import java.io.IOException;

import org.joml.Vector3f;

import geom.AxisAlignedBBox;
import io.FileUtils;
import map.architecture.Architecture;
import map.architecture.components.ArcClip;
import map.architecture.components.ArcTriggerClip;
import map.architecture.components.ClipType;
import map.architecture.vis.Bsp;
import util.CounterInputStream;

public class ArcLoadClips {
	static void readClips(Architecture arc, CounterInputStream in) throws IOException {
		Bsp bsp = arc.bsp;
		
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
	}
}
