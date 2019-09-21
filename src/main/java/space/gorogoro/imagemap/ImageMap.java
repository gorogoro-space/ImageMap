package space.gorogoro.imagemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public class ImageMap extends JavaPlugin implements Listener {

  static final String DS = "/";
  static final String IMAGE_DIR = "images";
  static final String IMAGE_PREFIX = "map";

  @Override
  public void onEnable(){
    try{
      getLogger().info("The Plugin Has Been Enabled!");

      getServer().getPluginManager().registerEvents(this, this);

      File imageDir = new File(getDataFolder() + DS + IMAGE_DIR);
      if(!imageDir.exists()){
        imageDir.mkdirs();
      }

      File configFile = new File(getDataFolder() + DS + "config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }

    } catch (Exception e){
      logStackTrace(e);
    }
  }

  // @return true:Success false:Display the usage dialog set in plugin.yml
  @Override
  public boolean onCommand( CommandSender sender, Command commandInfo, String label, String[] args) {
    try{
      if(!commandInfo.getName().equals("imagemap")) {
        return false;
      }

      if(args.length != 1) {
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

      String tempFileName = "downloading_" + p.getName() + "_" + FilenameUtils.getBaseName(args[0]) + ".png";
      File tempFile = new File(getDataFolder() + DS + IMAGE_DIR + DS + tempFileName);
      if(!downloadImageToPng(args[0], tempFile) || !tempFile.exists()) {
        return false;
      }

      MapView view=getServer().createMap(p.getWorld());
      Integer mapId = view.getId();
      Path savePath = Paths.get(getDataFolder() + DS + IMAGE_DIR + DS + IMAGE_PREFIX + mapId + ".png");
      Files.move(tempFile.toPath(), savePath);
      view.setCenterX(0);
      view.setCenterZ(0);
      view.setScale(MapView.Scale.CLOSEST);
      view.addRenderer(new ImageRenderer());
      ItemStack map = new ItemStack(Material.MAP, 1);
      MapMeta meta = (MapMeta)map.getItemMeta();
      meta.setMapView(view);
      map.setItemMeta((ItemMeta) meta);
      p.getInventory().setItem(emptySlot, map);

      return true;
    } catch (Exception e){
      logStackTrace(e);
    }
    return false;
  }

  @Override
  public void onDisable(){
    try{
      getLogger().info("The Plugin Has Been Disabled!");
    } catch (Exception e){
      logStackTrace(e);
    }
  }

  @EventHandler
  public void onMapInitializeEvent(MapInitializeEvent e){
    File imageFile = new File(getDataFolder() + DS + IMAGE_DIR + DS + IMAGE_PREFIX + e.getMap().getId() + ".png");
    if(imageFile.exists()) {
      e.getMap().removeRenderer(e.getMap().getRenderers().get(0));
      e.getMap().addRenderer(new ImageRenderer());
    }
  }

  private void logStackTrace(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    pw.flush();
    getLogger().warning(sw.toString());
  }

  private Boolean downloadImageToPng(String imageId, File destFile) {
    try {
      String tempFileName = destFile.getPath() + ".converting";
      File tempFile = new File(tempFileName);
      URL url = new URL(getConfig().getString("base-url") + imageId + ".png");
      ReadableByteChannel ch = Channels.newChannel(url.openStream());
      FileOutputStream outStream = new FileOutputStream(tempFile);
      outStream.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
      ch.close();
      outStream.close();
      BufferedImage bufimg = ImageIO.read(tempFile);
      ImageIO.write(bufimg, "png", destFile);
      if(tempFile.exists()) {
        tempFile.delete();
      }
      return true;
    } catch (Exception e){
      logStackTrace(e);
    }
    return false;
  }

  private class ImageRenderer extends MapRenderer {

    BufferedImage image;

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
      try {
        if(image != null) {
           return;
        }

        image = ImageIO.read(
          new File(getDataFolder() + DS + IMAGE_DIR + DS + IMAGE_PREFIX + view.getId() + ".png")
        );
        canvas.drawImage(0, 0, image);
      } catch(Exception e) {
        logStackTrace(e);
      }
    }
  }
}
