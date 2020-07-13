package me.samuel81.jin.module.bt;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;
import me.samuel81.jin.module.Pos;

@Getter
public class BTPos extends Pos implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5857525238608764131L;

	private TreeMap<String, PI> pos = new TreeMap<>();

	private String kelurahan, hak;

	private String latestCheck = "";

	private boolean needToResume = false;

	private int totalOnDB = 0;

	public BTPos(String kelurahan, String hak) {
		this.kelurahan = kelurahan;
		this.hak = hak;
	}

	public boolean isNeedToResume() {
		return needToResume;
	}

	public void setResume(boolean b) {
		this.needToResume = b;
	}

	public void setTotalOnDB(int i) {
		this.totalOnDB = i;
	}

	public Map<String, PI> getPos() {
		return pos;
	}

	public PI getPos(String noHak) {
		try {
			return pos.get(noHak);
		} catch (NullPointerException e) {
			return null;
		}
	}

	public void putPoint(String noHak, int page, int index) {
		pos.put(noHak, new PI(page, index));
		latestCheck = noHak;
	}

	public class PI implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8862703587584315883L;
		@Getter
		int x, y;

		public PI(int x, int y) {
			this.x = x;
			this.y = y;
		}

	}

}
