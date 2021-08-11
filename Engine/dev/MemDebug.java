package dev;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joml.Vector3f;

import ui.UI;
import util.Colors;

public class MemDebug {
	
	public static long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}
	
	public static long getFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}
	
	public static long getMaxMemory() {
		return Runtime.getRuntime().maxMemory();
	}
	
	public static double getPercentMemoryUsed() {
		final long TOTAL_MEM = getTotalMemory();
	    final long FREE_MEM = getFreeMemory();
	    final long USED_MEM = TOTAL_MEM - FREE_MEM;
		return ((USED_MEM * 1.0) / TOTAL_MEM) * 100.0;
	}
	
	private static long inMegabytes(long memory) {
		return memory / (8 * 1024);
	}
	
	public static String memoryInfo() {
	    final long TOTAL_MEM_MB = inMegabytes(getTotalMemory());
	    final long FREE_MEM_MB = inMegabytes(getFreeMemory());
	    final long USED_MEM_MB = TOTAL_MEM_MB - FREE_MEM_MB;
	    String memPercent = String.format("%.1f", getPercentMemoryUsed());
	    String memUsed = String.format("%-6d", USED_MEM_MB);
		String memTotal = String.format("%-6d", TOTAL_MEM_MB);
		String memFree = String.format("%6d", FREE_MEM_MB);
		return memPercent + "% Memory used\n" + memUsed + " / " + memTotal + "mb\n" + memFree + " mb free";
	}
	
	public static void visualizeInfo() {
		int prctUsed = (int)getPercentMemoryUsed();
		
		Vector3f color = Colors.RED;
		if (prctUsed < 25) {
			color = Colors.GREEN;
		} else if (prctUsed < 50) {
			color = Colors.YELLOW;
		} else if (prctUsed < 75) {
			color = Colors.ORANGE;
		}
		
		UI.drawRect(15, 300, 102, 13, Colors.BLACK);
		UI.drawRect(16, 301, prctUsed, 12, color);
	}

	public static void dump_heap() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String fileName = "heap_" + dateFormat.format(date) + ".hprof";
        boolean live = true;

        HeapDumper.dumpHeap(fileName, live);
        Console.log("Heap dumped to: " + fileName);
	}
}
