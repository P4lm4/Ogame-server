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
	
	@GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name,
    					@RequestParam(value = "surname", defaultValue = "World") String surname) {
		JSONObject json = new JSONObject();
		
		Ship ship = ShipIndex.getInstance().getById("scout");
		json.put("name", ship.getName());
		json.put("health", ship.getHealth());
		json.put("speed", ship.getSpeed());
		return json.toString();
	
    }
	
	@CrossOrigin(origins = "http://127.0.0.1:5500")
	@GetMapping("/register")
	public String register(String username, String password)
	{
		Player p = UniverseProvider.getUniverse().register(username, password);
		JSONObject json = new JSONObject();
		
		if(p != null)
		{
			json.put("success", true);
			System.out.println("Igrac " + p.getName() + " je registrovan!");
		}
		else
		{
			json.put("success", false);
			System.out.println("Registracija nije uspijesna! ");
		}
		
		return json.toString();
		
	}
	
	@CrossOrigin(origins = "http://127.0.0.1:5500")
	@GetMapping("/login")
	public String login(String username, String password)
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
			JSONArray buildingsArray = new JSONArray();
			
			for(Planet planet : p.getOwnedPlanets())
			{
				planetArray.put(planet.getId());
				
				for(Buidling b : )
			}
			json.put("ownedPlanets", planetArray);
			
			
			
			
			
			System.out.println(p.getName() + " " + p.getToken());
			System.out.println(username + " " + p.getOwnedPlanets().size());
			
			
		}
		else
		{
			json.put("success", false);
		}
		
		return json.toString();
		
	}
	
	@CrossOrigin(origins = "http://127.0.0.1:5500")
	@GetMapping("/signOut")
	public String signOut(String token)
	{
		Player p = UniverseProvider.getUniverse().authenticate(token);
		
		JSONObject json = new JSONObject();
		
		if(p != null)
		{
			json.put("success", true);
			p.clearToken();
			System.out.println("Token je obrisan! Izlogovani igrac je: " + p.getName());
		}
		else
		{
			json.put("success", false);
		}
		
		return json.toString();
	}
	
	
	@CrossOrigin(origins = "http://127.0.0.1:5500")
	@GetMapping("/resource")
	public String resource(String token, int idPlanet)
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
	
	@CrossOrigin(origins = "http://127.0.0.1:5500")
	@GetMapping("/planets")
	public String planets(String token)
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
	
	@CrossOrigin(origins = "http://127.0.0.1:5500")
	@GetMapping("/updateBuilding")
	public String updateBuilding(String token, int idPlanet, String idBuilding)
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
			
			System.out.println("Status gradnje je " + result);
			
		}
		else
		{
			json.put("success", false);
			
			System.out.println("Zgrada neuspjesno unapredjena! ");
		}
		
		return json.toString();
		
	}
	
	/*Rijesiti resurse u endpointu resource da ne spominjem nazive resorsa po imenu,
	  napraviti da bude fleksibilno za dalje tok igre ako dodje do promjena.*/

}
