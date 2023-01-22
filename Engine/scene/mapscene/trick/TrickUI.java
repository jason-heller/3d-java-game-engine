package scene.mapscene.trick;

import gl.Window;
import ui.UI;

public class TrickUI {
	private String trickString = "", trickSubstring = "";
	private String achievementString = "";

	private float displayTimer = 0f, achievementTimer = 0f;
	
	public void update() {
		if (displayTimer > 0f) {
			displayTimer = Math.max(displayTimer - Window.deltaTime, 0f);
			
			UI.drawString(trickString , 640, 600, .3f, true);
			UI.drawString(trickSubstring , 640, 640, .2f, true);
		} else {
			trickString = "";
		}
		
		if (achievementTimer > 0f) {
			achievementTimer = Math.max(achievementTimer - Window.deltaTime, 0f);
			
			UI.drawString(achievementString, 1000, 300, .3f, false);
		} else {
			achievementString = "";
		}
	}

	public void drawBufferedTrick(Trick trick) {
		UI.drawString(trick.getName(), 1000, 640, .4f, false);
	}

	public void setTimer(float time) {
		this.displayTimer = time;
	}
	
	public void addToTrickString(Trick trick, int trickPoints, int comboScore, float multiplier) {
		String color = "";

		if (trickPoints != trick.getPoints())
			color = (trickPoints == 0) ? "#r" : "#y";
		
		if (trickString.isEmpty()) {
			trickString = color + trick.getName();
		} else {
			trickString += " #wto " + color + trick.getName();
		}
		
		color = multiplier > 3.0f ? "#g" : "#w";
		
		trickSubstring = (comboScore) + " X " + color + (multiplier);
		achievementString = "";
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
	
}
