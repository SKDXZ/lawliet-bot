package Commands.InteractionsCategory;
import CommandListeners.CommandProperties;

import Commands.InteractionAbstract;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

@CommandProperties(
        trigger = "nom",
        emoji = "\uD83E\uDD62",
        executable = true,
        aliases = {"eat"}
)
public class NomCommand extends InteractionAbstract {

    protected String[] getGifs() {
        return new String[]{"https://media1.tenor.com/images/98b21965b83feb66b6f25b8be45d6214/tenor.gif?itemid=13516862",
                "https://media1.tenor.com/images/128c1cfb7f4e6ea4a4dce9b487648143/tenor.gif?itemid=12051598",
                "https://media1.tenor.com/images/c688d2cf5c50569c74ce8e8d87c40935/tenor.gif?itemid=13341413",
                "https://media1.tenor.com/images/11970fb6f0b92572722a5c01e878ff3d/tenor.gif?itemid=10724269",
                "https://media1.tenor.com/images/fb032d8945dbd67d472e794a7afd466c/tenor.gif?itemid=13721331",
                "https://media1.tenor.com/images/c0c0f8bb63f38f0ddf6a736354987050/tenor.gif?itemid=4383037",
                "https://media1.tenor.com/images/48679297034b0f3f6ee28815905efae8/tenor.gif?itemid=5416132",
                "https://media1.tenor.com/images/d53f784475cb4f2b7b9ea956558d8634/tenor.gif?itemid=6217327",
                "https://media1.tenor.com/images/44b750b624237a3e358e4f7d010f246e/tenor.gif?itemid=12018762",
                "https://media1.tenor.com/images/ff7ee46ace95eeb3d851cd54c015eadb/tenor.gif?itemid=12018771",
                "https://media1.tenor.com/images/4a1d3739bd6de090f77f56776391c0f1/tenor.gif?itemid=12018776",
                "https://media1.tenor.com/images/0de27657daa673ccd7a60cf6919084d9/tenor.gif?itemid=4848690",
                "https://media1.tenor.com/images/2b13a435a03af7d4b84edd3bb0bc9254/tenor.gif?itemid=12018765",
                "https://media1.tenor.com/images/8b8451ab32c9856e669d9bb627e86130/tenor.gif?itemid=12018785",
                "https://media1.tenor.com/images/2f086a162bb60f4d4d2fcf999617d2e0/tenor.gif?itemid=11695522",
                "https://media1.tenor.com/images/b6be500ce23dc8d6adcaa1d7e3604cc5/tenor.gif?itemid=4950011",
                "https://media1.tenor.com/images/9b782a9896bba5bcba4f8c7d1688a406/tenor.gif?itemid=13266333",
                "https://media1.tenor.com/images/6b645f7729cd1c7a548204bd161bf4df/tenor.gif?itemid=13622096",
                "https://media1.tenor.com/images/26beab5ca39fba753a2de57b1d74e519/tenor.gif?itemid=5215437",
                "https://media1.tenor.com/images/0f39c25fd31e3ba33cabf86d566cf55f/tenor.gif?itemid=13458184",
                "https://media1.tenor.com/images/de3c538d45c1a0a615352f1ddc66ef02/tenor.gif?itemid=12075787",
                "https://media1.tenor.com/images/5cfba1dd5108844a2bf565e0ce91e47a/tenor.gif?itemid=11927693",
                "https://media1.tenor.com/images/595c70fc60e18108f10dafe074e8833e/tenor.gif?itemid=11654111",
                "https://media1.tenor.com/images/f615ab049c0edc4b3579132ea276e3af/tenor.gif?itemid=8870471",
                "https://media1.tenor.com/images/d6e1ffa70eb553dacdf19d26a6be0500/tenor.gif?itemid=5571354",
                "https://media1.tenor.com/images/3799ae77c7fcd79eed7881e3e29ede4a/tenor.gif?itemid=6229812",
                "https://media1.tenor.com/images/17ba890dc4bd5aed76ddb152ff6753a7/tenor.gif?itemid=12700091",
                "https://i.pinimg.com/originals/0b/47/3f/0b473fb8b5fbf66a0a003d25bc937f49.gif",
                "https://media.giphy.com/media/t7UA6Mg6peVfq/giphy.gif",
                "https://media.giphy.com/media/eEViNnxHEOLsI/giphy.gif",
                "https://media.giphy.com/media/eSwGh3YK54JKU/giphy.gif",
                "https://media.giphy.com/media/hXYlYBixtHEFq/giphy.gif",
                "https://media.giphy.com/media/PxdjO4EBNuLrW/giphy.gif",
                "https://media.giphy.com/media/RneIcLEosVuta/giphy.gif",
                "https://media.giphy.com/media/mAuMfyo78F1vy/giphy.gif",
                "https://media.giphy.com/media/irBHYSZxbUifTxTgBL/giphy.gif",
                "https://i.imgur.com/Njfycqc.gif",
                "https://i.giphy.com/media/2EQ7NCJZhI8iQ/source.gif",
                "https://i.imgur.com/nI3b0Re.gif",
                "https://i.imgur.com/tXFyBIF.gif",
                "https://data.whicdn.com/images/221155408/original.gif",
                "https://i.giphy.com/media/ViC6liI1iz3TGxb4po/giphy.webp",
                "https://i.pinimg.com/originals/93/00/e4/9300e4114417a1cfdc83f8d7c7bd79ec.gif",
                "https://i.pinimg.com/originals/7a/aa/f0/7aaaf009f46db00b60d44dc12a08f967.gif",
                "https://gifimage.net/wp-content/uploads/2017/11/eating-anime-gif-4.gif",
                "https://cdn.discordapp.com/attachments/499629904380297226/708661265689739274/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f776174747061642d6d656469612d736572766963652f53746f.gif",
                "https://cdn.discordapp.com/attachments/499629904380297226/708661268772290560/unnamed.gif",
                "https://cdn.discordapp.com/attachments/499629904380297226/708661270336897054/GsUMxxu.gif",
                "https://tenor.com/view/holo-spice-and-wolf-anime-cartoon-japanese-gif-9404307",
                "https://i.gifer.com/BWYz.gif"
        };
    }

}
