import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.MinecraftServer;
import java.util.ArrayList;
import java.io.*;
import java.util.*;

public class MCAUtils extends Plugin {
	
	static final Logger log = Logger.getLogger("Minecraft");
	private Server server = etc.getServer();
    //private Properties  props; 
    private int[] disalloweditems;

	private Timer SaveAllTicker;
	private long SaveAllTickInterval = 3600000;
	
	private ArrayList<PluginRegisteredListener> listeners = new ArrayList<PluginRegisteredListener>();
	
    public void enable() {
		log.info("[MCAUtils] Mod Enabled.");
		loadProperties();
		SaveAllTicker = new Timer();
		SaveAllTicker.schedule(new SaveAllTickerTask(), SaveAllTickInterval, SaveAllTickInterval);
    }

    public void disable() {
    	
		PluginLoader loader = etc.getLoader();
		for (PluginRegisteredListener rl : listeners)
			loader.removeListener(rl);
		listeners.clear();
    	
		log.info("[MCAUtils] Mod Disabled");
        if (SaveAllTicker != null) {
            SaveAllTicker.cancel();
        }
    }
    
    public void initialize() {
    	Listener l = new Listener();

        listeners.add(etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, l, this, PluginListener.Priority.LOW));
        listeners.add(etc.getLoader().addListener(PluginLoader.Hook.COMMAND, l, this, PluginListener.Priority.LOW));
        listeners.add(etc.getLoader().addListener(PluginLoader.Hook.SERVERCOMMAND, l, this, PluginListener.Priority.LOW));
    }
    
    private class Listener extends PluginListener {
    	
    	public boolean onConsoleCommand(java.lang.String[] split) {
    		log.info("hook called");
    		
    		if(split[0].equalsIgnoreCase("ping")) {
    			log.info("pong");
    			return true;
    		} else if(split[0].equalsIgnoreCase("pong")) {
    			log.info("ping");
    			return true;
    		}
    		return false;
    	}
    	
    	public boolean onCommand(Player player, java.lang.String[] split) {
    		
    		if(split[0].equalsIgnoreCase("/tpos") && player.canUseCommand("/tpos")) {
    			if(split.length==3) {
    				try {
    					player.teleportTo(Double.parseDouble(split[1]) + 0.5D,
    										server.getHighestBlockY(Integer.parseInt(split[1]),
    																Integer.parseInt(split[2])),
    										Double.parseDouble(split[2]) + 0.5D,
    										player.getRotation(),
    										player.getPitch());
    					player.sendMessage("Teleported");
    					return true;
    				} catch (NumberFormatException ex) {
    					
    				}
    			}
    			
    			player.sendMessage("Incorrect arguments");
    			return true;
    		} else if(split[0].equalsIgnoreCase("/ping")) {
    			player.sendMessage(Colors.Rose + "Pong!");
    			return true;
    		} else if(split[0].equalsIgnoreCase("/pong")) {
    			player.sendMessage(Colors.Rose + "Ping!");
    			return true;
    		}
    		
    		return false;
    	}
    	
    	public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand) {
    		//block item if disallowed
    		/*if(disalloweditems.length>0 && !player.canUseCommand("/useblockeditems")) {
				player.sendMessage("Reached");
				for (int i = 0; i<disalloweditems.length;i++) {
					if (itemInHand == disalloweditems[i]) {
						player.sendMessage("The use of this item has been blocked. Talk to an admin for more info");
						return true;
					}
				}
			}*/
    		//whitelist fire
			if(itemInHand==259 || itemInHand==51) {
				if(player.canUseCommand("/usefire")) {
					player.sendMessage("Be careful with that fire");
					return false;
				} else {
					player.sendMessage("You can't use fire. Ask an admin to use it for you");
					return true;
				}
			}
			//feather
			else if ( itemInHand==288 && player.canUseCommand("/allowfirefeather")){
				for ( int i = (blockClicked.getX()-8); i<= blockClicked.getX()+8; i++ ){
					for ( int j = blockClicked.getY(); j<= 128; j++ ){
						for ( int k = (blockClicked.getZ()-8); k<= (blockClicked.getZ()+8); k++ ){
							if( server.getBlockIdAt(i, j, k) == 51 ){
								server.setBlockAt(0,i,j,k);
							}
						}
					}
				}
				return true;
			}
			// String
			else if ( itemInHand==287 && player.canUseCommand("/allowlavastring")){
				for ( int i = (blockClicked.getX()-2); i<= blockClicked.getX()+2; i++ ){
					for ( int j = blockClicked.getY(); j<= 128; j++ ){
						for ( int k = (blockClicked.getZ()-2); k<= (blockClicked.getZ()+2); k++ ){
							if( server.getBlockIdAt(i, j, k) == 11 || server.getBlockIdAt(i, j, k) == 10){
								server.setBlockAt(0,i,j,k);
							}
						}
					}
				}
				return true;
			}
			return false;
		}
    }
    
	private void loadProperties(){
		PropertiesFile properties = new PropertiesFile("MCAUtilsPlugin.properties");
		try {
			SaveAllTickInterval = properties.getLong("saveallintervalinminutes", 60) * 60000;
			String[] stringdisalloweditems = properties.getString("disalloweditems", "19,66,328,342,343").split(",");
       		disalloweditems = new int[stringdisalloweditems.length];
       		for (int i=0;i<stringdisalloweditems.length;i++) {
       			try {
       				disalloweditems[i] = Integer.parseInt(stringdisalloweditems[i]);
       			} catch (NumberFormatException nfe) {
       				disalloweditems[i] = -2;
       			}
       		}
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception while reading from MCAUtilsPlugin.properties", e);
        }
        // TODO : non-existant file
	}
    
    private class SaveAllTickerTask extends TimerTask {
        public void run() {
        	log.info("MCAUtils is calling a save-all");
            server.useConsoleCommand("save-all");
        }
    }
}