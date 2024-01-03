package ba.fakeogame.ogame_server;

import java.io.File;

import pak.Planet;
import pak.Universe;

public class UniverseProvider
{
	private static Universe universe = null;
	public static Object mutex = new Object();
	
	public static Universe getUniverse()
	{
		if(universe == null)
		{
			File universeFile = new File("Universe.json");
			
			if(universeFile.exists() && !universeFile.isDirectory())
			{
				System.out.println("Ucitavamo univerzum iz fajla.");
				universe = new Universe("Universe.json");
			}
			else
			{
				System.out.println("Nema fajla, pravimo novi univerzum.");
				
				universe = new Universe(100, 100, 100);
				universe.register("Igor", "123456");
				universe.register("Aco", "654321");
				Planet planet1 = universe.getPlanet(25);
				universe.getPlayer(1).takePlanet(planet1);
				
				universe.saveToFile("Universe.json");
			}
		}
		
		universe.update();
		
		return universe;
	}
	
	public static void save()
	{
		getUniverse().saveToFile("Universe.json");
	}
}
