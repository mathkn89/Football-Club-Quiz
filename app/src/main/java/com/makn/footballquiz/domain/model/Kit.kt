package com.makn.footballquiz.domain.model

/** A club's home kit as plain colours — drawn by the app, never the club's own artwork. */
data class Kit(
    val pattern: KitPattern,
    val primary: KitColour,
    /** Null for a plain one-colour shirt. */
    val secondary: KitColour?,
    val shorts: KitColour,
) {
    /** Colour families on the shirt, used to keep look-alike kits out of the same question. */
    val shirtFamilies: Set<KitColour.Family> get() = setOfNotNull(primary.family, secondary?.family)

    companion object {
        /** Null when any part is missing or unknown (e.g. a row synced before kits existed). */
        fun from(pattern: String?, primary: String?, secondary: String?, shorts: String?): Kit? {
            val kitPattern = KitPattern.fromRaw(pattern) ?: return null
            val main = KitColour.fromRaw(primary) ?: return null
            val shortsColour = KitColour.fromRaw(shorts) ?: return null
            val second = KitColour.fromRaw(secondary)
            if (kitPattern != KitPattern.PLAIN && second == null) return null
            return Kit(kitPattern, main, second.takeIf { kitPattern != KitPattern.PLAIN }, shortsColour)
        }
    }
}

enum class KitPattern {
    PLAIN, SLEEVES, STRIPES, HOOPS, HALVES, QUARTERS;

    companion object {
        fun fromRaw(raw: String?): KitPattern? = entries.find { it.name.equals(raw?.trim(), ignoreCase = true) }
    }
}

enum class KitColour(val family: Family) {
    RED(Family.RED),
    WHITE(Family.WHITE),
    BLACK(Family.BLACK),
    NAVY(Family.BLUE),
    BLUE(Family.BLUE),
    SKY(Family.SKY),
    CLARET(Family.CLARET),
    AMBER(Family.YELLOW),
    GOLD(Family.YELLOW),
    YELLOW(Family.YELLOW),
    ORANGE(Family.ORANGE),
    GREEN(Family.GREEN),
    LIME(Family.GREEN),
    ;

    /** Colours a player could mistake for one another (navy vs blue, amber vs gold vs yellow). */
    enum class Family { RED, WHITE, BLACK, BLUE, SKY, CLARET, YELLOW, ORANGE, GREEN }

    companion object {
        fun fromRaw(raw: String?): KitColour? = entries.find { it.name.equals(raw?.trim(), ignoreCase = true) }
    }
}
