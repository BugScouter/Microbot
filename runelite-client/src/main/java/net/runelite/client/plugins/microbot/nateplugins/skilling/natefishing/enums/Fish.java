package net.runelite.client.plugins.microbot.nateplugins.skilling.natefishing.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.FishingSpot;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Fish
{
    SHRIMP(
            "shrimp/anchovies",
            FishingSpot.SHRIMP.getIds(),
            List.of("net", "small net"),
            List.of("raw shrimps","raw anchovies")
    ),
    SARDINE(
            "sardine/herring",
            FishingSpot.SHRIMP.getIds(),
            List.of("bait"),
            List.of("raw sardine", "raw herring")
    ),
    MACKEREL(
            "mackerel/cod/bass",
            FishingSpot.SHARK.getIds(),
            List.of("big net"),
            List.of("raw mackerel", "raw cod", "raw bass")
    ),
    TROUT(
            "trout/salmon",
            FishingSpot.SALMON.getIds(),
            List.of("lure"),
            List.of("raw trout", "raw salmon")
    ),
    PIKE(
            "pike",
            FishingSpot.SALMON.getIds(),
            List.of("bait"),
            List.of("raw pike")
    ),
    TUNA(
            "tuna/swordfish",
            FishingSpot.LOBSTER.getIds(),
            List.of("harpoon"),
            List.of("raw tuna", "raw swordfish")
    ),
    CAVE_EEL(
            "cave eel",
            FishingSpot.CAVE_EEL.getIds(),
            List.of("bait"),
            List.of("raw cave eel")
    ),
    LOBSTER(
            "lobster",
            FishingSpot.LOBSTER.getIds(),
            List.of("cage"),
            List.of("raw lobster")
    ),
    MONKFISH(
            "monkfish",
            FishingSpot.MONKFISH.getIds(),
            List.of("net"),
            List.of("raw monkfish")
    ),
    KARAMBWANJI(
            "karambwanji",
            FishingSpot.KARAMBWANJI.getIds(),
            List.of("net"),
            List.of("raw karambwanji")
    ),
    LAVA_EEL(
            "lava eel",
            FishingSpot.LAVA_EEL.getIds(),
            List.of("lure"),
            List.of("raw lava eel")
    ),
    SHARK(
            "shark",
            FishingSpot.SHARK.getIds(),
            List.of("harpoon"),
            List.of("raw shark")
    ),
    ANGLERFISH(
            "anglerfish",
            FishingSpot.ANGLERFISH.getIds(),
            List.of("sandworms", "bait"),
            List.of("raw anglerfish")
    ),
    KARAMBWAN(
            "karambwan",
            FishingSpot.KARAMBWAN.getIds(),
            List.of("fish"),
            List.of("raw karambwan")
    ),
    BARB_FISH(
            "Barbarian Fishing",
            FishingSpot.BARB_FISH.getIds(),
            List.of("use-rod"),
            List.of("leaping trout", "leaping salmon", "leaping sturgeon")
    );


    private final String name;
    private final int[] fishingSpot;
    private final List<String> actions;
    private final List<String> rawNames;

    @Override
    public String toString()
    {
        return name;
    }
}
