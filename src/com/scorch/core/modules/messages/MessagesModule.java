package com.scorch.core.modules.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.AbstractModule;
import com.scorch.core.modules.data.exceptions.DataObtainException;
import com.scorch.core.modules.data.exceptions.NoDefaultConstructorException;
import com.scorch.core.utils.Logger;

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
	}

	@Override
	public void initialize() {
		messages = new ArrayList<CMessage>();

		try {
			Logger.log("Loading messages...");

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
				Logger.log(msg.getId() + " does not exist, saving default.");
			}

		} catch (NoDefaultConstructorException | DataObtainException e) {
			e.printStackTrace();
		}
		Logger.log("Successfully loaded " + messages.size() + " message" + (messages.size() == 1 ? "" : "s") + ".");
	}

	public CMessage getMessage(String id) {
		return messages.stream().filter(cm -> cm.getId().equals(id)).findFirst().orElse(null);
	}

	@Override
	public void disable() {

	}
}
