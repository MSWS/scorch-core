package com.scorch.core.modules.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.scorch.core.ScorchCore;
import com.scorch.core.commands.TestCommand;
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
	}

	@Override
	public void initialize() {
		messages = new ArrayList<CMessage>();

		new TestCommand();

		try {
			Logger.log("Creating table...");
			ScorchCore.getInstance().getDataManager().createTable("messages", CMessage.class);
			Logger.log("Getting objects...");
			ScorchCore.getInstance().getDataManager().getAllObjects("messages").forEach(cm -> {
				messages.add((CMessage) cm);
			});

			/**
			 * Save any messages that aren't already saved
			 */
			Logger.log("Saving objects...");
			for (CMessage msg : defaults.stream().filter(cm -> getMessage(cm.getId()) == null)
					.collect(Collectors.toList())) {
				ScorchCore.getInstance().getDataManager().saveObject("messages", msg);
			}

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
