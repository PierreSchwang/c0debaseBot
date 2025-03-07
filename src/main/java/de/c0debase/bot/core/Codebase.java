package de.c0debase.bot.core;

import de.c0debase.bot.commands.CommandManager;
import de.c0debase.bot.database.DataManager;
import de.c0debase.bot.database.MongoDataManager;
import de.c0debase.bot.listener.guild.GuildMemberJoinListener;
import de.c0debase.bot.listener.guild.GuildMemberLeaveListener;
import de.c0debase.bot.listener.guild.GuildMemberNickChangeListener;
import de.c0debase.bot.listener.guild.GuildMemberRoleListener;
import de.c0debase.bot.listener.message.MessageReactionListener;
import de.c0debase.bot.listener.message.MessageReceiveListener;
import de.c0debase.bot.listener.message.TableFlipListener;
import de.c0debase.bot.listener.other.GuildReadyListener;
import de.c0debase.bot.listener.voice.GuildVoiceListener;
import de.c0debase.bot.tempchannel.Tempchannel;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Codebase {

    private static final Logger logger = LoggerFactory.getLogger(Codebase.class);

    private final JDA jda;
    private Guild guild;
    private final DataManager dataManager;
    private final CommandManager commandManager;
    private final Map<String, Tempchannel> tempchannels;

    public Codebase() throws Exception {
        final long startTime = System.currentTimeMillis();
        logger.info("Starting c0debase");

        tempchannels = new HashMap<>();

        dataManager = initializeDataManager();
        logger.info("Database-Connection set up!");

        jda = initializeJDA();
        logger.info("JDA set up!");

        commandManager = new CommandManager(this);
        logger.info("Command-Manager set up!");

        new GuildVoiceListener(this);

        new MessageReactionListener(this);
        new MessageReceiveListener(this);
        new TableFlipListener(this);

        new GuildMemberJoinListener(this);
        new GuildMemberLeaveListener(this);
        new GuildMemberNickChangeListener(this);
        new GuildMemberRoleListener(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                dataManager.close();
            } catch (final Exception exception) {
                exception.printStackTrace();
            }
            jda.shutdown();
        }));
        logger.info(String.format("Startup finished in %dms!", System.currentTimeMillis() - startTime));
    }

    /***
     * Connect to the database
     * @return The {@link DataManager} instance
     * @throws Exception
     */
    private DataManager initializeDataManager() throws Exception {
        try {
            return new MongoDataManager(System.getenv("MONGO_HOST") == null ? "localhost" : System.getenv("MONGO_HOST"), System.getenv("MONGO_PORT") == null ? 27017 : Integer.valueOf(System.getenv("MONGO_PORT")), this);
        } catch (final Exception exception) {
            logger.error("Encountered exception while initializing Database-Connection!");
            throw exception;
        }
    }

    /**
     *
     * @return The {@link JDA} instance fot the current session
     * @throws Exception
     */
    private JDA initializeJDA() throws Exception {
        try {
            final JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
            jdaBuilder.setToken(System.getenv("DISCORD_TOKEN"));
            jdaBuilder.setActivity(Activity.playing("auf c0debase"));
            jdaBuilder.addEventListeners(new ListenerAdapter() {
                @Override
                public void onGuildReady(@Nonnull GuildReadyEvent event) {
                    guild = event.getGuild();
                }
            }, new GuildReadyListener(this));
            return jdaBuilder.build().awaitReady();
        } catch (Exception exception) {
            logger.error("Encountered exception while initializing ShardManager!");
            throw exception;
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public JDA getJDA() {
        return jda;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Map<String, Tempchannel> getTempchannels() {
        return tempchannels;
    }

    public Guild getGuild() {
        return guild;
    }
}
