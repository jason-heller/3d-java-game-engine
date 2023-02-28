package map.architecture.read;

import java.io.IOException;

import org.joml.Vector3f;

import dev.cmd.Console;
import geom.AABB;
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
		
		int numClips = in.readShort();
		ArcClip[] clips = new ArcClip[numClips];
		
		for (int i = 0; i < clips.length; i++) {
			
			final int clipId = in.readByte() & 0xFF;
			final int numPlanes = in.readByte() & 0xFF;
			
			final ClipType clipType = ClipType.values()[clipId];
			int[] planes = new int[numPlanes];
			
			for(int j = 0; j < numPlanes; j++)
				planes[j] = in.readInt();
			
			ArcClip clip = new ArcClip();
			clip.planes = planes;
			clip.id = clipType;
			clip.center = new Vector3f(in.readFloat(), in.readFloat(), in.readFloat());
			clip.halfSize = new Vector3f(in.readFloat(), in.readFloat(), in.readFloat());
			clips[i] = clip;
		}
		
		bsp.clips = clips;
	}
}
