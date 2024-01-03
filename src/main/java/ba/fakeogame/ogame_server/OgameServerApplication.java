package ba.fakeogame.ogame_server;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pak.Building;
import pak.BuildingIndex;
import pak.ConstructionResult;
import pak.Planet;
import pak.Player;
import pak.ResourceAmount;
import pak.ResourceType;
import pak.Ship;
import pak.ShipIndex;

@RestController
@SpringBootApplication
public class OgameServerApplication {


	public static void main(String[] args) {
		SpringApplication.run(OgameServerApplication.class, args);
	}
	
	@CrossOrigin
	@GetMapping("/register")
	public String register(String username, String password)
	{
		synchronized(UniverseProvider.mutex)
		{
			
			Player p = UniverseProvider.getUniverse().register(username, password);
			JSONObject json = new JSONObject();
			
			if(p != null)
			{
				json.put("success", true);
				System.out.println("Igrac " + p.getName() + " je registrovan!");
				UniverseProvider.save();
			}
			else
			{
				json.put("success", false);
				System.out.println("Registracija nije uspijesna! ");
			}
			
			return json.toString();
		
		}
		
	}
	
	@CrossOrigin
	@GetMapping("/login")
	public String login(String username, String password)
	{
		synchronized(UniverseProvider.mutex)
		{
			System.out.println("STIGAO LOGIN: " + username + " / " + password);
			
			Player p = UniverseProvider.getUniverse().login(username, password);
			
			JSONObject json = new JSONObject();
			
			if(p != null)
			{
				json.put("success", true);
				json.put("username", p.getName());
				json.put("ID", p.getId());
				json.put("token", p.getToken());
				
				JSONArray planetArray = new JSONArray();
				
				for(Planet planet : p.getOwnedPlanets())
				{
					planetArray.put(planet.getId());
				}
				
				
				json.put("ownedPlanets", planetArray);
				
				
				System.out.println(p.getName() + " " + p.getToken());
				System.out.println(username + " " + p.getOwnedPlanets().size());
				
				System.out.println("ODGOVOR:\n" + json);
				UniverseProvider.save();
			}
			else
			{
				json.put("success", false);
			}
			
			return json.toString();
		
		}
		
	}
	
	@CrossOrigin
	@GetMapping("/signOut")
	public String signOut(String token)
	{
		synchronized(UniverseProvider.mutex)
		{
			Player p = UniverseProvider.getUniverse().authenticate(token);
			
			JSONObject json = new JSONObject();
			
			if(p != null)
			{
				json.put("success", true);
				p.clearToken();
				System.out.println("Token je obrisan! Izlogovani igrac je: " + p.getName());
				UniverseProvider.save();
			}
			else
			{
				json.put("success", false);
			}
			
			return json.toString();
		}
	}
	
	
	@CrossOrigin
	@GetMapping("/resource")
	public String resource(String token, int idPlanet)
	{
		synchronized(UniverseProvider.mutex)
		{
			Player p = UniverseProvider.getUniverse().authenticate(token);
			Planet planet = UniverseProvider.getUniverse().getPlanet(idPlanet);
			
			JSONObject json = new JSONObject();
			
			if(p != null && planet != null && p == planet.getOwner())
			{
				ResourceAmount r = planet.getResource();
				
				json.put("success", true);
				json.put("iron", r.getSingleResource(ResourceType.IRON));
				json.put("crystal", r.getSingleResource(ResourceType.CRYSTAL));
				json.put("deuterium", r.getSingleResource(ResourceType.DEUTERIUM));
				json.put("energyProduction", planet.getEnergyProduction());
				json.put("energyConsumption", planet.getEnergyConsumption());
			}
			else
			{
				json.put("success", false);
			}
			
			return json.toString();
		}
		
	}
	
	@CrossOrigin
	@GetMapping("/planets")
	public String planets(String token)
	{
		synchronized(UniverseProvider.mutex)
		{
			Player p = UniverseProvider.getUniverse().authenticate(token);
			JSONObject json = new JSONObject();
			
			if(p != null)
			{
				JSONArray planetsArray = new JSONArray();
				
				json.put("success", true);
				
				for(Planet planets : p.getOwnedPlanets())
				{
					JSONObject planetJSON = new JSONObject();
					
					planetJSON.put("id",planets.getId());
					planetJSON.put("name", planets.getName());
					planetJSON.put("positionX", planets.getPositionX());
					planetJSON.put("positionY", planets.getPositionY());
					
					planetsArray.put(planetJSON);
				}
				json.put("ownedPlanets", planetsArray);
			}
			else
			{
				json.put("success", false);
			}
			
			return json.toString();
		}
	}
	
	@CrossOrigin
	@GetMapping("/updateBuilding")
	public String updateBuilding(String token, int idPlanet, String idBuilding)
	{
		synchronized(UniverseProvider.mutex)
		{
			Player p = UniverseProvider.getUniverse().authenticate(token);
			Planet planet = UniverseProvider.getUniverse().getPlanet(idPlanet);
			Building building = BuildingIndex.getInstance().getById(idBuilding);
			
			JSONObject json = new JSONObject();
			
			if(p != null && planet != null && p == planet.getOwner() && building != null)
			{
				
				ConstructionResult result = planet.constructBuilding(building);
				
				json.put("success", result == ConstructionResult.OK);
				json.put("name", building.getName());
				json.put("conTime", building.getConstractionTimer());
				json.put("status", result);
				json.put("lvl", planet.getBuildingLvl(building));
				
				System.out.println("Status gradnje zgrade je " + result);
				
				if(result == ConstructionResult.OK)
				{
					UniverseProvider.save();
				}
				
			}
			else
			{
				json.put("success", false);
				
				System.out.println("Zgrada neuspjesno unapredjena! ");
			}
			
			return json.toString();
		}
		
	}
	
	@CrossOrigin
	@GetMapping("/buildShip")
	public String updateShip(String token, int idPlanet, String idShip, int count)
	{
		synchronized(UniverseProvider.mutex)
		{
			Player p = UniverseProvider.getUniverse().authenticate(token);
			Planet planet = UniverseProvider.getUniverse().getPlanet(idPlanet);
			Ship ship = ShipIndex.getInstance().getById(idShip);
			
			
			JSONObject json = new JSONObject();
			
			if(p != null && planet != null && p == planet.getOwner() && ship != null && count > 0)
			{
				ConstructionResult result = planet.constructShip(ship, count);
				
				json.put("success", result == ConstructionResult.OK);
				json.put("name", ship.getName());
				json.put("count", count);
				json.put("conTime", ship.getConstructionTimer());
				json.put("totalConTime", ship.getConstructionTimer() * count);
				json.put("status", result);
				
				System.out.println("Status gradenje brodova je: " + result);
				
				if(result == ConstructionResult.OK)
				{
					UniverseProvider.save();
				}
			}
			else
			{
				json.put("success", false);
				
				System.out.println("Brodovi nisu poceli da se prave! ");
			}
			
			return json.toString();
		}
	}
	
	@CrossOrigin
	@GetMapping("/updateBuildingInfo")
	public String updateBuildingInfo(String token, int idPlanet)
	{
		synchronized(UniverseProvider.mutex)
		{
			Player p = UniverseProvider.getUniverse().authenticate(token);
			Planet planet = UniverseProvider.getUniverse().getPlanet(idPlanet);
	
			
			JSONObject json = new JSONObject();
			
			if(p != null && planet != null && p == planet.getOwner())
			{
				Building b = planet.getBuildingInConstruction();
				
				json.put("success", true);
				
				if(b != null)
				{
					json.put("isConstructingBuilding", true);
					json.put("id", b.getId());
					json.put("name", b.getName());
					json.put("buildingLvl", planet.getBuildingLvl(b));
					json.put("conTime", planet.getBuildingConstructionTimer());
				}
				else
				{
					json.put("isConstructingBuilding", false);
				}
			}
			else
			{
				json.put("successs", false);
				System.out.println("Greska u upitu. ");
			}
			
			return json.toString();
		}
		
	}
	
	@CrossOrigin
	@GetMapping("/buildShipInfo")
	public String buildShipInfo(String token, int idPlanet)
	{
		synchronized(UniverseProvider.mutex)
		{
			Player p = UniverseProvider.getUniverse().authenticate(token);
			Planet planet = UniverseProvider.getUniverse().getPlanet(idPlanet);
			
			JSONObject json = new JSONObject();
			
			if(p != null && planet != null && p == planet.getOwner())
			{
				Ship s = planet.getShipInConstruction();
				
				json.put("success", true);
				
				if(s != null)
				{
					json.put("isConstructionShip", true);
					json.put("id", s.getId());
					json.put("name", s.getName());
					json.put("countShips", planet.getConShipCount());
					json.put("conTime", planet.getShipConstructionTimer());
					json.put("fullConTime", planet.getfullConShipTimer());
				}
				else
				{
					json.put("isConstructionShip", false);
				}
			}
			else
			{
				json.put("success", false);
				System.out.println("Greska u upitu. ");
			}
			
			return json.toString();
		}
		
		
	}
	
	
	/*Rijesiti resurse u endpointu resource da ne spominjem nazive resorsa po imenu,
	  napraviti da bude fleksibilno za dalje tok igre ako dodje do promjena.*/

}
