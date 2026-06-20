package com.example.data.model

data class MissionObject(
    val name: String,
    val iconName: String, // Icon reference (like "local_activity")
    val difficulty: String,
    val synonyms: List<String> // Expected ML Kit label outputs
)

object ObjectPool {
    val items = listOf(
        // Easy
        MissionObject("Toothbrush", "brush", "Easy", listOf("toothbrush", "brush", "toiletries")),
        MissionObject("Mug", "local_cafe", "Easy", listOf("mug", "cup", "coffee cup", "teacup", "coffeecup", "drinkware")),
        MissionObject("Book", "book", "Easy", listOf("book", "novel", "publication", "hardcover", "textbook")),
        MissionObject("Pen", "edit", "Easy", listOf("pen", "pencil", "office supplies", "writing instrument")),
        MissionObject("Keys", "key", "Easy", listOf("key", "keys", "keychain")),

        // Medium
        MissionObject("Shoes", "sports_handball", "Medium", listOf("shoe", "footwear", "sneaker", "boot")),
        MissionObject("Water Bottle", "local_drink", "Medium", listOf("water bottle", "bottle", "plastic bottle", "drink container", "flask")),
        MissionObject("Pillow", "bed", "Medium", listOf("pillow", "cushion", "bedding", "textile")),
        MissionObject("Towel", "dry", "Medium", listOf("towel", "cloth", "textile", "bathroom")),
        MissionObject("Wallet", "monetization_on", "Medium", listOf("wallet", "purse", "pouch", "leather")),

        // Hard
        MissionObject("Chair", "chair", "Hard", listOf("chair", "stool", "armchair", "seat", "furniture")),
        MissionObject("Remote Control", "settings_remote", "Hard", listOf("remote control", "remote", "electronics", "gadget")),
        MissionObject("Laptop", "laptop", "Hard", listOf("laptop", "computer", "notebook", "netbook", "personal computer")),
        MissionObject("Backpack", "backpack", "Hard", listOf("backpack", "bag", "luggage")),
        MissionObject("Spectacles", "visibility", "Hard", listOf("spectacles", "glasses", "eyewear", "goggles", "sunglasses"))
    )

    fun getItemsForDifficulty(difficulty: String): List<MissionObject> {
        return items.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
    }

    fun getRandomItemForDifficulty(difficulty: String): MissionObject {
        val filtered = getItemsForDifficulty(difficulty)
        return if (filtered.isEmpty()) items.random() else filtered.random()
    }

    // Match list of labels with synonyms
    fun matches(targetName: String, labels: List<String>): Boolean {
        // Find mission object
        val mission = items.find { it.name.equals(targetName, ignoreCase = true) } ?: return false
        val lowerCaseLabels = labels.map { it.lowercase() }
        
        return mission.synonyms.any { synonym ->
            lowerCaseLabels.any { label -> label.contains(synonym.lowercase()) || synonym.lowercase().contains(label) }
        }
    }
}
