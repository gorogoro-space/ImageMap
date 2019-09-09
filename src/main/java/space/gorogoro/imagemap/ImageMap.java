package space.gorogoro.imagemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.imageio.ImageIO;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * ImageMap
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2019
 * @author     kubotan
 * @see        <a href="https://blog.gorogoro.space">Kubotan's blog.</a>
 */
public class ImageMap extends JavaPlugin {

  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable(){
    try{
      getLogger().info("The Plugin Has Been Enabled!");
      // If there is no setting file, it is created
      if(!getDataFolder().exists()){
        getDataFolder().mkdir();
      }
      File configFile = new File(getDataFolder() + "/config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }
    } catch (Exception e){
      logStackTrace(e);
    }
  }
 
  /**
   * JavaPlugin method onCommand.
   * @return true:Success false:Display the usage dialog set in plugin.yml
   */
  @Override
  public boolean onCommand( CommandSender sender, Command commandInfo, String label, String[] args) {
    try{
      if(!commandInfo.getName().equals("imagemap")) {
        return false;
      }

      if(args.length == 1) {
        return false;
      }

      if (!(sender instanceof Player)) {
        return false;
      }

      Player p = (Player) sender;
      
      int emptySlot = p.getInventory().firstEmpty();
      if (emptySlot == -1) {
        return false;
      }

      MapView view=getServer().createMap(p.getWorld());
      view.setCenterX(0);
      view.setCenterZ(0);
      view.setScale(MapView.Scale.CLOSEST);
      // TODO 鯖再起動やreloadやchunkアンロードでレンダーが外れてしまったら、MapInitializeEventで付け直す処理をする。その場合はMapRendererの無名クラスをちゃんと有名クラスにして扱う
      view.addRenderer(new MapRenderer() {
    	@Override
    	public void render(MapView view, MapCanvas canvas, Player player) {
    	  try {
            // TODO UUIDでフォルダを切りたい。
    	    BufferedImage image = ImageIO.read(new File(getDataFolder() + "/image/map_777.png"));
    	    canvas.drawImage(0, 0, image);
    	    image.flush();
    	  } catch(Exception e) {
    	    logStackTrace(e);
    	  }        
    	}
      });
      
      ItemStack map = new ItemStack(Material.MAP,view.getId());
      p.getInventory().setItem(emptySlot, map);

      return true;
    } catch (Exception e){
        logStackTrace(e);
    }
	return false;
  }
  
  /**
   * logStackTrace
   */
  private void logStackTrace(Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.flush();
      getLogger().warning(sw.toString());
  }
  
  /**
   * JavaPlugin method onDisable.
   */
  @Override
  public void onDisable(){
    try{
      getLogger().info("The Plugin Has Been Disabled!");
    } catch (Exception e){
      logStackTrace(e);
    }
  }
}
