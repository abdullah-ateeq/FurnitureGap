package com.example.furnituregap.data.model

data class FurniturePresetItem(
    val name: String,
    val category: String,
    val widthMm: Double,
    val depthMm: Double,
    val heightMm: Double = 850.0,
    val requiredClearanceMm: Double = 600.0,
    val colorHex: Long = 0xFF2A52BE
) {
    fun toFurnitureObject(xMm: Double = 500.0, yMm: Double = 500.0): FurnitureObject {
        return FurnitureObject(
            name = name,
            category = category,
            widthMm = widthMm,
            depthMm = depthMm,
            heightMm = heightMm,
            xMm = xMm,
            yMm = yMm,
            rotationDegrees = 0,
            requiredClearanceMm = requiredClearanceMm,
            notes = "",
            colorHex = colorHex
        )
    }
}

object FurniturePresets {
    val all: Map<String, List<FurniturePresetItem>> = mapOf(
        "Living Room" to listOf(
            FurniturePresetItem("2-seat Sofa", "Living Room", 1600.0, 900.0, 850.0, 600.0, 0xFF2C3E50),
            FurniturePresetItem("3-seat Sofa", "Living Room", 2000.0, 900.0, 850.0, 600.0, 0xFF34495E),
            FurniturePresetItem("Sectional Sofa", "Living Room", 2800.0, 1800.0, 850.0, 750.0, 0xFF2C3E50),
            FurniturePresetItem("Armchair", "Living Room", 900.0, 850.0, 850.0, 600.0, 0xFF4A6572),
            FurniturePresetItem("Recliner", "Living Room", 950.0, 950.0, 1000.0, 700.0, 0xFF34495E),
            FurniturePresetItem("Coffee Table", "Living Room", 1200.0, 600.0, 450.0, 450.0, 0xFF8D6E63),
            FurniturePresetItem("Side Table", "Living Room", 500.0, 500.0, 550.0, 400.0, 0xFFA1887F),
            FurniturePresetItem("TV Stand", "Living Room", 1800.0, 450.0, 500.0, 600.0, 0xFF5D4037),
            FurniturePresetItem("TV", "Living Room", 1250.0, 150.0, 750.0, 300.0, 0xFF212121),
            FurniturePresetItem("Bookshelf", "Living Room", 900.0, 350.0, 1800.0, 600.0, 0xFF6D4C41),
            FurniturePresetItem("Cabinet", "Living Room", 1000.0, 450.0, 1100.0, 600.0, 0xFF546E7A)
        ),
        "Bedroom" to listOf(
            FurniturePresetItem("Single Bed", "Bedroom", 1000.0, 2000.0, 600.0, 600.0, 0xFF3F51B5),
            FurniturePresetItem("Double Bed", "Bedroom", 1400.0, 2000.0, 600.0, 600.0, 0xFF3949AB),
            FurniturePresetItem("Queen Bed", "Bedroom", 1600.0, 2000.0, 600.0, 600.0, 0xFF303F9F),
            FurniturePresetItem("King Bed", "Bedroom", 1900.0, 2000.0, 600.0, 600.0, 0xFF283593),
            FurniturePresetItem("Nightstand", "Bedroom", 500.0, 450.0, 550.0, 300.0, 0xFF795548),
            FurniturePresetItem("Dresser", "Bedroom", 1200.0, 500.0, 850.0, 750.0, 0xFF5D4037),
            FurniturePresetItem("Wardrobe", "Bedroom", 1500.0, 600.0, 2000.0, 900.0, 0xFF4E342E),
            FurniturePresetItem("Dressing Table", "Bedroom", 1100.0, 500.0, 750.0, 600.0, 0xFF8D6E63),
            FurniturePresetItem("Chair", "Bedroom", 550.0, 550.0, 850.0, 500.0, 0xFF607D8B)
        ),
        "Dining" to listOf(
            FurniturePresetItem("Dining Table", "Dining", 1800.0, 900.0, 750.0, 900.0, 0xFF795548),
            FurniturePresetItem("Dining Chair", "Dining", 500.0, 500.0, 850.0, 600.0, 0xFF8D6E63),
            FurniturePresetItem("Bench", "Dining", 1400.0, 400.0, 450.0, 500.0, 0xFFA1887F),
            FurniturePresetItem("Sideboard", "Dining", 1600.0, 450.0, 850.0, 750.0, 0xFF5D4037),
            FurniturePresetItem("Buffet", "Dining", 1500.0, 500.0, 900.0, 750.0, 0xFF4E342E)
        ),
        "Office" to listOf(
            FurniturePresetItem("Desk", "Office", 1400.0, 700.0, 750.0, 800.0, 0xFF37474F),
            FurniturePresetItem("Office Chair", "Office", 650.0, 650.0, 950.0, 750.0, 0xFF263238),
            FurniturePresetItem("Filing Cabinet", "Office", 500.0, 500.0, 1000.0, 600.0, 0xFF455A64),
            FurniturePresetItem("Bookshelf", "Office", 800.0, 350.0, 1800.0, 600.0, 0xFF546E7A)
        ),
        "Kitchen" to listOf(
            FurniturePresetItem("Kitchen Cabinet", "Kitchen", 600.0, 600.0, 900.0, 900.0, 0xFF78909C),
            FurniturePresetItem("Refrigerator", "Kitchen", 750.0, 700.0, 1800.0, 900.0, 0xFFB0BEC5),
            FurniturePresetItem("Oven", "Kitchen", 600.0, 600.0, 850.0, 900.0, 0xFF37474F),
            FurniturePresetItem("Dishwasher", "Kitchen", 600.0, 600.0, 850.0, 900.0, 0xFF455A64),
            FurniturePresetItem("Washing Machine", "Kitchen", 600.0, 600.0, 850.0, 900.0, 0xFF90A4AE)
        ),
        "Other" to listOf(
            FurniturePresetItem("Rug", "Other", 2400.0, 1700.0, 10.0, 0.0, 0xFFC5CAE9),
            FurniturePresetItem("Plant", "Other", 400.0, 400.0, 1100.0, 300.0, 0xFF2E7D32),
            FurniturePresetItem("Storage Box", "Other", 800.0, 500.0, 450.0, 400.0, 0xFF8D6E63),
            FurniturePresetItem("Custom Object", "Other", 1000.0, 1000.0, 800.0, 600.0, 0xFF1976D2)
        )
    )
}
