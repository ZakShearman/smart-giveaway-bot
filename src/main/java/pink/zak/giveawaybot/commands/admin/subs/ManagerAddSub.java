package pink.zak.giveawaybot.commands.admin.subs;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pink.zak.giveawaybot.GiveawayBot;
import pink.zak.giveawaybot.lang.enums.Text;
import pink.zak.giveawaybot.models.Server;
import pink.zak.giveawaybot.service.command.command.SubCommand;

import java.util.List;

public class ManagerAddSub extends SubCommand {

    public ManagerAddSub(GiveawayBot bot) {
        super(bot, true);
        this.addFlatWithAliases("role", "manager");
        this.addFlat("add");
        this.addArgument(Role.class);
    }

    @Override
    public void onExecute(Member sender, Server server, MessageReceivedEvent event, List<String> args) {
        if (server.getManagerRoles().size() >= 5) {
            this.langFor(server, Text.ADMIN_MANAGER_LIMIT_REACHED).to(event.getTextChannel());
            return;
        }
        Role role = this.parseArgument(args, event.getGuild(), 2);
        if (role == null) {
            this.langFor(server, Text.COULDNT_FIND_ROLE).to(event.getTextChannel());
            return;
        }
        if (server.getManagerRoles().contains(role.getIdLong())) {
            this.langFor(server, Text.ADMIN_MANAGER_ALREADY_CONTAINS).to(event.getTextChannel());
            return;
        }
        server.getManagerRoles().add(role.getIdLong());
        this.langFor(server, Text.ADMIN_MANAGER_ROLE_ADDED, replacer -> replacer.set("name", role.getName())).to(event.getTextChannel());
    }
}
