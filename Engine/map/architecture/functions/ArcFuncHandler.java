package map.architecture.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import dev.cmd.Console;
import util.StrUtils;

public class ArcFuncHandler {
	Map<Class<? extends ArcFunction>, ArrayList<ArcFunction>> functions;

	public ArcFuncHandler() {
		functions = new HashMap<>();
	}

	public void add(ArcFunction function) {
		Class<? extends ArcFunction> c = function.getClass();

		ArrayList<ArcFunction> funcList = functions.get(c);
		if (funcList == null) {
			funcList = new ArrayList<ArcFunction>();
			functions.put(c, funcList);
		}

		funcList.add(function);
	}

	public void callCommand(Vector3f callerPosition, String cmdRaw) {
		String cmd = parseInput(cmdRaw);
		String[] strs = StrUtils.parseCommand(cmd);

		if (strs == null || strs.length == 0) {
			return;
		}

		final String commandStr = strs[0].toUpperCase();
		ArcCommands command = null;
		
		for(ArcCommands arcCmd : ArcCommands.values()) {
			if (arcCmd.name().equals(commandStr)) {
				command = arcCmd;
				break;
			}
		}
		
		if (command == null) {	// If null, assume is console command
			Console.send(cmd);
			return;
		}
		
		Class<? extends ArcFunction> c = command.getArcFuncClass();

		List<ArcFunction> funcList = functions.get(c);
		if (funcList == null) {
			Console.log("#yWarning: Map command \"" + command.name() + "\" was called, but the map has no associated functions to use.");
			Console.log("\"" + cmd + "\"");
			return;
		}
		
		ArcFuncCallMethod callMethod = command.getPrefCallMethod();

		int index = -1;
		float range = -1;

		List<String> argArrList = new ArrayList<String>();
		if (strs.length > 1) {
			for (int i = 1; i < strs.length; i++) {
				String line = strs[i];
				if (line.startsWith("call=")) {
					callMethod = ArcFuncCallMethod.valueOf(line.substring(5));
				} else if (line.startsWith("index=")) {
					index = Integer.parseInt(line
							.replace("size", "" + funcList.size())
							.replace("last", "" + (funcList.size() - 1)).substring(6));
				} else if (line.startsWith("range=")) {
					range = Float.parseFloat(line.substring(6));
				} else if (line != null) {
					argArrList.add(line);
				}
			}
		}

		String[] args = new String[argArrList.size()];
		args = argArrList.toArray(args);
		
		switch(callMethod) {
		case BY_RANDOM:
			index = (int)(funcList.size() * Math.random());
			funcList.get(index).trigger(args);
			break;
		case BY_NEAREST:
			ArcFunction closestFunc = null;	
			range = Float.POSITIVE_INFINITY;
			for(ArcFunction func : funcList) {
				float newRange = Vector3f.distanceSquared(callerPosition, func.getPosition());
				if (newRange < range) {
					closestFunc = func;
					range = newRange;
				}
			}
			closestFunc.trigger(args);
			break;
		case BY_PROXIMITY:
			range = Float.POSITIVE_INFINITY;
			for(ArcFunction func : funcList) {
				float newRange = Vector3f.distanceSquared(callerPosition, func.getPosition());
				if (newRange < range) {
					func.trigger(args);
					range = newRange;
				}
			}
			break;
		case BY_RANDOM_PROXIMITY:
			List<ArcFunction> validFuncs = new ArrayList<>();
			float compRange = range * range;
			for(ArcFunction func : funcList) {
				float newRange = Vector3f.distanceSquared(callerPosition, func.getPosition());
				if (newRange < compRange) {
					validFuncs.add(func);
				}
			}
			if (validFuncs.size() > 0) {
				index = (int)(validFuncs.size() * Math.random());
				validFuncs.get(index).trigger(args);
			} else {
				Console.log("#yMap command " + command.name() + " called via RANDOM_PROXIMIY, range=" + range + " all functions were of range.");
				Console.log("\"" + cmd + "\"");
			}
			break;
		case BY_INDEX:
			funcList.get(Math.floorMod(index, funcList.size())).trigger(args);
			break;
		case ALL:
			for(ArcFunction func : funcList) {
				func.trigger(args);
			}
			break;
		default:	// BY_FIRST, UNSPECIFIED
			funcList.get(0).trigger(args);
		}
	}

	private String parseInput(String string) {
		return string
				.replaceAll("!random", "" + Math.random());
	}
}
