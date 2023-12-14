package ba.fakeogame.ogame_server;

import pak.Universe;

public class UniverseProvider
{
	private static Universe universe = null;
	
	public static Universe getUniverse()
	{
		if(universe == null)
		{
			universe = new Universe(100, 100, 100);
			universe.register("Igor", "123456");
			universe.register("Aco", "654321");
		}
		
		universe.update();
		
		return universe;
	}
}
