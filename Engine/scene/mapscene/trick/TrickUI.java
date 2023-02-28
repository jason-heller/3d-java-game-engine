package scene.mapscene.trick;

import gl.Window;
import scene.entity.util.PlayerEntity;
import ui.Font;
import ui.UI;

public class TrickUI {
	private String trickString = "", trickSubstring = "";
	private String achievementString = "";

	private float displayTimer = 0f, achievementTimer = 0f;
	
	public void update() {
		if (displayTimer > 0f) {
			displayTimer = Math.max(displayTimer - Window.deltaTime, 0f);
			
			UI.drawString(Font.vhsFont, trickString , 640, 600, .3f, 800, true);
			UI.drawString(Font.vhsFont, trickSubstring , 640, 640, .2f, Integer.MAX_VALUE, true);
		} else {
			clearTrickString();
		}
		
		if (achievementTimer > 0f) {
			achievementTimer = Math.max(achievementTimer - Window.deltaTime, 0f);
			
			UI.drawString(Font.vhsFont, achievementString, 1000, 300, .3f, Integer.MAX_VALUE, false);
		} else {
			achievementString = "";
		}
	}

	public void drawBufferedTrick(Trick trick) {
		UI.drawString(Font.vhsFont, trick.getName(), 1000, 640, .4f, Integer.MAX_VALUE, false);
	}

	public void setTimer(float time) {
		this.displayTimer = time;
	}
	
	public void addToTrickString(PlayerEntity player, Trick trick, int trickPoints, int comboScore, float multiplier) {
		String color = "";
		String trickName;
		

		boolean isFrontside = (trick.getType() == TrickType.GRIND_TRICK) ? player.getFrontside() : false;
		boolean isSwitch = player.isSwitch();

		if (trick.getType() == TrickType.GRIND_TRICK)
			trickName = ((isFrontside) ? "fs " : "bs ") + trick.getName();
		else
			trickName = ((isSwitch) ? "switch " : "") + trick.getName();

		if (trickPoints != trick.getPoints())
			color = (trickPoints == 0) ? "#r" : "#y";
		
		if (trickString.isEmpty()) {
			trickString = color + trickName;
		} else {
			trickString += " #wto " + color + trickName;
		}
		
		updateTrickSubtring(comboScore, multiplier);
		achievementString = "";
	}
	
	public void addToTrickString(String name, int trickPoints, int comboScore, float multiplier) {
		if (trickString.isEmpty()) {
			trickString = name;
		} else {
			trickString += " #wto " + name;
		}
		
		updateTrickSubtring(comboScore, multiplier);
		this.displayTimer = 3f;
	}
	
	public void updateTrickSubtring(int comboScore, float multiplier) {
		String color = multiplier > 3.0f ? "#g" : "#w";
		
		trickSubstring = (comboScore) + " X " + color + (multiplier);
	}

	public void clearTrickString() {
		trickString = "";
	}

	public void setTrickSubstring(String str) {
		this.trickSubstring = str;
	}

	public void addAchievement(String str) {
		achievementString += str;
		achievementTimer = 3f;
	}

	public String getTrickString() {
		return this.trickString;
	}
	
}
