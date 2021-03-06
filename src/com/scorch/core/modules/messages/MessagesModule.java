package com.scorch.core.modules.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.scorch.core.ScorchCore;
import com.scorch.core.events.messages.MessageReceiveEvent;
import com.scorch.core.events.messages.MessageSendEvent;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.communication.exceptions.WebSocketException;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.DataPrimaryKeyException;
import com.scorch.core.modules.data.exceptions.DataUpdateException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;
import com.scorch.core.utils.MSG;

/**
 * Module that loads and keeps track of all messages stored in the database
 * 
 * @author imodm
 *
 */
public class MessagesModule extends AbstractModule implements Listener {

	private List<CMessage> messages, defaults;

	public MessagesModule(String id) {
		super(id);

		Bukkit.getPluginManager().registerEvents(this, ScorchCore.getInstance());

		defaults = new ArrayList<CMessage>();

		defaults.add(new CMessage("test", "The test works!"));
		defaults.add(new CMessage("noperm", "&cYou do not have permission."));
		defaults.add(new CMessage("punishmessage", "&c&l[PUNISH] &a%staff% &c%verb% &e%target%&7. Reason: &e%reason%"));
		defaults.add(new CMessage("appeallink", "https://yourwebsite.com/appeallink"));
		defaults.add(new CMessage("tempbanmessage",
				"&c&lYou have been &4&l%verb% &c&lby &6&l%staff% &c&lfor &e&l%duration%\n&r%reason%\n&c&lTime Left: %timeleft%\n%appeal%"));
		defaults.add(new CMessage("permbanmessage",
				"&c&lYou have been &4&l%verb% &c&lby &6&l%staff% \n&r%reason%\n%appeal%"));
		defaults.add(new CMessage("warningmessage",
				"&c&l[&4&lWARNING&c&l] &7You have been issued a punishment by &c%staff%|&c&lReason&7: %reason%|&dPlease make sure to read the rules to avoid further punishment."));
		defaults.add(new CMessage("tempmutemessage",
				"&cYou are muted for &e%timeleft% &7by &a%staff%&c for &e%reason%.&c &7(Time Left: &8%timeleft%&7)"));
		defaults.add(
				new CMessage("permmutemessage", "&cYou are &4Permanentely &cmuted by &a%staff% &cfor &e%reason%&7."));
		defaults.add(new CMessage("reportbanmessage",
				"&7&e%staff% &7has restricted your ability to create reports. Reason: &e%reason%"));
		defaults.add(new CMessage("offlinemessageheader", "&7You have &e%amo% &7unread message%s%."));
		defaults.add(new CMessage("offlinemessageformat", "&1%sender%&9: &b%message% &7[&8%time%&7]"));
		defaults.add(new CMessage("aformat", "%prefix%%player%&d %message%"));
		defaults.add(new CMessage("maformat-receiver", "&5<- &r%group%%player%&d %message%"));
		defaults.add(new CMessage("maformat-sender", "&5-> &r%group%%player%&d %message%"));
		defaults.add(new CMessage("maformat-spec",
				"%senderprefix%%sendername% &5-> &r%receiverprefix%%receivername% &d%message%"));
		defaults.add(new CMessage("helpmessage", "Edit this in the database"));
		defaults.add(new CMessage("vanishenablemessage", "Vanish enabled"));
		defaults.add(new CMessage("vanishdisablemessage", "Vanish disabled"));

		defaults.add(new CMessage("togglevanish", "&7You %status% &7%target%'%s% &7vanish status."));

		defaults.add(new CMessage("seenformat", "&e%player% &7was last seen &a%time% &7ago."));
		defaults.add(new CMessage("playtimeformat", "&e%player% &7has played for &a%time%&7."));
		defaults.add(new CMessage("noplaytime", "&e%player% &7has never played on this server."));
		defaults.add(
				new CMessage("gamemodeformat", "&aSuccessfully &7set &e%player%&7'%s% gamemode to &e%gamemode%&7."));
		defaults.add(new CMessage("commandtoggleformat", "&7The &e/%command% &7command has been %status%&7."));
		defaults.add(new CMessage("moduletoggleformat", "&7The &e%module% &7module has been %status%&7."));
		defaults.add(
				new CMessage("buildmodeinspectentity-natural", "&7This entity was not spawned in via build mode."));
		defaults.add(new CMessage("buildmodeinspectentity-player", "&7This entity was spawned in by &e%player%&7."));
		defaults.add(
				new CMessage("buildmodeinspectblock-natural", "&7This block was not placed by someone in build mode."));
		defaults.add(new CMessage("buildmodeinspectblock-player", "&7This block was placed by &e%player%&7."));
		defaults.add(new CMessage("buildmoderollbackall", "&7Rolled back everyone's builds."));
		defaults.add(new CMessage("buildmoderollback", "&7Rolled back &e%player%&7'%s% builds."));
		defaults.add(new CMessage("buildmoderollbacknumber",
				"&7Rolled back &a%block% &7block%bs% &7of &e%player%&7'%s% blocks."));
		defaults.add(new CMessage("buildmoderollbackfail",
				"&e%player% &7does not have &c%block% &7block%s% in build mode."));
		defaults.add(new CMessage("buildmodetoggle", "&e%player%&7'%s% &6%mode% &7mode has been %status%&7."));
		defaults.add(new CMessage("healmessage", "&aSuccessfully &2healed &e%target%&a."));
		defaults.add(new CMessage("feedmessage", "&aSuccessfully &2fed &e%target%&a."));
		defaults.add(new CMessage("reportcreated", "&9Report &b#%id% &asuccessfully &9created."));
		defaults.add(new CMessage("nomessages", "&cYou do not have any recent messages."));
		defaults.add(new CMessage("noreports", "&cAll reports in this category have been handled. &aYay!"));
		defaults.add(new CMessage("reportassigned", "&7You are now assigned to report &e%id%&7."));
		defaults.add(new CMessage("resolvereport", "&7You marked report &e%id% &7as &a%status%&7. Reason: &6%reason%"));
		defaults.add(new CMessage("notassigned", "&cYou are not assigned to a report"));
		defaults.add(new CMessage("mustauthenticate", "&cPlease authenticate &7using &e/2fa [Code]"));
		defaults.add(
				new CMessage("welcomeauthenticate", "&7Welcome back &a%player%&7. Please sign in using your 2FA app."));
		defaults.add(
				new CMessage("authenticatetimeleft", "&7Welcome back &a%player%&7. You are signed in for &a%time%&7."));
		defaults.add(new CMessage("authenticated", "&aSuccessfully authenticated. Welcome!"));
		defaults.add(new CMessage("announcement-title", "&c&lANNOUNCEMENT"));
		defaults.add(new CMessage("announcement-subtitle", "&b%msg%"));
		defaults.add(new CMessage("announcement-message", "&3Announcement> &b%msg%"));
		defaults.add(new CMessage("report-resolved",
				"&b%id% &7was marked as &e%status% &7by &a%staff%&7. Reason: &b%reason%"));
		defaults.add(new CMessage("abusive-report",
				"&c&lWARNING &7Abusing the report system will result in a &4Report Ban&7."));
//		defaults.add(new CMessage("", ""));
	}

	@Override
	public void initialize() {
		reloadMessages();
	}

	public void reloadMessages() {
		messages = new ArrayList<CMessage>();

		try {
			Logger.log("&9Loading messages...");

			ScorchCore.getInstance().getDataManager().createTable("messages", CMessage.class);
			ScorchCore.getInstance().getDataManager().getAllObjects("messages").forEach(cm -> {
				messages.add((CMessage) cm);
			});

			/**
			 * Save any messages that aren't already saved
			 */
			try {
				for (CMessage msg : defaults.stream().filter(cm -> getMessage(cm.getId()) == null)
						.collect(Collectors.toList())) {
					ScorchCore.getInstance().getDataManager().updateObject("messages", msg);

					messages.add(msg);
					Logger.log("&e" + msg.getId() + " &cdoes not exist&b, saving default.");
				}
			} catch (DataUpdateException e) {
				e.printStackTrace();
			}
			Logger.log("&aSuccessfully loaded &e" + messages.size() + "&a message" + (messages.size() == 1 ? "" : "s")
					+ ".");
		} catch (NoDefaultConstructorException | DataObtainException | DataPrimaryKeyException e) {
			e.printStackTrace();
		}

	}

	public CMessage getMessage(String id) {
		return messages.stream().filter(cm -> cm.getId().equals(id)).findFirst().orElse(null);
	}

	@Override
	public void disable() {

	}

	public void sendMessage(Player sender, UUID receiver, String message) {
		MessageSendEvent mse = new MessageSendEvent(sender.getUniqueId(), sender.getName(), receiver, message);
		try {
			ScorchCore.getInstance().getCommunicationModule().dispatchEvent(mse);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void messageSend(MessageSendEvent event) {
		UUID receiver = event.getReceiver(), sender = event.getSender();
		String message = event.getMessage();

		OfflinePlayer receivePlayer = Bukkit.getOfflinePlayer(receiver);

		if (!receivePlayer.isOnline())
			return;

		MSG.tell(receivePlayer.getPlayer(), message);

		MessageReceiveEvent mre = new MessageReceiveEvent(receiver, sender, message);
		try {
			ScorchCore.getInstance().getCommunicationModule().dispatchEvent(mre);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void messageReceived(MessageReceiveEvent event) {
		UUID receiver = event.getReceiver(), sender = event.getSender();
		String message = event.getMessage();

		OfflinePlayer receiverPlayer = Bukkit.getOfflinePlayer(sender);

		if (!receiverPlayer.isOnline())
			return;

		MSG.tell(receiverPlayer.getPlayer(), message);

		MessageReceiveEvent mre = new MessageReceiveEvent(receiver, sender, message);
		try {
			ScorchCore.getInstance().getCommunicationModule().dispatchEvent(mre);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

}
