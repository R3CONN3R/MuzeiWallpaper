package de.jamoo.muzei;

import java.io.Serializable;
import java.util.ArrayList;

public class NodeCategory implements Serializable {
	private static final long serialVersionUID = 1L;

	public String name;
	public ArrayList<NodeWallpaper> wallpaperList;
}
