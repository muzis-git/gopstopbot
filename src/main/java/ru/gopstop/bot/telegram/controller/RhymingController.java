package ru.gopstop.bot.telegram.controller;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.ActionType;
import org.telegram.telegrambots.api.methods.send.SendChatAction;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.gopstop.bot.engine.CleverEngine;
import ru.gopstop.bot.engine.entities.GopSong;
import ru.gopstop.bot.engine.entities.Rhyme;
import ru.gopstop.bot.muzis.entity.Performer;
import ru.gopstop.bot.muzis.entity.SearchResult;
import ru.gopstop.bot.muzis.entity.Song;
import ru.gopstop.bot.telegram.Constants;
import ru.gopstop.bot.telegram.TGBot;
import ru.gopstop.bot.telegram.internal.Emoji;
import ru.gopstop.bot.telegram.user.TGSession;
import ru.gopstop.bot.util.TwitGen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Semyon on 30.07.2016.
 */
public class RhymingController extends BaseMuzisController {

    public RhymingController(final TGBot bot) {
        super(bot);
    }

    @Override
    public String getKey() {
        return Constants.RHYMES;
    }

    @Override
    public String getEntry() {
        return Constants.RHYMES_PLS + " " + Emoji.FACE_THROWING_A_KISS.toString();
    }

    @Override
    public void handleMessage(final Message request, final TGSession session) throws TelegramApiException {
        final String text = request.getText();
        if (getEntry().equals(text)) {
            onMain(request, session);
            return;
        }

        if (text.equals(BACK)) {
            back(request, session);
            return;
        }

        sendAction(request.getChatId().toString(), ActionType.TYPING);

        final Rhyme rhyme = CleverEngine.getRhyme(text);

        if (rhyme != null) {
            onRhymeFound(request, session, rhyme);
        } else {
            sendMessage(request.getChatId().toString(), "Ничего не нашли");
        }
    }

    private void onRhymeFound(final Message request, final TGSession session, final Rhyme rhyme) throws TelegramApiException {

        final GopSong gopSong = rhyme.getGopSong();
        sendHtmlMessage(
                request.getChatId().toString(),
                String.format("Рифмы подъехали\n<b>%s</b>\n(%s - %s)", rhyme.getRhyme(), gopSong.getAuthor(), gopSong.getName()));

        String gopSongName = gopSong.getName().replace("-", " "); // иначе не ищет!

        final SearchResult res = muzisService.search(gopSongName + " " + gopSong.getAuthor(), null, null, null, null, null, null);

        final Optional<Song> foundSong = res.getSongs()
                .stream()
                .filter(song -> song.getPerformer().contains(gopSong.getAuthor()) || gopSong.getAuthor().contains(song.getPerformer()))
                .findFirst();

        if (!foundSong.isPresent()) {
            // ничего не нашли, пробуем исполнителя и ищем по его id 6 треков
            final List<Performer> performers = muzisSearchHelper.searchByPerformer(gopSong.getAuthor());
            final Optional<Performer> first = performers
                    .stream()
                    // возможно это надо убрать
                    // проверка, что это точно тот автор, который нам нужен
                    .filter(performer -> muzisSearchHelper.checkPerformer(performer, gopSong.getAuthor()))
                    .findFirst();

            final List<Song> songs = first.map(performer ->
                    muzisService.songsByPerformer(performer.getId())).map(byAuthorRes ->
                    byAuthorRes.getSongs().stream()
                            .limit(6)
                            .collect(Collectors.toList())).orElseGet(Collections::emptyList);

            // к названию приклеиваем "Слушать ", чтобы потом было понятно, что это запрос на прослушивание
            final List<String> keyboard = songs.stream()
                    .map(song -> "Слушать " + song.getTitle())
                    .collect(Collectors.toList());

            String reply = "";
            if (keyboard.isEmpty()) {
                reply = String.format("Но мы не нашли репертуар автора (%s) на Muzis. Можешь искать рифмы дальше.", gopSong.getAuthor());
            } else {
                reply = "Эту песню мы не нашли, но вот другие песни автора";
                // перекидываем на поиск по песням
                session.setLastController(Constants.SONGS);
            }
            keyboard.add(BACK);
            final ReplyKeyboardMarkup replyKeyboardMarkup = buildKeyboard(keyboard);

            session.put("results", songs);

            final SendMessage msg = createMessageWithKeyboard(request.getChatId().toString(), request.getMessageId(), reply, replyKeyboardMarkup);
            bot.sendMessage(msg);

            sendMessage(
                    request.getChatId().toString(),
                    "[Рассказать пацанам из твиттера](" + TwitGen.generate(request.getText(), rhyme.getRhyme(), rhyme.getGopSong().getName()) + ")");

            return;
        }

        final Song song = foundSong.get();
        sendSongAndCover(request, song);

        sendMessage(
                request.getChatId().toString(),
                "[Рассказать пацанам из твиттера](" + TwitGen.generate(request.getText(), rhyme.getRhyme(), rhyme.getGopSong().getName()) + ")");
        sendMessage(request.getChatId().toString(), "Послушай, а потом можешь искать новые рифмы и песни");
    }

    private void onMain(final Message request, final TGSession session) throws TelegramApiException {
        final ReplyKeyboardMarkup replyKeyboardMarkup = buildKeyboard(Arrays.asList(BACK));
        final SendMessage msg = createMessageWithKeyboard(request.getChatId().toString(), request.getMessageId(), "Сегодня мы с тобой рифмуем", replyKeyboardMarkup);
        bot.sendMessage(msg);
    }

}
