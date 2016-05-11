/*
 * Main.java
 *
 * Copyright 2016 Àngel Mariages <angel[dot]mariages[at]gmail[dot]com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 *
 */

import com.firebase.client.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;

import java.util.List;

public class Main {
	private static TelegramBot telegramBot = null;
	private static boolean gettingUpdates = false;
	private static Firebase firebase = null;

	public static void main(String[] args) {
		telegramBot = TelegramBotAdapter.build("224863668:AAHXadjCsn5S6d__SdmyH0YzNp_l_upUV30");
		firebase = new Firebase("https://enratllabottelegram.firebaseio.com/");

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
								System.out.println("New update from: " + message.from().firstName() + ", text: " + message.text() + ", chat: " + message.chat());
								checkCommand(message.text(), message.chat().id(), message.from().username());
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

	private static void checkCommand(String command, final Object chatId, String from) {
		if(command.startsWith("/novapartida")) {
			ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(new String[][]{
					{"\u1F30\udc30", "\u2B55", "\u2B55"},
					{"\u2B55", "\u1F30\uDC30", "\u2716"},
					{"\u1F30\uDC30", "\u2B55", "\u2B55"}
			},false, true,false);
			//sendMessage(chatId, "Nova partida!", replyKeyboardMarkup);
			sendMessage(chatId, "Cagun");
		} else if(command.equals("/start")) {
			sendMessage(chatId, "Benvingut al bot del 3 en ratlla");
			firebase.child(from).child("user_id").setValue(chatId);
		} else if(command.startsWith("/comprovarusuari ")) {
			final String usuari = command.substring("/comprovarusuari ".length());
			System.out.println("US:" + usuari);
			if(!usuari.isEmpty()) {
				firebase.child(usuari).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						if(dataSnapshot.child("user_id").getValue() != null) {
							long idUsuari = (long) dataSnapshot.child("user_id").getValue() ;
							sendMessage(chatId, "L'usuari " + usuari + " esta a la nostra base de dades (" + idUsuari + ")");
						}
					}

					@Override
					public void onCancelled(FirebaseError firebaseError) {

					}
				});
			}
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