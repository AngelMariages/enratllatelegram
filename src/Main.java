import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.sun.istack.internal.Nullable;

import java.util.List;

public class Main {
	private static TelegramBot telegramBot = null;
	private static boolean gettingUpdates = false;

	public static void main(String[] args) {
		telegramBot = TelegramBotAdapter.build("224863668:AAHXadjCsn5S6d__SdmyH0YzNp_l_upUV30");
		gettingUpdates = true;
		getUpdates();
	}

	private static void getUpdates() {
		Thread updateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				GetUpdatesResponse updatesResponse;
				int lastUpdate = 0;

				while(gettingUpdates) {
					try {
						updatesResponse = telegramBot.execute(new GetUpdates()
								.offset(lastUpdate)
								.limit(100)
								.timeout(20));
						List<Update> updates = updatesResponse.updates();
						for(Update update : updates) {
							lastUpdate = update.updateId() + 1;
							Message message = update.message();

							if(message.text() != null) {
								System.out.println("New update from: " + message.from().firstName() + ", text: " + message.text());
								checkCommand(message.text(), message.chat().id());
							}
						}
						Thread.sleep(1000);
					} catch(RuntimeException e) {
						System.out.println("Didn't get updates for long time, waiting 5 seconds");
						try {
							Thread.sleep(5000);
						} catch(InterruptedException ignored) {}
					} catch(Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}
		});
		updateThread.run();
	}

	private static void checkCommand(String command, Object chatId) {
		if(command.equals("/novapartida")) {
			ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(new String[][]{
					{"\u2716", "\u2B55", "\u2B55"},
					{"\u2B55", "\u2716", "\u2716"},
					{"\u2716", "\u2B55", "\u2B55"}
			},false, true,false);
			sendMessage(chatId, "Nova partida!", replyKeyboardMarkup);
		} else if(command.equals("/start")) {
			sendMessage(chatId, "Benvingut al bot del 3 en ratlla");
		}
	}

	private static void sendMessage(Object chatId, String message) {
		telegramBot.execute(new SendMessage(chatId, message));
	}

	private static void sendMessage(Object chatId, String message, ReplyKeyboardMarkup keyBoardMarkup) {
		telegramBot.execute(new SendMessage(chatId,message).
				replyMarkup(keyBoardMarkup));
	}
}