package map.architecture.read;

import java.io.IOException;
import java.util.List;

import io.FileUtils;
import map.architecture.components.ArcObjects;
import map.architecture.components.ArcStaticObject;
import map.architecture.vis.Bsp;
import util.CounterInputStream;

public class ArcLoadObjects {
	
	static void readObjects(Bsp bsp, List<ArcStaticObject> objects, CounterInputStream in) throws IOException {
		String[] objModelReference = new String[in.readShort()];
		for(int i = 0; i < objModelReference.length; i++) {
			objModelReference[i] = FileUtils.readString(in);
		}
		
		short[] objLeafResidence = new short[in.readShort()];
		for(int i = 0; i < objLeafResidence.length; i++) {
			objLeafResidence[i] = in.readShort();
		}
		
		//objects = new ArcStaticObject[in.readShort()];
		int numObjects = in.readShort();
		for(int i = 0; i < numObjects; i++) {
			ArcStaticObject obj = new ArcStaticObject();
			obj.pos = FileUtils.readVec3(in);
			obj.rot = FileUtils.readVec3(in);
			obj.lightingPos = FileUtils.readVec3(in);
			obj.objLeafResIndex = in.readShort();
			obj.numObjLeafRes = in.readByte();
			in.readByte();		// "skin" but is unused for now
			obj.model = in.readShort();
			obj.solidity = in.readByte();
			obj.visRange = in.readFloat();
			
			objects.add(obj);
		}
		
		bsp.objects = new ArcObjects(bsp.leaves, objModelReference, objLeafResidence, objects);
	}
}
