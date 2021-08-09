package map.architecture.read;

import java.io.DataInputStream;
import java.io.IOException;

import io.FileUtils;
import map.architecture.components.ArcObjects;
import map.architecture.components.ArcStaticObject;
import map.architecture.vis.Bsp;

public class ArcLoadObjects {
	
	static void readObjects(Bsp bsp, DataInputStream in) throws IOException {
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
	}
}
