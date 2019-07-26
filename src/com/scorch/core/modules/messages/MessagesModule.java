package com.scorch.core.modules.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;

/**
 * Module that loads and keeps track of all messages stored in the database
 * 
 * @author imodm
 *
 */
public class MessagesModule extends AbstractModule {

	private List<CMessage> messages, defaults;

	public MessagesModule(String id) {
		super(id);

		defaults = new ArrayList<CMessage>();

		defaults.add(new CMessage("test", "The test works!"));
		defaults.add(new CMessage("noperm", "&cYou do not have permission."));
		defaults.add(new CMessage("punishmessage", "&c&l[PUNISH] &a%staff% &c%verb% &e%target%&7. Reason: &e%reason%"));
		defaults.add(new CMessage("appeallink", "https://yourwebsite.com/appeallink"));
		defaults.add(new CMessage("tempbanmessage",
				"&c&lYou have been &4&l%verb% &c&lby &6&l%staff% &c&lfor &e&l%duration%\n&b%reason%\n&c&lTime Left: %timeleft%\n%appeal%"));
		defaults.add(new CMessage("permbanmessage",
				"&c&lYou have been &4&l%verb% &c&lby &6&l%staff% \n&b%reason%\n%appeal%"));
		defaults.add(new CMessage("warningmessage",
				"&c&l[&4&lWARNING&c&l] &7You have been issued a punishment by &c%staff%|&c&lReason&7: %reason%|&dPlease make sure to read the rules to avoid further punishment."));
		defaults.add(new CMessage("tempmutemessage",
				"&cYou are muted for &e%timeleft% &7by &a%staff%&c for &e%reason%.&c &7(Time Left: &8%timeleft%&7)"));
		defaults.add(
				new CMessage("permmutemessage", "&cYou are &4Permanentely &cmuted by &a%staff% &cfor &e%reason%&7."));
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
		defaults.add(new CMessage("buildmodetoggle", "&e%player%&7'%s% build mode has been %status%&7."));
		defaults.add(
				new CMessage("buildmodeinspectentity-natural", "&7This entity was not spawned in via build mode."));
		defaults.add(new CMessage("buildmodeinspectentity-player", "&7This entity was spawned in by &e%player%&7."));
		defaults.add(
				new CMessage("buildmodeinspectblock-natural", "&7This block was not placed by someone in build mode."));
		defaults.add(new CMessage("buildmodeinspectblock-player", "&7This block was placed by &e%player%&7."));
		defaults.add(
				new CMessage("buildmodeinspecttoggle", "&e%target%&7'%s% &6inspection &7mode has been %status%&7."));
		defaults.add(
				new CMessage("buildmodeoverridetoggle", "&e%target%&7'%s% &6override &7mode has been %status%&7."));
		defaults.add(new CMessage("buildmodebuildtoggle", "&e%target%&7'%s% &6build &7mode has been %status%&7."));

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
			for (CMessage msg : defaults.stream().filter(cm -> getMessage(cm.getId()) == null)
					.collect(Collectors.toList())) {
				ScorchCore.getInstance().getDataManager().saveObject("messages", msg);
				messages.add(msg);
				Logger.log("&e" + msg.getId() + " &cdoes not exist&b, saving default.");
			}

			Logger.log("&aSuccessfully loaded &e" + messages.size() + "&a message" + (messages.size() == 1 ? "" : "s")
					+ ".");
		} catch (NoDefaultConstructorException | DataObtainException e) {
			e.printStackTrace();
		}

	}

	public CMessage getMessage(String id) {
		return messages.stream().filter(cm -> cm.getId().equals(id)).findFirst().orElse(null);
	}

	@Override
	public void disable() {

	}

}
