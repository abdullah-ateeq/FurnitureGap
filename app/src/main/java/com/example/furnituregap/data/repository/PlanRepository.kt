package com.example.furnituregap.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.furnituregap.data.model.AppSettings
import com.example.furnituregap.data.model.AppThemeMode
import com.example.furnituregap.data.model.CalculationHistoryItem
import com.example.furnituregap.data.model.ClearancePresets
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.Door
import com.example.furnituregap.data.model.FloorPlan
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.data.model.Window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PlanRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("furnituregap_prefs", Context.MODE_PRIVATE)

    private val _plans = MutableStateFlow<List<FloorPlan>>(emptyList())
    val plans: StateFlow<List<FloorPlan>> = _plans.asStateFlow()

    private val _history = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
    val history: StateFlow<List<CalculationHistoryItem>> = _history.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
        loadPlans()
        loadHistory()
        if (_plans.value.isEmpty()) {
            initSamplePlans()
        }
    }

    // --- PLANS ---

    private fun loadPlans() {
        val jsonStr = prefs.getString("saved_plans_json", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<FloorPlan>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(deserializePlan(obj))
            }
            _plans.value = list
        } catch (e: Exception) {
            _plans.value = emptyList()
        }
    }

    private fun savePlansToPrefs(list: List<FloorPlan>) {
        val array = JSONArray()
        list.forEach { array.put(serializePlan(it)) }
        prefs.edit().putString("saved_plans_json", array.toString()).apply()
        _plans.value = list
    }

    fun savePlan(plan: FloorPlan) {
        val current = _plans.value.toMutableList()
        val index = current.indexOfFirst { it.id == plan.id }
        val updated = plan.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(0, updated)
        }
        savePlansToPrefs(current)
        saveLastPlanId(updated.id)
    }

    fun deletePlan(id: String) {
        val updated = _plans.value.filter { it.id != id }
        savePlansToPrefs(updated)
    }

    fun duplicatePlan(id: String): FloorPlan? {
        val original = _plans.value.find { it.id == id } ?: return null
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val current = _plans.value.toMutableList()
        current.add(0, copy)
        savePlansToPrefs(current)
        return copy
    }

    fun renamePlan(id: String, newName: String) {
        val current = _plans.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = current[index].copy(name = newName, updatedAt = System.currentTimeMillis())
            savePlansToPrefs(current)
        }
    }

    fun getLastPlan(): FloorPlan? {
        val lastId = prefs.getString("last_plan_id", null)
        return if (lastId != null) {
            _plans.value.find { it.id == lastId } ?: _plans.value.firstOrNull()
        } else {
            _plans.value.firstOrNull()
        }
    }

    private fun saveLastPlanId(id: String) {
        prefs.edit().putString("last_plan_id", id).apply()
    }

    // --- HISTORY (MAX 10) ---

    private fun loadHistory() {
        val jsonStr = prefs.getString("history_json", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<CalculationHistoryItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CalculationHistoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        subtitle = obj.optString("subtitle", ""),
                        calculationMode = obj.optString("calculationMode", "Wall"),
                        clearanceMm = obj.optDouble("clearanceMm", 0.0),
                        requiredClearanceMm = obj.optDouble("requiredClearanceMm", 600.0),
                        status = try {
                            ClearanceStatus.valueOf(obj.optString("status", "ADEQUATE"))
                        } catch (e: Exception) {
                            ClearanceStatus.ADEQUATE
                        },
                        details = obj.optString("details", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        snapshotJson = obj.optString("snapshotJson", "")
                    )
                )
            }
            _history.value = list
        } catch (e: Exception) {
            _history.value = emptyList()
        }
    }

    fun addHistoryItem(item: CalculationHistoryItem) {
        val current = _history.value.toMutableList()
        current.add(0, item)
        // Keep max 10
        val trimmed = if (current.size > 10) current.subList(0, 10) else current
        _history.value = trimmed

        val array = JSONArray()
        trimmed.forEach { h ->
            val o = JSONObject()
            o.put("id", h.id)
            o.put("title", h.title)
            o.put("subtitle", h.subtitle)
            o.put("calculationMode", h.calculationMode)
            o.put("clearanceMm", h.clearanceMm)
            o.put("requiredClearanceMm", h.requiredClearanceMm)
            o.put("status", h.status.name)
            o.put("details", h.details)
            o.put("timestamp", h.timestamp)
            o.put("snapshotJson", h.snapshotJson)
            array.put(o)
        }
        prefs.edit().putString("history_json", array.toString()).apply()
    }

    fun deleteHistoryItem(id: String) {
        val updated = _history.value.filter { it.id != id }
        _history.value = updated
        val array = JSONArray()
        updated.forEach { h ->
            val o = JSONObject()
            o.put("id", h.id)
            o.put("title", h.title)
            o.put("subtitle", h.subtitle)
            o.put("calculationMode", h.calculationMode)
            o.put("clearanceMm", h.clearanceMm)
            o.put("requiredClearanceMm", h.requiredClearanceMm)
            o.put("status", h.status.name)
            o.put("details", h.details)
            o.put("timestamp", h.timestamp)
            o.put("snapshotJson", h.snapshotJson)
            array.put(o)
        }
        prefs.edit().putString("history_json", array.toString()).apply()
    }

    fun duplicateHistoryItem(id: String): CalculationHistoryItem? {
        val orig = _history.value.find { it.id == id } ?: return null
        val copy = orig.copy(
            id = UUID.randomUUID().toString(),
            title = "${orig.title} (Copy)",
            timestamp = System.currentTimeMillis()
        )
        addHistoryItem(copy)
        return copy
    }

    // --- SETTINGS ---

    private fun loadSettings() {
        val themeStr = prefs.getString("setting_theme", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val themeMode = try { AppThemeMode.valueOf(themeStr) } catch (e: Exception) { AppThemeMode.SYSTEM }
        val dynamicColor = prefs.getBoolean("setting_dynamic_color", true)
        val unitStr = prefs.getString("setting_unit", LengthUnit.CENTIMETERS.name) ?: LengthUnit.CENTIMETERS.name
        val unit = try { LengthUnit.valueOf(unitStr) } catch (e: Exception) { LengthUnit.CENTIMETERS }
        val gridSpacing = prefs.getFloat("setting_grid_spacing", 500f).toDouble()
        val snap = prefs.getBoolean("setting_snap_to_grid", false)
        val targetMm = prefs.getFloat("setting_default_target", 600f).toDouble()
        val decimals = prefs.getInt("setting_decimals", 2)

        val tight = prefs.getFloat("setting_preset_tight", 450f).toDouble()
        val comfy = prefs.getFloat("setting_preset_comfy", 600f).toDouble()
        val spacious = prefs.getFloat("setting_preset_spacious", 900f).toDouble()

        _settings.value = AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            unit = unit,
            defaultGridSpacingMm = gridSpacing,
            snapToGrid = snap,
            defaultTargetClearanceMm = targetMm,
            presets = ClearancePresets(tightMm = tight, comfortableMm = comfy, spaciousMm = spacious),
            decimalPlaces = decimals,
            showDimensions = prefs.getBoolean("setting_show_dimensions", true),
            showGrid = prefs.getBoolean("setting_show_grid", true),
            showClearanceLines = prefs.getBoolean("setting_show_clearance_lines", true),
            showRoomOccupancy = prefs.getBoolean("setting_show_occupancy", true),
            showRoomDiagram = prefs.getBoolean("setting_show_room_diagram", true),
            showClearanceMatrix = prefs.getBoolean("setting_show_matrix", true),
            showDimensionLines = prefs.getBoolean("setting_show_dimension_lines", true),
            showObjectLabels = prefs.getBoolean("setting_show_object_labels", true),
            showCalculationBreakdown = prefs.getBoolean("setting_show_breakdown", true),
            liveCalculation = prefs.getBoolean("setting_live_calc", true),
            haptics = prefs.getBoolean("setting_haptics", true),
            animations = prefs.getBoolean("setting_animations", true),
            confirmDelete = prefs.getBoolean("setting_confirm_delete", true),
            restoreLastPlan = prefs.getBoolean("setting_restore_last_plan", true)
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putString("setting_theme", newSettings.themeMode.name)
            .putBoolean("setting_dynamic_color", newSettings.dynamicColor)
            .putString("setting_unit", newSettings.unit.name)
            .putFloat("setting_grid_spacing", newSettings.defaultGridSpacingMm.toFloat())
            .putBoolean("setting_snap_to_grid", newSettings.snapToGrid)
            .putFloat("setting_default_target", newSettings.defaultTargetClearanceMm.toFloat())
            .putInt("setting_decimals", newSettings.decimalPlaces)
            .putFloat("setting_preset_tight", newSettings.presets.tightMm.toFloat())
            .putFloat("setting_preset_comfy", newSettings.presets.comfortableMm.toFloat())
            .putFloat("setting_preset_spacious", newSettings.presets.spaciousMm.toFloat())
            .putBoolean("setting_show_dimensions", newSettings.showDimensions)
            .putBoolean("setting_show_grid", newSettings.showGrid)
            .putBoolean("setting_show_clearance_lines", newSettings.showClearanceLines)
            .putBoolean("setting_show_occupancy", newSettings.showRoomOccupancy)
            .putBoolean("setting_show_room_diagram", newSettings.showRoomDiagram)
            .putBoolean("setting_show_matrix", newSettings.showClearanceMatrix)
            .putBoolean("setting_show_dimension_lines", newSettings.showDimensionLines)
            .putBoolean("setting_show_object_labels", newSettings.showObjectLabels)
            .putBoolean("setting_show_breakdown", newSettings.showCalculationBreakdown)
            .putBoolean("setting_live_calc", newSettings.liveCalculation)
            .putBoolean("setting_haptics", newSettings.haptics)
            .putBoolean("setting_animations", newSettings.animations)
            .putBoolean("setting_confirm_delete", newSettings.confirmDelete)
            .putBoolean("setting_restore_last_plan", newSettings.restoreLastPlan)
            .apply()
    }

    fun resetSettings() {
        val def = AppSettings()
        updateSettings(def)
    }

    // --- JSON Serialization Helpers ---

    private fun serializePlan(plan: FloorPlan): JSONObject {
        val obj = JSONObject()
        obj.put("id", plan.id)
        obj.put("name", plan.name)
        obj.put("roomWidthMm", plan.roomWidthMm)
        obj.put("roomDepthMm", plan.roomDepthMm)
        obj.put("createdAt", plan.createdAt)
        obj.put("updatedAt", plan.updatedAt)

        val objArray = JSONArray()
        plan.objects.forEach { f ->
            val fObj = JSONObject()
            fObj.put("id", f.id)
            fObj.put("name", f.name)
            fObj.put("category", f.category)
            fObj.put("widthMm", f.widthMm)
            fObj.put("depthMm", f.depthMm)
            fObj.put("heightMm", f.heightMm)
            fObj.put("xMm", f.xMm)
            fObj.put("yMm", f.yMm)
            fObj.put("rotationDegrees", f.rotationDegrees)
            fObj.put("requiredClearanceMm", f.requiredClearanceMm)
            fObj.put("notes", f.notes)
            fObj.put("colorHex", f.colorHex)
            objArray.put(fObj)
        }
        obj.put("objects", objArray)

        val doorArray = JSONArray()
        plan.doors.forEach { d ->
            val dObj = JSONObject()
            dObj.put("id", d.id)
            dObj.put("name", d.name)
            dObj.put("hingeXmm", d.hingeXmm)
            dObj.put("hingeYmm", d.hingeYmm)
            dObj.put("widthMm", d.widthMm)
            dObj.put("swingDegrees", d.swingDegrees)
            dObj.put("rotation", d.rotation)
            doorArray.put(dObj)
        }
        obj.put("doors", doorArray)

        val winArray = JSONArray()
        plan.windows.forEach { w ->
            val wObj = JSONObject()
            wObj.put("id", w.id)
            wObj.put("name", w.name)
            wObj.put("xMm", w.xMm)
            wObj.put("yMm", w.yMm)
            wObj.put("widthMm", w.widthMm)
            wObj.put("sillHeightMm", w.sillHeightMm)
            wObj.put("heightMm", w.heightMm)
            winArray.put(wObj)
        }
        obj.put("windows", winArray)

        return obj
    }

    private fun deserializePlan(obj: JSONObject): FloorPlan {
        val id = obj.optString("id", UUID.randomUUID().toString())
        val name = obj.optString("name", "Untitled Layout")
        val roomWidthMm = obj.optDouble("roomWidthMm", 5000.0)
        val roomDepthMm = obj.optDouble("roomDepthMm", 4000.0)
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())

        val objects = mutableListOf<FurnitureObject>()
        val objArray = obj.optJSONArray("objects") ?: JSONArray()
        for (i in 0 until objArray.length()) {
            val f = objArray.getJSONObject(i)
            objects.add(
                FurnitureObject(
                    id = f.optString("id", UUID.randomUUID().toString()),
                    name = f.optString("name", "Furniture"),
                    category = f.optString("category", "General"),
                    widthMm = f.optDouble("widthMm", 1000.0),
                    depthMm = f.optDouble("depthMm", 1000.0),
                    heightMm = f.optDouble("heightMm", 850.0),
                    xMm = f.optDouble("xMm", 0.0),
                    yMm = f.optDouble("yMm", 0.0),
                    rotationDegrees = f.optInt("rotationDegrees", 0),
                    requiredClearanceMm = f.optDouble("requiredClearanceMm", 600.0),
                    notes = f.optString("notes", ""),
                    colorHex = f.optLong("colorHex", 0xFF2A52BE)
                )
            )
        }

        val doors = mutableListOf<Door>()
        val doorArray = obj.optJSONArray("doors") ?: JSONArray()
        for (i in 0 until doorArray.length()) {
            val d = doorArray.getJSONObject(i)
            doors.add(
                Door(
                    id = d.optString("id", UUID.randomUUID().toString()),
                    name = d.optString("name", "Door"),
                    hingeXmm = d.optDouble("hingeXmm", 0.0),
                    hingeYmm = d.optDouble("hingeYmm", 0.0),
                    widthMm = d.optDouble("widthMm", 900.0),
                    swingDegrees = d.optDouble("swingDegrees", 90.0),
                    rotation = d.optDouble("rotation", 0.0)
                )
            )
        }

        val windows = mutableListOf<Window>()
        val winArray = obj.optJSONArray("windows") ?: JSONArray()
        for (i in 0 until winArray.length()) {
            val w = winArray.getJSONObject(i)
            windows.add(
                Window(
                    id = w.optString("id", UUID.randomUUID().toString()),
                    name = w.optString("name", "Window"),
                    xMm = w.optDouble("xMm", 0.0),
                    yMm = w.optDouble("yMm", 0.0),
                    widthMm = w.optDouble("widthMm", 1500.0),
                    sillHeightMm = w.optDouble("sillHeightMm", 900.0),
                    heightMm = w.optDouble("heightMm", 1200.0)
                )
            )
        }

        return FloorPlan(
            id = id,
            name = name,
            roomWidthMm = roomWidthMm,
            roomDepthMm = roomDepthMm,
            objects = objects,
            doors = doors,
            windows = windows,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun initSamplePlans() {
        val sample1 = FloorPlan(
            id = "sample_living_room",
            name = "Living Room Arrangement",
            roomWidthMm = 5000.0,
            roomDepthMm = 4000.0,
            objects = listOf(
                FurnitureObject(
                    id = "lr_sofa",
                    name = "3-seat Sofa",
                    category = "Living Room",
                    widthMm = 2000.0,
                    depthMm = 900.0,
                    heightMm = 850.0,
                    xMm = 500.0,
                    yMm = 1000.0,
                    rotationDegrees = 0,
                    requiredClearanceMm = 600.0,
                    colorHex = 0xFF2C3E50
                ),
                FurnitureObject(
                    id = "lr_table",
                    name = "Coffee Table",
                    category = "Living Room",
                    widthMm = 1200.0,
                    depthMm = 600.0,
                    heightMm = 450.0,
                    xMm = 500.0,
                    yMm = 2300.0,
                    rotationDegrees = 0,
                    requiredClearanceMm = 450.0,
                    colorHex = 0xFF8D6E63
                ),
                FurnitureObject(
                    id = "lr_tv",
                    name = "TV Stand",
                    category = "Living Room",
                    widthMm = 1800.0,
                    depthMm = 450.0,
                    heightMm = 500.0,
                    xMm = 500.0,
                    yMm = 3300.0,
                    rotationDegrees = 0,
                    requiredClearanceMm = 600.0,
                    colorHex = 0xFF5D4037
                )
            ),
            doors = listOf(
                Door(
                    id = "lr_door",
                    name = "Main Door",
                    hingeXmm = 4100.0,
                    hingeYmm = 0.0,
                    widthMm = 900.0,
                    swingDegrees = 90.0,
                    rotation = 0.0
                )
            ),
            windows = listOf(
                Window(
                    id = "lr_win",
                    name = "Balcony Window",
                    xMm = 1500.0,
                    yMm = 0.0,
                    widthMm = 1800.0,
                    sillHeightMm = 900.0,
                    heightMm = 1400.0
                )
            )
        )
        savePlan(sample1)
    }
}
