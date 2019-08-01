package com.scorch.core.modules.staff;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.players.ScorchPlayer;
import com.scorch.core.utils.MSG;
import com.scorch.core.utils.QRMap;
import com.scorch.core.utils.TOTP;

public class AuthenticationModule extends AbstractModule implements Listener {
	public AuthenticationModule(String id) {
		super(id);
	}

	private long expire;

	@Override
	public void initialize() {
		expire = ScorchCore.getInstance().getConfig().getLong("TFAExpiration");
		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());

		for (Player player : Bukkit.getOnlinePlayers()) {

			if (!player.hasPermission("scorch.2fa"))
				continue;

			ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
			if (!sp.hasData("lastAuthentication")) {
				setupAuthentication(player);
				continue;
			}
			if (sp.hasData("lastAuthentication") // 1 Day
					&& System.currentTimeMillis()
							- sp.getData("lastAuthentication", Number.class).longValue() < expire) {
				String msg = ScorchCore.getInstance().getMessage("authenticatetimeleft");
				msg = msg
						.replace("%time%",
								MSG.getTime(expire - (System.currentTimeMillis()
										- sp.getData("lastAuthentication", Number.class).longValue())))
						.replace("%player%", player.getName());

				MSG.tell(player, msg);
				continue;
			}

			String msg = ScorchCore.getInstance().getMessage("welcomeauthenticate");
			msg = msg.replace("%player%", player.getName());

			MSG.tell(player, msg);
		}
	}

	public boolean authenticated(UUID uuid) {
		if (!ScorchCore.getInstance().getPlayer(uuid).hasData("2fakey"))
			return true;
		return ScorchCore.getInstance().getPlayer(uuid).getData("authenticated", Boolean.class, false);
	}

	@Override
	public void disable() {

	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (!player.hasPermission("scorch.2fa"))
			return;

		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		if (!sp.hasData("lastAuthentication")) {
			setupAuthentication(player);
			return;
		}
		if (sp.hasData("lastAuthentication") // 1 Day
				&& System.currentTimeMillis() - sp.getData("lastAuthentication", Number.class).longValue() < expire) {
			String msg = ScorchCore.getInstance().getMessage("authenticatetimeleft");
			msg = msg
					.replace("%time%",
							MSG.getTime(System.currentTimeMillis()
									- sp.getData("lastAuthentication", Number.class).longValue()))
					.replace("%player%", player.getName());

			MSG.tell(player, msg);
			sp.setData("authenticated", true);
			return;
		}

		String msg = ScorchCore.getInstance().getMessage("welcomeauthenticate");
		msg = msg.replace("%player%", player.getName());

		MSG.tell(player, msg);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (!authenticated(player.getUniqueId())) {
			authenticate(player, event.getMessage().replace(" ", ""));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!authenticated(player.getUniqueId())) {
			if (event.getTo().getX() == event.getFrom().getX() && event.getTo().getZ() == event.getFrom().getZ())
				return;
			MSG.cTell(player, "mustauthenticate");
			Location to = event.getFrom();
			to.setPitch(event.getTo().getPitch());
			to.setYaw(event.getTo().getYaw());
			event.setTo(to);
		}
	}

	@EventHandler
	public void onItemChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		if (!authenticated(player.getUniqueId()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (!authenticated(player.getUniqueId()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onItemChange(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!authenticated(player.getUniqueId()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String name = event.getMessage().split(" ")[0].substring(1);
		if (!authenticated(player.getUniqueId())) {
			if (!Arrays.asList("2fa", "2factor", "tfactor", "twofactor").contains(name.toLowerCase())) {
				event.setCancelled(true);
				MSG.cTell(player, "mustauthenticate");
			}
		}
	}

	public void setupAuthentication(Player player) {
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		String key = sp.getData("2fakey", String.class, TOTP.generateBase32Secret());
		try {
			URL url = new URL(TOTP.qrImageUrl(ScorchCore.getInstance().getName() + "@" + player.getName(), key));

			BufferedImage image = ImageIO.read(url);
			ItemStack i = new ItemStack(Material.FILLED_MAP);
			MapMeta meta = (MapMeta) i.getItemMeta();
			MapView mv = Bukkit.createMap(player.getWorld());

			mv.getRenderers().clear();
			mv.addRenderer(new QRMap(image));
			meta.setMapView(mv);
			meta.setDisplayName(MSG.color("&72FA Key " + "&e" + key));
			i.setItemMeta(meta);
			player.getInventory().setItemInMainHand(i);
		} catch (Exception e) {
			e.printStackTrace();
		}

		MSG.tell(player, " ");
		MSG.tell(player, "&3&l2FA Setup Information");
		MSG.tell(player, "&9In order to access this server, you must");
		MSG.tell(player, "&9setup &btwo-factor authentication&9.");
		MSG.tell(player, "&9Download a 2FA app and scan the barcode displayed");
		MSG.tell(player, "&9or type in the code &e" + key);
		MSG.tell(player, " ");
		MSG.tell(player,
				"&cKeep this key to yourself. If you lose access to your phone/app/2FA app, you will be unable to login.");
		sp.setData("2fakey", key);
		sp.setData("authenticated", false);
	}

	public boolean authenticate(Player player, String key) {
		ScorchPlayer sp = ScorchCore.getInstance().getPlayer(player.getUniqueId());
		try {
			MSG.tell(player, "[" + TOTP.generateCurrentNumber(sp.getData("2fakey", String.class)) + "]");
			if (key.equals(TOTP.generateCurrentNumberString(sp.getData("2fakey", String.class)))) {
				MSG.cTell(player, "authenticated");
				sp.setData("lastAuthentication", System.currentTimeMillis());
				sp.setData("authenticated", true);
				if (player.getInventory().getItemInMainHand() != null
						&& player.getInventory().getItemInMainHand().getType() == Material.FILLED_MAP) {
					player.getInventory().setItemInMainHand(new ItemStack((Material.AIR)));
				}
				return true;
			} else {
				MSG.tell(player, "Unable to authenticate.");
				return false;
			}

		} catch (GeneralSecurityException e) {
			MSG.tell(player, "Unable to authenticate");
			e.printStackTrace();
			return false;
		}
	}

}
